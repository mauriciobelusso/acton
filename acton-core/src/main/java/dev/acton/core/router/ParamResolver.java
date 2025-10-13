package dev.acton.core.router;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public interface ParamResolver {
  boolean supports(Method method, int paramIndex, Parameter param);
  Object resolve(Method method, int paramIndex, Parameter param, ResolutionContext ctx) throws Exception;

  interface ResolutionContext {
    <T> T getBean(Class<T> type);           // Spring context access
    String body();                          // raw body (nullable)
    String pathVariable(String name);       // path var
    String queryParam(String name);         // query param (first value)
    String header(String name);             // header
    ClassLoader classLoader();              // if needed
  }
}
