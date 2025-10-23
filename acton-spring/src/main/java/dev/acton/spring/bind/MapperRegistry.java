package dev.acton.spring.bind;

import dev.acton.core.bind.ContractMapper;
import java.util.List;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ResolvableType;

public final class MapperRegistry {
    private final ListableBeanFactory factory;

    public MapperRegistry(ListableBeanFactory factory) { this.factory = factory; }


    @SuppressWarnings("unchecked")
    public <Q, D> ContractMapper<Q, D> findUnique(Class<Q> contract, Class<D> target) {
        ResolvableType type = ResolvableType.forClassWithGenerics(ContractMapper.class, contract, target);

        ObjectProvider<?> provider = factory.getBeanProvider(type);

        Object preferred = provider.getIfAvailable();
        if (preferred != null) {
            return (ContractMapper<Q, D>) preferred;
        }

        List<?> candidates = provider.stream().toList();
        if (candidates.isEmpty()) {
            throw new IllegalStateException("No ContractMapper<" + contract.getSimpleName() + "," + target.getSimpleName() + "> found");
        }
        if (candidates.size() == 1) {
            return (ContractMapper<Q, D>) candidates.getFirst();
        }

        throw new IllegalStateException(
                "Multiple ContractMapper<" + contract.getSimpleName() + "," + target.getSimpleName() + "> found. " +
                        "Mark one as @Primary or specify @FromContract(using=...).");
    }
}
