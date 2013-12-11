package ru.fizteh.fivt.students.sterzhanovVladislav.fileMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.storeable.StoreableUtils;
import ru.fizteh.fivt.students.sterzhanovVladislav.shell.ShellUtility;

public class IOUtility {
    private static final int MAX_KEY_SIZE = 1 << 24;
    private static final int MAX_VALUE_SIZE = 1 << 24;

    public static FileMap parseDatabase(Path dbDir) 
            throws IllegalStateException, IOException {
        HashMap<String, Storeable> map = new HashMap<String, Storeable>();
        List<Class<?>> dbSignature = parseSignature(dbDir);
        for (File subdir : dbDir.toFile().listFiles()) {
            if (subdir.getName().equals(FileMapProvider.SIGNATURE_FILE_NAME)) {
                continue;
            }
            if (!subdir.isDirectory() || !subdir.getName().matches("^([0-9]|[1][0-5])\\.dir$")) {
                throw new IllegalStateException("Error: Malformed database");
            }
            File[] listFiles = subdir.listFiles();
            if (listFiles.length == 0) {
                throw new IllegalStateException("Error: Malformed database");
            }
            for (File file : listFiles) {
                if (!file.isFile() || file.length() == 0) {
                    throw new IllegalStateException("Error: Malformed database");
                }
                if (!file.getName().matches("(^([0-9]|[1][0-5])\\.dat$)|")) {
                    throw new IllegalStateException("Error: Malformed database");
                }
                int dirID = Integer.parseInt(subdir.getName().replaceAll("\\.dir", ""));
                int fileID = Integer.parseInt(file.getName().replaceAll("\\.dat", ""));
                parseFileIntoDB(file, map, dirID, fileID, dbSignature);
            }
        }
        return new FileMap(dbDir.toFile().getName(), map, dbSignature);
    }

    public static void parseFileIntoDB(File dbFile, HashMap<String, Storeable> map, int checkDirID, int checkFileID,
            List<Class<?>> signature) 
            throws FileNotFoundException, IOException {
        try (FileInputStream fstream = new FileInputStream(dbFile)) {
            while (fstream.available() > 0) {
                Map.Entry<String, Storeable> newEntry = parseEntry(fstream, checkDirID, checkFileID, signature);
                map.put(newEntry.getKey(), newEntry.getValue());
            }
        }
    }

    public static Map.Entry<String, Storeable> parseEntry(FileInputStream fstream, int checkDirID, int checkFileID,
            List<Class<?>> signature) 
            throws IllegalStateException, IOException, UnsupportedEncodingException {
        byte[] sizeBuf = new byte[4];
        safeRead(fstream, sizeBuf, 4);
        int keySize = ByteBuffer.wrap(sizeBuf).getInt();
        safeRead(fstream, sizeBuf, 4);
        int valueSize = ByteBuffer.wrap(sizeBuf).getInt();
        if (keySize <= 0 || valueSize <= 0 || keySize > MAX_KEY_SIZE || valueSize > MAX_VALUE_SIZE) {
            throw new IllegalStateException("Error: malformed database");
        }
        byte[] keyBuf = new byte[keySize];
        safeRead(fstream, keyBuf, keySize);
        byte[] valueBuf = new byte[valueSize];
        safeRead(fstream, valueBuf, valueSize);
        int b = keyBuf[0];
        if (b < 0) {
            b *= -1;
        }
        int directoryID = b % 16;
        int fileID = b / 16 % 16;
        if (directoryID != checkDirID || fileID != checkFileID) {
            throw new IllegalStateException("Error: malformed database");
        }
        Storeable value;
        try {
            value = StoreableUtils.deserialize(new String(valueBuf, StandardCharsets.UTF_8), signature);
        } catch (ParseException e) {
            throw new IllegalStateException("Error: malformed database");
        }
        return new AbstractMap.SimpleEntry<String, Storeable>(new String(keyBuf, StandardCharsets.UTF_8), value);
    }
    
    public static void writeDiff(Set<Integer> modifiedFiles, MultiHashMap dataBase, 
            Path path, List<Class<?>> classes) throws IOException {
        if (!path.toFile().exists() || !path.toFile().isDirectory()) {
            throw new IOException("Error: bad directory path");
        }
        for (int fileHash : modifiedFiles) {
            int dirId = fileHash % 16;
            int fileId = fileHash / 16 % 16;
            HashMap<String, Storeable> db = dataBase.dbArray[dirId][fileId];
            File subdir = Paths.get(path.normalize() + "/" + dirId + ".dir").toFile();
            File file = Paths.get(path.normalize() + "/" + dirId + ".dir/" + fileId + ".dat").toFile();
            if (db.isEmpty()) {
                if (subdir.exists() && subdir.listFiles().length == 0) {
                    ShellUtility.removeDir(subdir.toPath());
                } else if (file.exists()) {
                    if (!file.delete()) {
                        throw new IOException("Unable to delete file");
                    }
                }
                continue;
            }
            if (!subdir.exists()) {
                subdir.mkdir();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            try (FileOutputStream fstream = new FileOutputStream(file, true)) {
                IOUtility.writeFile(db, fstream, classes);
            }
        } 
    }

    public static void writeFile(HashMap<String, Storeable> db, FileOutputStream fstream, List<Class<?>> signature) 
            throws IOException {
        for (Map.Entry<String, Storeable> entry : db.entrySet()) {
            byte[] keyBuf = entry.getKey().getBytes(StandardCharsets.UTF_8);
            byte[] valueBuf = StoreableUtils.serialize(entry.getValue(), signature).getBytes(StandardCharsets.UTF_8);
            fstream.write(ByteBuffer.allocate(4).putInt(keyBuf.length).array());
            fstream.write(ByteBuffer.allocate(4).putInt(valueBuf.length).array());
            fstream.write(keyBuf);
            fstream.write(valueBuf);
        }
    }

    public static void safeRead(FileInputStream fstream, byte[] buf, int readCount) 
            throws IllegalStateException, IOException {
        int bytesRead = 0;
        if (readCount < 0) {
            throw new IllegalStateException("Error: malformed database");
        }
        while (bytesRead < readCount) {
            int readNow = fstream.read(buf, bytesRead, readCount - bytesRead);
            if (readNow < 0) {
                throw new IllegalStateException("Error: malformed database");
            }
            bytesRead += readNow;
        }
    }
    
    private static List<Class<?>> parseSignature(Path rootDir) throws IOException {
        if (!rootDir.toFile().exists() || !rootDir.toFile().isDirectory()) {
            throw new IOException("Error: Directory does not exist");
        }
        Path signaturePath = Paths.get(rootDir.toString() + "/" + FileMapProvider.SIGNATURE_FILE_NAME);
        if (signaturePath == null) {
            throw new IOException("Error: signature does not exist");
        }
        List<Class<?>> signature = new ArrayList<Class<?>>();
        try {
            byte[] buf = Files.readAllBytes(signaturePath);
            String typeNamesList = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(buf)).toString();
            signature = parseSignatureFromString(typeNamesList);
            
        } catch (IOException e) {
            throw new IllegalStateException("Error: malformed database");
        }
        return signature;
    }
    
    public static List<Class<?>> parseSignatureFromString(String typeNamesList) {
        List<Class<?>> signature = new ArrayList<Class<?>>();
        typeNamesList = typeNamesList.replaceAll("\n", " ").trim();
        for (String typeName : typeNamesList.split(" +")) {
            typeName = typeName.replaceAll("\n", " ");
            if (!StoreableUtils.TYPENAMES.containsKey(typeName)) {
                if (typeName.isEmpty()) {
                    typeName = "[empty]";
                }
                throw new ColumnFormatException("wrong type (" + typeName + " unknown)");
            }
            signature.add(StoreableUtils.TYPENAMES.get(typeName));
        }
        return signature;
    }
    
    public static void writeSignature(Path rootDir, List<Class<?>> signature) throws IOException {
        if (!rootDir.toFile().exists() || !rootDir.toFile().isDirectory()) {
            throw new IOException("Error: Directory does not exist");
        }
        Path signaturePath = Paths.get(rootDir.toString() + "/" + FileMapProvider.SIGNATURE_FILE_NAME);
        if (signaturePath == null) {
            throw new IllegalStateException("Error: malformed database");
        }
        try (FileOutputStream fstream = new FileOutputStream(signaturePath.toFile())) {
            for (int typeID = 0; typeID < signature.size(); ++typeID) {
                String typeName = StoreableUtils.CLASSES.get(signature.get(typeID));
                byte[] typeNameBuf = (typeName + ((typeID < signature.size() - 1) ? " " : ""))
                        .getBytes(StandardCharsets.UTF_8);
                fstream.write(typeNameBuf);
            }
        } catch (IOException e) {
            throw e;
        }
    }
}
