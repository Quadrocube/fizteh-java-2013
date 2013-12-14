package ru.fizteh.fivt.students.sterzhanovVladislav.fileMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;

import ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.network.TelnetServerContext;
import ru.fizteh.fivt.students.sterzhanovVladislav.shell.Command;
import ru.fizteh.fivt.students.sterzhanovVladislav.shell.ShellUtility;

public class Wrapper {
    
    public static void main(String[] args) {
        String dbDir = System.getProperty("fizteh.db.dir");
        if (dbDir == null) {
            System.out.println("fizteh.db.dir not set");
            System.exit(-1);
        }
        FileMapProvider provider = null;
        try {
            provider = new FileMapProvider(dbDir);
        } catch (IllegalArgumentException | IOException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
        try (final DatabaseContext dbContext = new DatabaseContext(provider);
                final TelnetServerContext serverContext = new TelnetServerContext(provider)) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        dbContext.close();
                    } catch (IOException e) {
                        // Ignore
                    }
                    try {
                        serverContext.close();
                    } catch (IllegalStateException | InterruptedException e) {
                        // Ignore
                    }
                }
            });
            HashMap<String, Command> cmdMap = ShellUtility.initCmdMap(dbContext, serverContext);
            boolean isInteractiveMode = true;
            InputStream cmdStream = System.in;
            PrintStream errorStream = System.err;
            if (args.length > 0) {
                cmdStream = ShellUtility.createStream(args);
                errorStream = System.out;
                isInteractiveMode = false;
            }
            ShellUtility.execShell(cmdMap, cmdStream, System.out, errorStream, isInteractiveMode, isInteractiveMode);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
        System.exit(0);
    }
}
