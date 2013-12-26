package ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.network;

import java.util.List;

public class TelnetCommands {
    public static class Start extends TelnetCommand {
        @Override
        public void innerExecute(String[] args) throws Exception {
            if (args.length < 1 || args.length > 2) {
                throw new IllegalArgumentException(args[0] + " [port]");
            }
            int port = -1;
            try {
                port = args.length == 1 ? 10001 : Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                throw new RuntimeException("not started: wrong port number");
            }
            try {
                context.start(port);
            } catch (Exception e) {
                throw new IllegalStateException("not started: " + e.getMessage());
            }
            parentShell.out.println("started at " + port);
        }
        
        public Start() {
            super(-1);
        }
    }

    public static class Stop extends TelnetCommand {
        @Override
        public void innerExecute(String[] args) throws Exception {
            int oldPort = context.stop();
            parentShell.out.println("stopped at " + oldPort);
        }
        
        public Stop() {
            super(1);
        }
    }

    public static class ListUsers extends TelnetCommand {
        @Override
        public void innerExecute(String[] args) throws Exception {
            List<String> userList = context.getActiveUsers();
            for (String user : userList) {
                parentShell.out.println(user);
            }
        }
        
        public ListUsers() {
            super(1);
        }
    }
}
