package ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.proxy;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.Map;

public class LoggerInvocationHandler implements InvocationHandler {

    private final Object loggedObject;
    private final Writer writer;
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        
        Object result = null;
        
        if (method.getDeclaringClass().equals(Object.class)) {
            try {
                return method.invoke(loggedObject, args);
            } catch (InvocationTargetException e) {
                Throwable targetException = e.getTargetException();
                throw targetException;
            }
        }
        
        JSONObject logEntry = new JSONObject();
        logEntry.put("timestamp", System.currentTimeMillis());
        logEntry.put("class", loggedObject.getClass().getName());
        logEntry.put("method", method.getName());
        
        JSONArray arguments = new JSONArray();
        if (args != null) {
            for (int i = 0; i < args.length; ++i) {
                logArgument(args[i], arguments, new IdentityHashMap<Object, Object>());
            }
        }
        logEntry.put("arguments", arguments);
        
        try {
            result = method.invoke(loggedObject, args);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            logEntry.put("thrown", targetException.toString());
            writer.write(logEntry.toString() + '\n');
            throw targetException;
        } catch (Exception e) {
            // Proxy can't throw own exceptions -> ignore
        }
        
        if (method.getReturnType() != void.class) {
            logEntry.put("returnValue", (result != null) ? result : JSONObject.NULL);
        }
        
        try {
            writer.write(logEntry.toString() + '\n');
        } catch (Exception e) {
            // Proxy can't throw own excetpions -> ignore
        }
        
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private void logArgument(Object argument, JSONArray arguments, Map<Object, Object> cycles) {
        if (argument == null) {
            arguments.put(JSONObject.NULL);
        } else if (cycles.containsKey(argument)) {
            arguments.put("cyclic");
        } else if (Iterable.class.isAssignableFrom(argument.getClass())) {
            JSONArray list = new JSONArray();
            cycles.put(argument, null);
            for (Object element : (Iterable<Object>) argument) {
                logArgument(element, list, cycles);
            }
            cycles.remove(argument);
            arguments.put(list);
        } else if (argument.getClass().isArray()) {
            arguments.put(argument.toString());
        } else {
            arguments.put(argument);
        }
    }

    LoggerInvocationHandler(Object target, Writer writer) {
        this.loggedObject = target;
        this.writer = writer;
    }
    
}
