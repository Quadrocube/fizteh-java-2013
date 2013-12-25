package ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.ParseException;
import java.util.List;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.FileMap;
import ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.storeable.StoreableUtils;

public class RemoteFileMap implements Table, AutoCloseable {

    String name;
    Socket session;
    private List<Class<?>> columnTypes = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private final RemoteFileMapProvider parentProvider;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Storeable get(String key) {
        ensureIsStillAlive();
        ensureValidKey(key);
        return queryStoreableResponse("get " + key);
    }

    @Override
    public Storeable put(String key, Storeable value)
            throws ColumnFormatException {
        ensureIsStillAlive();
        ensureValidKey(key);
        ensureValidValue(value);
        return queryStoreableResponse("put " + key + " " + StoreableUtils.serialize(value, columnTypes));
    }

    @Override
    public Storeable remove(String key) {
        ensureIsStillAlive();
        ensureValidKey(key);
        return queryStoreableResponse("remove " + key);
    }

    @Override
    public int size() {
        ensureIsStillAlive();
        return queryIntResponse("size");
    }

    @Override
    public int commit() throws IOException {
        ensureIsStillAlive();
        return queryIntResponse("commit");
    }

    @Override
    public int rollback() {
        ensureIsStillAlive();
        return queryIntResponse("rollback");
    }

    @Override 
    public int getColumnsCount() {
        ensureIsStillAlive();
        return columnTypes.size();
    }

    @Override
    public Class<?> getColumnType(int columnIndex) throws IndexOutOfBoundsException {
        ensureIsStillAlive();
        if (columnIndex >= columnTypes.size() || columnIndex < 0) {
            throw new IndexOutOfBoundsException();
        }
        return columnTypes.get(columnIndex);
    }

    @Override
    public void close() throws IOException {
        if (isAlive()) {
            rollback();
            if (parentProvider != null) {
                try {
                    parentProvider.resetTable(name);
                } catch (IllegalStateException e) {
                    // Provider is already closed -> just leave it alone.
                }
            }
            session.close();
        }
    }

    public RemoteFileMap(String dbName, List<Class<?>> classes, Socket session, RemoteFileMapProvider provider) 
            throws IOException {
        name = dbName;
        this.parentProvider = provider;
        for (Class<?> type : classes) {
            if (type == null || !StoreableUtils.CLASSES.containsKey(type)) {
                throw new IllegalArgumentException("Invalid column type");
            }
        }
        this.columnTypes = classes;
        this.session = session;
        out = new PrintWriter(session.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(session.getInputStream()));
    }

    private Storeable queryStoreableResponse(String request) {
        String response;
        try {
            response = NetworkUtils.queryResponse(session, in, out, request);
            if (response.equals("not found") || response.equals("new")) {
                return null;
            } else {
                response = NetworkUtils.queryResponse(session, in, out, null);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while trying to query server: " + e.getMessage());
        }
        try {
            return StoreableUtils.deserialize(response, columnTypes);
        } catch (ParseException e) {
            throw new RuntimeException("Error from server received: " + response);
        }
    }

    private int queryIntResponse(String request) {
        String response;
        try {
            response = NetworkUtils.queryResponse(session, request);
        } catch (IOException e) {
            throw new RuntimeException("Error while trying to query server: " + e.getMessage());
        }
        try {
            return Integer.parseInt(response);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Error from server received: " + response);
        }
    }

    public boolean isAlive() {
        return !session.isClosed();
    }
    
    private void ensureIsStillAlive() {
        if (!isAlive()) {
            throw new IllegalStateException("Error: Table " + name + " was already closed");
        }
    }
    
    private void ensureValidKey(String key) {
        if (!FileMap.isValidKey(key)) {
            throw new IllegalArgumentException("Invalid key");
        }
    }
    
    private void ensureValidValue(Storeable value) {
        if (value == null) {
            throw new IllegalArgumentException("Invalid value");
        }
        if (!StoreableUtils.validate(value, columnTypes)) {
            throw new ColumnFormatException();
        }
    }
}
