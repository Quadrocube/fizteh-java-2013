package ru.fizteh.fivt.students.sterzhanovVladislav.fileMap;

import java.io.IOException;
import java.util.List;

import ru.fizteh.fivt.storage.structured.RemoteTableProvider;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.storeable.StoreableUtils;

public class DatabaseContext implements AutoCloseable {
    private final AtomicTableProvider localProvider;
    private RemoteTableProvider remoteProvider = null;
    private AtomicTableProvider activeProvider = null;
    private Table activeMap = null;

    public String remove(String key) throws Exception {
        if (activeMap == null) {
            throw new IllegalStateException("no table");
        }
        Storeable oldValue = activeMap.remove(key);
        if (oldValue == null) {
            return null;
        }
        return activeProvider.serialize(activeMap, oldValue);
    }

    public String get(String key) throws Exception {
        if (activeMap == null) {
            throw new IllegalStateException("no table");
        }
        Storeable value = activeMap.get(key);
        if (value == null) {
            return null;
        }
        return activeProvider.serialize(activeMap, value);
    }

    public String put(String key, String value) throws Exception {
        if (activeMap == null) {
            throw new IllegalStateException("no table");
        }
        Storeable oldValue = activeMap.put(key, activeProvider.deserialize(activeMap, value));
        if (oldValue == null) {
            return null;
        }
        return activeProvider.serialize(activeMap, oldValue);
    }
    
    public int commit() throws IOException {
        if (activeMap == null) {
            throw new IllegalStateException("no table");
        }
        return activeMap.commit();
    }
    
    public int rollback() {
        if (activeMap == null) {
            throw new IllegalStateException("no table");
        }
        return activeMap.rollback();
    }
    
    public int getActiveSize() {
        if (activeMap == null) {
            throw new IllegalStateException("no table");
        }
        return activeMap.size();
    }
    
    public void loadTable(String dbName) throws IllegalStateException, IOException {
        Table newMap = activeProvider.getTable(dbName);
        if (newMap == null) {
            throw new IllegalStateException(dbName + " not exists");
        }
        if (activeMap != null) {
            activeProvider.closeTableIfNotModified(activeMap.getName());
        }
        activeMap = newMap;
    }
    
    public void createTable(String dbName, List<Class<?>> signature) throws IllegalStateException {
        try {
            Table newMap = activeProvider.createTable(dbName, signature);
            if (newMap == null) {
                throw new IllegalStateException(dbName + " exists");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create directory");
        }
    }
    
    public void removeTable(String dbName) throws IllegalStateException, IOException {
        String currentTableName = activeMap.getName();
        activeProvider.removeTable(dbName);
        if (activeMap != null && dbName.equals(currentTableName)) {
            activeMap = null;
        }
    }
    
    public void closeActiveTable() throws IOException {
        if (activeMap != null) {
            activeMap.commit();
            activeMap = null;
        }
    }
    
    public RemoteTableProvider getRemoteProvider() {
        return this.remoteProvider;
    }
    
    public void attachRemoteProvider(RemoteTableProvider provider) throws IllegalStateException {
        if (this.remoteProvider != null) {
            throw new IllegalStateException("not connected: you already have an active connection");
        }
        if (this.activeMap != null) {
            try {
                activeProvider.closeTableIfNotModified(activeMap.getName());
            } catch (IOException e) {
                // Ignore
            }
        }
        activeMap = null;
        this.remoteProvider = provider;
        this.activeProvider = (AtomicTableProvider) provider;
    }
    
    public void disconnectRemoteProvider() throws IllegalStateException {
        if (this.remoteProvider == null) {
            throw new IllegalStateException("not connected");
        }
        if (this.activeMap != null) {
            try {
                activeProvider.closeTableIfNotModified(activeMap.getName());
            } catch (IOException e) {
                // Ignore
            }
        }
        activeMap = null;
        try {
            this.remoteProvider.close();
        } catch (Exception e) {
            // Don't actually care
        }
        this.remoteProvider = null;
        this.activeProvider = localProvider;
    }
    
    public DatabaseContext(AtomicTableProvider provider) throws IllegalStateException, IOException {
        this.localProvider = provider;
        this.activeProvider = provider;
    } 
    
    public void close() throws IOException {
        if (activeMap != null) {
            activeProvider.closeTableIfNotModified(activeMap.getName());
        }
    }
    
    public String describe() {
        if (activeMap == null) {
            throw new IllegalStateException("no database");
        }
        StringBuilder signature = new StringBuilder();
        for (Class<?> type : StoreableUtils.generateSignature(activeMap)) {
            signature.append(StoreableUtils.CLASSES.get(type));
            signature.append(" ");
        }
        return signature.substring(0, signature.length() - 1);
    }
    
    public void exit() {
        if (remoteProvider != null) {
            disconnectRemoteProvider();
        } else {
            System.exit(0);
        }
    }
}
