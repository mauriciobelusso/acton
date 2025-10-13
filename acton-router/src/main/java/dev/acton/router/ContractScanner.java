package dev.acton.router;

import dev.acton.core.annotation.Contract;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Scans the classpath for @Contract-annotated records.
 * Note: in production this should be replaced with an index or AOT processor.
 */
public class ContractScanner {

    public Set<Class<?>> scan(String basePackage) {
        var classes = new HashSet<Class<?>>();
        try {
            var path = basePackage.replace('.', '/');
            var resources = Thread.currentThread().getContextClassLoader().getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                // Simplified: in dev environments, manual registration is preferred.
            }
        } catch (IOException e) {
            throw new RouterException("Failed to scan package: " + basePackage, e);
        }
        return classes;
    }

    public static boolean isContract(Class<?> type) {
        for (Annotation a : type.getAnnotations()) {
            if (a.annotationType() == Contract.class) return true;
        }
        return false;
    }
}
