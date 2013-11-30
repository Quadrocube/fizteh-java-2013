package ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.proxy;

import java.io.Writer;
import java.lang.reflect.Proxy;

import ru.fizteh.fivt.proxy.LoggingProxyFactory;

public class LoggerProxyFactory implements LoggingProxyFactory {

    @Override
    public Object wrap(Writer writer, Object implementation, Class<?> interfaceClass) {
        if (writer == null || implementation == null || interfaceClass == null) {
            throw new IllegalArgumentException("Null is not a valid argument");
        }
        
        if (!interfaceClass.isAssignableFrom(implementation.getClass())) {
            throw new IllegalArgumentException("'implementation' does not implement 'interfaceClass'");
        }
        
        return Proxy.newProxyInstance(
                ClassLoader.getSystemClassLoader(),
                new Class[]{interfaceClass},
                new LoggerInvocationHandler(implementation, writer));
    }
    
    public LoggerProxyFactory() {}

}
