package ru.fizteh.fivt.students.sterzhanovVladislav.fileMap;

import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.storeable.StoreableRow;
import ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.storeable.StoreableUtils;
import ru.fizteh.fivt.students.sterzhanovVladislav.shell.ShellUtility;

public class FileMapProvider implements AtomicTableProvider {

    public static final String SIGNATURE_FILE_NAME = "signature.tsv";

    private Path rootDir;
    private HashMap<String, FileMap> tables;
    private volatile boolean isClosed = false;
    
    private Lock lock = new ReentrantLock();
    
    @Override
    public FileMap getTable(String name) {
        ensureIsOpen();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Path dbPath = Paths.get(rootDir.normalize() + "/" + name);
        if (dbPath == null || !isValidFileName(name)) {
            throw new IllegalArgumentException("Invalid path");
        }
        FileMap table;
        lock.lock();
        try {
            table = tables.get(name);
            if (table == null) {
                table = IOUtility.parseDatabase(dbPath);
                table.setProvider(this);
                tables.put(name, table);
            }
            return table; 
        } catch (IOException e) {
            return null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public FileMap createTable(String name, List<Class<?>> columnTypes) throws IOException {
        ensureIsOpen();
        if (name == null || name.isEmpty() || columnTypes == null || columnTypes.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Path dbPath = Paths.get(rootDir.normalize() + "/" + name);
        if (dbPath == null || !isValidFileName(name)) {
            throw new IllegalArgumentException("Invalid path");
        }
        lock.lock();
        try {
            if (dbPath.toFile().exists()) {
                return null;
            }
            FileMap newFileMap = new FileMap(name, columnTypes);
            newFileMap.setProvider(this);
            createFileStructure(dbPath, columnTypes);
            tables.put(name, newFileMap);
            return newFileMap;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removeTable(String name) {
        ensureIsOpen();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException();
        }
        lock.lock();
        try {
            FileMap removedTable = tables.remove(name);
            Path dbPath = Paths.get(getRootDir() + "/" + name);
            if (dbPath == null) {
                throw new IllegalArgumentException("Invalid database path");
            }
            ShellUtility.removeDir(dbPath);
            if (removedTable != null) {
                removedTable.destroy();
            }
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        } finally {
            lock.unlock();
        }
    }
    
    public String getRootDir() {
        return rootDir.toString();
    }

    public FileMapProvider(String root) throws IllegalArgumentException, IOException {
        if (root == null || root.isEmpty()) {
            throw new IllegalArgumentException();
        }
        rootDir = Paths.get(root);
        if (rootDir == null) {
            throw new IllegalArgumentException("Error: RootDir can not be null");
        }
        if (!rootDir.toFile().exists()) {
            if (!rootDir.toFile().mkdir()) {
                throw new IOException("Error: Root directory does not exist, unable to create root directory");
            }
        }
        if (!rootDir.toFile().isDirectory()) {
            throw new IllegalArgumentException("Error: Root did not resolve to a valid directory");
        }
        tables = new HashMap<String, FileMap>();
    }
    
    private static void createFileStructure(Path dbPath, List<Class<?>> columnTypes) throws IOException {
        if (!dbPath.toFile().mkdir()) {
            throw new IOException("Unable to create directory");
        }
        IOUtility.writeSignature(dbPath, columnTypes);
    }
    
    private static boolean isValidFileName(String name) {
        return !(name.contains("\\") || name.contains("/")
                || name.contains(":") || name.contains("*")
                || name.contains("?") || name.contains("\"")
                || name.contains("<") || name.contains(">")
                || name.contains("\n") || name.contains(" ")
                || name.contains("|") || name.contains("\t"));
    }

    @Override
    public Storeable deserialize(Table table, String value)
            throws ParseException {
        ensureIsOpen();
        return StoreableUtils.deserialize(value, StoreableUtils.generateSignature(table));
    }

    @Override
    public String serialize(Table table, Storeable value)
            throws ColumnFormatException {
        ensureIsOpen();
        if (!StoreableUtils.validate(value, StoreableUtils.generateSignature(table))) {
            throw new ColumnFormatException("wrong type (can't serialize value according to signature)");
        }
        return StoreableUtils.serialize(value, StoreableUtils.generateSignature(table));
    }

    @Override
    public Storeable createFor(Table table) {
        ensureIsOpen();
        return new StoreableRow(StoreableUtils.generateSignature(table));
    }

    @Override
    public Storeable createFor(Table table, List<?> values)
            throws ColumnFormatException, IndexOutOfBoundsException {
        ensureIsOpen();
        return new StoreableRow(StoreableUtils.generateSignature(table), values);
    }

    @Override
    public void closeTableIfNotModified(String name) throws IllegalStateException, IOException {
        ensureIsOpen();
        FileMap table = tables.get(name);
        if (table == null) {
            throw new IllegalStateException(name + " not exists");
        }
        int currentDiffSize = table.getDiffSize();
        if (table != null && currentDiffSize > 0) {
            throw new IllegalStateException(currentDiffSize + " unsaved changes");
        }
    }
    @Override
    public void close() throws Exception {
        isClosed = true;
        for (FileMap table : tables.values()) {
            table.close();
        }
    }

    public void resetTable(String tableName) {
        tables.remove(tableName);
    }

    private void ensureIsOpen() {
        if (isClosed) {
            throw new IllegalStateException("TableProvider was closed");
        }
    }
   
}
