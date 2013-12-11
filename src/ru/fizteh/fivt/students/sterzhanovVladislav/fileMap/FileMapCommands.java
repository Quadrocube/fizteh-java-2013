package ru.fizteh.fivt.students.sterzhanovVladislav.fileMap;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.fizteh.fivt.storage.structured.RemoteTableProvider;
import ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.network.RemoteFileMapProvider;
import ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.storeable.StoreableUtils;
import ru.fizteh.fivt.students.sterzhanovVladislav.shell.CommandParser;
import ru.fizteh.fivt.students.sterzhanovVladislav.shell.CommandParsers;

public class FileMapCommands {
    public static class Put extends FileMapCommand {
        @Override
        public void innerExecute(String[] args) throws Exception {
            String oldValue = dbContext.put(args[1], args[2]);
            if (oldValue == null) {
                parentShell.out.println("new");
            } else {
                parentShell.out.println("overwrite\n" + oldValue);
            }
        }
        
        @Override
        public CommandParser getParser() {
            return new CommandParsers.FileMapPutCommandParser();
        }
        
        public Put() {
            super(3);
        }
    }
    
    public static class Get extends FileMapCommand {
        @Override
        public void innerExecute(String[] args) throws Exception {
            String value = dbContext.get(args[1]);
            if (value == null) {
                parentShell.out.println("not found");
            } else {
                parentShell.out.println("found\n" + value);
            }
        }
        
        @Override
        public CommandParser getParser() {
            return new CommandParsers.DefaultCommandParser();
        }

        public Get() {
            super(2);
        }
    }
    
    public static class Remove extends FileMapCommand {
        @Override
        public void innerExecute(String[] args) throws Exception {
            String value = dbContext.remove(args[1]);
            if (value == null) {
                parentShell.out.println("not found");
            } else {
                parentShell.out.println("removed");
            }
        }
        
        @Override
        public CommandParser getParser() {
            return new CommandParsers.DefaultCommandParser();
        }
        
        public Remove() {
            super(2);
        }
    }
    
    public static class Use extends FileMapCommand {
        @Override
        public void innerExecute(String[] args) throws Exception {
            dbContext.loadTable(args[1]);
            parentShell.out.println("using " + args[1]);
        }
        
        @Override
        public CommandParser getParser() {
            return new CommandParsers.DefaultCommandParser();
        }
        
        public Use() {
            super(2);
        }
    }
    
    public static class Create extends FileMapCommand {
        @Override
        public void innerExecute(String[] args) throws Exception {
            if (args.length < 3) {
                throw new IllegalArgumentException("wrong type (Not enough arguments)");
            }
            List<Class<?>> typeList = new ArrayList<Class<?>>();
            for (int i = 2; i < args.length; ++i) {
                String className = args[i];
                if (i == 2) {
                    Matcher firstTypeMatcher = Pattern.compile("^\\s*\\(\\s*(.+)\\s*").matcher(className);
                    if (firstTypeMatcher.find()) {
                        className = firstTypeMatcher.group(1);
                    } else {
                        throw new IllegalArgumentException("wrong type (Wrong command, expected 'create name (args)')");
                    }
                }
                if (i == args.length - 1) {
                    Matcher lastTypeMatcher = Pattern.compile("\\s*(.+)\\s*\\)\\s*").matcher(className);
                    if (lastTypeMatcher.find()) {
                        className = lastTypeMatcher.group(1);
                    } else {
                        throw new IllegalArgumentException("wrong type (Wrong command, expected 'create name (args)')");
                    }
                }
                Class<?> type = StoreableUtils.resolveClass(className);
                if (type == null) {
                    throw new IllegalArgumentException("wrong type (Illegal class given: " + className + ")");
                }
                typeList.add(type);
            }
            dbContext.createTable(args[1], typeList);
            parentShell.out.println("created");
        }
        
        @Override
        public CommandParser getParser() {
            return new CommandParsers.DefaultCommandParser();
        }
        
        public Create() {
            super(-1);
        }
    }
    
    public static class Drop extends FileMapCommand {
        @Override
        public void innerExecute(String[] args) throws IllegalStateException, IOException {
            dbContext.removeTable(args[1]);
            parentShell.out.println("dropped");
        }
        
        @Override
        public CommandParser getParser() {
            return new CommandParsers.DefaultCommandParser();
        }
        
        public Drop() {
            super(2);
        }
    }
    
    public static class Exit extends FileMapCommand {
        @Override
        public void innerExecute(String[] args) throws Exception {
            dbContext.close();
            dbContext.exit();
        }
        
        @Override
        public CommandParser getParser() {
            return new CommandParsers.DefaultCommandParser();
        }
        
        public Exit() {
            super(1);
        }
    }
    
    public static class Commit extends FileMapCommand {
        public Commit() {
            super(1);
        }

        @Override
        public CommandParser getParser() {
            return new CommandParsers.DefaultCommandParser();
        }

        @Override
        public void innerExecute(String[] args) throws Exception, IOException {
            int numChanges = dbContext.commit();
            parentShell.out.println(numChanges);
        }
    }
    
    public static class Rollback extends FileMapCommand { 
        public Rollback() {
            super(1);
        }

        @Override
        public CommandParser getParser() {
            return new CommandParsers.DefaultCommandParser();
        }

        @Override
        public void innerExecute(String[] args) throws Exception, IOException {
            int numChanges = dbContext.rollback();
            parentShell.out.println(numChanges);
        }
    }
    
    public static class Size extends FileMapCommand {
        public Size() {
            super(1);
        }

        @Override
        public CommandParser getParser() {
            return new CommandParsers.DefaultCommandParser();
        }

        @Override
        public void innerExecute(String[] args) throws Exception, IOException {
            parentShell.out.println(dbContext.getActiveSize());
        }
    }
    
    public static class Connect extends FileMapCommand {
        public Connect() {
            super(-1);
        }

        @Override
        public CommandParser getParser() {
            return new CommandParsers.DefaultCommandParser();
        }

        @Override
        public void innerExecute(String[] args) throws Exception, IOException {
            if (args.length < 2 || args.length > 3) {
                throw new IllegalArgumentException("connect host [port]");
            }
            String hostname = args[1];
            int port = 10001;
            if (args.length == 3) {
                port = Integer.parseInt(args[2]);
            }
            dbContext.attachRemoteProvider(new RemoteFileMapProvider(hostname, port));
            parentShell.out.println("connected");
        }
    }    

    public static class Disconnect extends FileMapCommand {
        public Disconnect() {
            super(1);
        }

        @Override
        public CommandParser getParser() {
            return new CommandParsers.DefaultCommandParser();
        }

        @Override
        public void innerExecute(String[] args) throws Exception, IOException {
            dbContext.disconnectRemoteProvider();
            parentShell.out.println("disconnected");
        }
    }    

    public static class Whereami extends FileMapCommand {
        public Whereami() {
            super(1);
        }

        @Override
        public CommandParser getParser() {
            return new CommandParsers.DefaultCommandParser();
        }

        @Override
        public void innerExecute(String[] args) throws Exception, IOException {
            RemoteTableProvider remote = dbContext.getRemoteProvider();
            if (remote != null) {
                parentShell.out.println("remote " + remote);
            } else {
                parentShell.out.println("local");
            }
        }
    }
    
    public static class Describe extends FileMapCommand {
        public Describe() {
            super(1);
        }

        @Override
        public CommandParser getParser() {
            return new CommandParsers.DefaultCommandParser();
        }

        @Override
        public void innerExecute(String[] args) throws Exception, IOException {
            parentShell.out.println(dbContext.describe());
        }
    }
}
