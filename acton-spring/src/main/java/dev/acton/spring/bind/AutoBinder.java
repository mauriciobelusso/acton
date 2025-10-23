package dev.acton.spring.bind;

import dev.acton.core.annotation.Bind;
import dev.acton.core.annotation.Contract;
import dev.acton.core.annotation.FromContract;
import dev.acton.core.bind.ContractMapper;
import dev.acton.core.bind.MapReturn;
import dev.acton.core.bind.ResultMapper;
import dev.acton.core.store.Store;
import dev.acton.core.store.StoreFactory;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;

public final class AutoBinder implements SmartInitializingSingleton, Ordered {

    private final ApplicationContext ctx;
    private final DefaultActOnBinder binder;
    private final MapperRegistry mappers;
    private final BeanFactory factory;

    public AutoBinder(ApplicationContext ctx,
                      DefaultActOnBinder binder,
                      MapperRegistry mappers,
                      BeanFactory factory) {
        this.ctx = ctx; this.binder = binder; this.mappers = mappers; this.factory = factory;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void afterSingletonsInstantiated() {
        for (String name : ctx.getBeanDefinitionNames()) {
            Object target = runCatching(() -> ctx.getBean(name));
            if (target == null) continue;

            Class<?> targetClass = AopUtils.getTargetClass(target);

            var methodsWithBind = MethodIntrospector.selectMethods(
                    targetClass,
                    (Method m) -> AnnotatedElementUtils.findMergedAnnotation(m, Bind.class)
            );

            if (methodsWithBind.isEmpty()) continue;

            for (Method m : methodsWithBind.keySet()) {
                Method specific = ClassUtils.getMostSpecificMethod(m, targetClass);
                Method bridged  = BridgeMethodResolver.findBridgedMethod(specific);
                bridged.setAccessible(true);

                Bind bind = AnnotatedElementUtils.findMergedAnnotation(bridged, Bind.class);
                if (bind == null) continue;

                Class<?> contractType = bind.value();
                if (contractType.getAnnotation(Contract.class) == null) {
                    throw new IllegalStateException("@Bind(" + contractType.getName() + ") is not a @Contract");
                }

                var mapReturn = m.getAnnotation(MapReturn.class);
                var returnType = m.getGenericReturnType();

                binder.handle(contractType, contract -> {
                    Object result = bridged.invoke(target, buildArgs(bridged, contractType, contract));
                    Object unwrapped = dev.acton.spring.util.TypeUtils.unwrapValue(result);

                    if (mapReturn == null) return unwrapped;

                    Class<?> to = mapReturn.value();

                    if (unwrapped == null) return null;

                    if (unwrapped instanceof java.util.Collection<?> col) {
                        Class<?> srcElem = resolveElementType(returnType);
                        @SuppressWarnings("unchecked")
                        ResultMapper<Object, Object> elemMapper =
                                (ResultMapper<Object, Object>) resolveResultMapper((Class<?>) srcElem, (Class<?>) to, mapReturn);

                        java.util.List<Object> mapped = new java.util.ArrayList<>(col.size());
                        for (Object e : col) {
                            mapped.add(elemMapper.map(e));
                        }
                        return mapped;
                    } else {
                        @SuppressWarnings("unchecked")
                        ResultMapper<Object, Object> mapper =
                                (ResultMapper<Object, Object>) resolveResultMapper((Class<?>) unwrapped.getClass(), (Class<?>) to, mapReturn);
                        return mapper.map(unwrapped);
                    }
                });

            }
        }
    }

    private <T> T runCatching(java.util.concurrent.Callable<T> c) {
        try { return c.call(); } catch (Throwable t) { return null; }
    }

    private Object[] buildArgs(Method m, Class<?> contractType, Object contract) throws Exception {
        var params = m.getParameters();
        var args = new ArrayList<>(params.length);

        boolean atLeastOneFrom = false;
        StoreFactory storeFactory = ctx.getBeanProvider(StoreFactory.class).getIfAvailable();

        for (int i = 0; i < params.length; i++) {
            var p  = params[i];
            var pt = p.getType();
            var fc = p.getAnnotation(FromContract.class);

            if (fc != null) {
                atLeastOneFrom = true;
                Class<?> target = (fc.to() != Void.class) ? fc.to() : pt;

                if (fc.using() != ContractMapper.Auto.class) {
                    var mapper = ctx.getBean(fc.using());
                    Object out = invokeMapper(mapper, contract);
                    ensureAssignable(m, i, pt, out);
                    args.add(out);
                    continue;
                }
                var mapper = mappers.findUnique(contractType, target);
                Object out = invokeMapper(mapper, contract);
                ensureAssignable(m, i, pt, out);
                args.add(out);
                continue;
            }

            if (pt.isAssignableFrom(contractType)) { args.add(contract); continue; }

            if (Store.class.isAssignableFrom(pt)) {
                if (storeFactory == null) {
                    throw new IllegalStateException("No StoreFactory available to resolve parameter " + p);
                }
                Class<?> entityType = org.springframework.core.ResolvableType
                        .forMethodParameter(m, i)
                        .as(Store.class).getGeneric(0).resolve();
                if (entityType == null) {
                    throw new IllegalStateException("Unable to resolve generic type for " + p + " in " + m);
                }
                args.add(storeFactory.create(entityType));
                continue;
            }

            args.add(ctx.getBean(pt));
        }

        if (!atLeastOneFrom) {
            boolean anyAcceptsDto = false;
            for (java.lang.reflect.Parameter param : params) {
                if (param.getType().isAssignableFrom(contractType)) {
                    anyAcceptsDto = true;
                    break;
                }
            }
            if (!anyAcceptsDto) {
                throw new IllegalStateException("No parameter maps/accepts contract " + contractType.getSimpleName()
                        + " in " + m + ". Add @FromContract or accept the DTO type directly.");
            }
        }
        return args.toArray();
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    private Object invokeMapper(Object mapper, Object contract) throws Exception {
        if (mapper instanceof ContractMapper cm) return cm.toDomain(contract);
        var method = mapper.getClass().getMethod("toDomain", contract.getClass());
        return method.invoke(mapper, contract);
    }

    private void ensureAssignable(Method m, int idx, Class<?> paramType, Object out) {
        if (out != null && !paramType.isInstance(out)) {
            throw new IllegalStateException("Mapper result type " + out.getClass().getName()
                    + " not assignable to parameter #" + idx + " (" + paramType.getName() + ") for " + m);
        }
    }

    @SuppressWarnings("unchecked")
    private <S,R> ResultMapper<S,R> resolveResultMapper(Class<S> src, Class<R> dst, MapReturn ann) {
        if (ann.using() != ResultMapper.Auto.class) {
            return (ResultMapper<S, R>) ctx.getBean(ann.using());
        }

        var rt = org.springframework.core.ResolvableType.forClassWithGenerics(ResultMapper.class, src, dst);
        var provider = factory.getBeanProvider(rt);
        var preferred = (ResultMapper<S,R>) provider.getIfAvailable();
        if (preferred != null) return preferred;

        var candidates = provider.stream().toList();
        if (candidates.isEmpty())
            throw new IllegalStateException("No ResultMapper<%s,%s> found".formatted(src.getSimpleName(), dst.getSimpleName()));
        if (candidates.size() > 1)
            throw new IllegalStateException("Multiple ResultMapper<%s,%s>. Mark one as @Primary or use @MapReturn(using=...)."
                    .formatted(src.getSimpleName(), dst.getSimpleName()));
        return (ResultMapper<S,R>) candidates.getFirst();
    }

    private Class<?> resolveElementType(Type genericReturnType) {
        // remove ResponseEntity<>, Optional<>, CompletionStage<> etc.
        Type unwrapped = dev.acton.spring.util.TypeUtils.unwrap(genericReturnType);

        var rt = org.springframework.core.ResolvableType.forType(unwrapped);

        // List<Foo>, Set<Foo>...
        var asCollection = rt.as(java.util.Collection.class);
        if (asCollection != org.springframework.core.ResolvableType.NONE) {
            Class<?> elem = asCollection.getGeneric(0).resolve();
            return (elem != null) ? elem : Object.class;
        }

        // Foo[]
        if (rt.isArray()) {
            Class<?> elem = rt.getComponentType().resolve();
            return (elem != null) ? elem : Object.class;
        }

        // fallback
        return Object.class;
    }
}
