package ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.network;

import java.io.IOException;
import java.net.Socket;
import java.text.ParseException;
import java.util.Hashtable;
import java.util.List;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.RemoteTableProvider;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.AtomicTableProvider;
import ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.IOUtility;
import ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.storeable.StoreableRow;
import ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.storeable.StoreableUtils;

public class RemoteFileMapProvider implements RemoteTableProvider, AtomicTableProvider {
    
    private int port;
    private String host;
    private Socket providerSession;
    
    private Hashtable<String, RemoteFileMap> tableSessions = new Hashtable<String, RemoteFileMap>();

    @Override
    public Table getTable(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Table table = tableSessions.get(name);
        if (table != null) {
            return table;
        }
        Socket tableSession;
        try {
            tableSession = new Socket(host, port);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        String response;
        try {
            response = NetworkUtils.queryResponse(tableSession, "use " + name);
        } catch (IOException e) {
            throw new RuntimeException("Error while trying to query server: " + e.getMessage());
        }
        if (!response.equals("using " + name)) {
            return null;
        }
        String typeNames;
        try {
            typeNames = NetworkUtils.queryResponse(tableSession, "describe");
        } catch (IOException e) {
            throw new RuntimeException("Error while trying to query server: " + e.getMessage());
        }
        if (typeNames.equals(name + " not exists")) {
            return null;
        }
        List<Class<?>> signature = IOUtility.parseSignatureFromString(typeNames);

        try {
            RemoteFileMap result = new RemoteFileMap(name, signature, tableSession);
            tableSessions.put(name, result);
            return result;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public Table createTable(String name, List<Class<?>> columnTypes)
            throws IOException {
        if (name == null || name.isEmpty() || columnTypes == null || columnTypes.isEmpty()) {
            throw new IllegalArgumentException();
        }
        StringBuilder signature = new StringBuilder();
        for (Class<?> type : columnTypes) {
            String typeName = StoreableUtils.CLASSES.get(type);
            if (typeName == null) {
                throw new ColumnFormatException("Unable to handle class: " + typeName);
            }
            signature.append(typeName);
            signature.append(" ");
        }
        String response = NetworkUtils.queryResponse(providerSession, "create " + name 
                + " (" + signature.substring(0, signature.length() - 1) + ")");
        if (!response.equals("created")) {
            if (response.equals(name + " exists")) {
                return null;
            } else {
                throw new RuntimeException(response);
            }
        }
        Socket tableSession;
        try {
            tableSession = new Socket(host, port);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        response = NetworkUtils.queryResponse(tableSession, "use " + name);
        if (!response.equals("using " + name)) {
            throw new RuntimeException(response);
        }
        try {
            RemoteFileMap result = new RemoteFileMap(name, columnTypes, tableSession);
            tableSessions.put(name, result);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void removeTable(String name) throws IOException {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException();
        }
        RemoteFileMap removedMap = tableSessions.remove(name);
        if (removedMap != null) {
            tableSessions.remove(name);
            removedMap.session.close();
        }
        String response = NetworkUtils.queryResponse(providerSession, "drop " + name);
        if (!response.isEmpty()) {
            throw new IOException(response);
        }
    }
    
    @Override
    public Storeable deserialize(Table table, String value)
            throws ParseException {
        return StoreableUtils.deserialize(value, StoreableUtils.generateSignature(table));
    }

    @Override
    public String serialize(Table table, Storeable value)
            throws ColumnFormatException {
        if (!StoreableUtils.validate(value, StoreableUtils.generateSignature(table))) {
            throw new ColumnFormatException("wrong type (can't serialize value according to signature)");
        }
        return StoreableUtils.serialize(value, StoreableUtils.generateSignature(table));
    }

    @Override
    public Storeable createFor(Table table) {
        if (table == null) {
            throw new IllegalArgumentException("null table");
        }
        return new StoreableRow(StoreableUtils.generateSignature(table));
    }

    @Override
    public Storeable createFor(Table table, List<?> values)
            throws ColumnFormatException, IndexOutOfBoundsException {
        if (table == null) {
            throw new IllegalArgumentException("null table");
        }
        return new StoreableRow(StoreableUtils.generateSignature(table), values);
    }

    @Override
    public void close() throws IOException {
        for (RemoteFileMap table : tableSessions.values()) {
            table.close();
        }
        providerSession.close();
    }
    
    public RemoteFileMapProvider(String hostname, int port) throws IOException {
        this.host = hostname;
        this.port = port;
        this.providerSession = new Socket(hostname, port);
    }

    @Override
    public void closeTableIfNotModified(String name) throws IllegalStateException, IOException {
        RemoteFileMap table = tableSessions.get(name);
        if (table == null) {
            throw new IllegalStateException(name + " not exists");
        }
        String response = NetworkUtils.queryResponse(table.session, "use " + name);
        if (!response.equals("using " + name)) {
            throw new IllegalStateException(response);
        }
    }
    
    @Override
    public String toString() {
        return host + " " + port;
    }
    
}
