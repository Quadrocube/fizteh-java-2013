package ru.fizteh.fivt.students.sterzhanovVladislav.shell;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.util.HashMap;
import java.nio.file.attribute.BasicFileAttributes;

import ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.DatabaseContext;
import ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.FileMapCommands;
import ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.network.TelnetCommands;
import ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.network.TelnetServerContext;

public class ShellUtility {
    public static InputStream createStream(String[] args) {
        StringBuilder argline = new StringBuilder();
        for (String arg : args) {
            argline.append(arg).append(" ");
        }
        String cmdLine = argline.toString();
        return new ByteArrayInputStream(cmdLine.getBytes(Charset.defaultCharset()));
    }
    
    public static void execShell(HashMap<String, Command> cmdMap, InputStream in, PrintStream out, PrintStream err, 
            boolean isInteractiveMode, boolean doPrintPrompt) {
        try {
            Shell cmdShell = new Shell(cmdMap);
            cmdShell.execCommandStream(in, out, err, isInteractiveMode, doPrintPrompt);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }
    
    public static HashMap<String, Command> initCmdMap(DatabaseContext dbContext, TelnetServerContext serverContext) {
        HashMap<String, Command> cmdMap = new HashMap<String, Command>();
        cmdMap.put("put", new FileMapCommands.Put().setContext(dbContext));
        cmdMap.put("get", new FileMapCommands.Get().setContext(dbContext));
        cmdMap.put("remove", new FileMapCommands.Remove().setContext(dbContext));
        cmdMap.put("create", new FileMapCommands.Create().setContext(dbContext));
        cmdMap.put("drop", new FileMapCommands.Drop().setContext(dbContext));
        cmdMap.put("use", new FileMapCommands.Use().setContext(dbContext));
        cmdMap.put("exit", new FileMapCommands.Exit().setContext(dbContext));
        cmdMap.put("commit", new FileMapCommands.Commit().setContext(dbContext));
        cmdMap.put("rollback", new FileMapCommands.Rollback().setContext(dbContext));
        cmdMap.put("size", new FileMapCommands.Size().setContext(dbContext));
        cmdMap.put("describe", new FileMapCommands.Describe().setContext(dbContext));
        cmdMap.put("connect", new FileMapCommands.Connect().setContext(dbContext));
        cmdMap.put("disconnect", new FileMapCommands.Disconnect().setContext(dbContext));
        cmdMap.put("whereami", new FileMapCommands.Whereami().setContext(dbContext));
        cmdMap.put("start", new TelnetCommands.Start().setContext(serverContext));
        cmdMap.put("stop", new TelnetCommands.Stop().setContext(serverContext));
        cmdMap.put("listusers", new TelnetCommands.ListUsers().setContext(serverContext));
        return cmdMap;
    }
    
    public static void removeDir(Path path) throws IOException {
        if (!path.toFile().exists() || !path.toFile().isDirectory()) {
            throw new IllegalStateException(path.toFile().getName() + " not exists");
        }
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }
                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                        if (e == null) {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        } else {
                            throw e;
                        }
                    }
                });
    }
}
