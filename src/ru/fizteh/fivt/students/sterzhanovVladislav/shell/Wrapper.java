package ru.fizteh.fivt.students.sterzhanovVladislav.shell;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;

public class Wrapper {
    private static HashMap<String, Command> cmdMap = new HashMap<String, Command>();
    
    static {
        cmdMap.put("cd", new ShellCommands.Cd());
        cmdMap.put("mkdir", new ShellCommands.Mkdir());
        cmdMap.put("pwd", new ShellCommands.Pwd());
        cmdMap.put("rm", new ShellCommands.Rm());
        cmdMap.put("cp", new ShellCommands.Cp());
        cmdMap.put("mv", new ShellCommands.Mv());
        cmdMap.put("dir", new ShellCommands.Dir());
        cmdMap.put("exit", new ShellCommands.Exit());
    }
    
    public static void main(String[] args) {
        boolean isInteractiveMode = true;
        InputStream cmdStream = System.in;
        PrintStream errorStream = System.err;
        if (args.length > 0) {
            cmdStream = ShellUtility.createStream(args);
            errorStream = System.out;
            isInteractiveMode = false;
        }
        ShellUtility.execShell(cmdMap, cmdStream, System.out, errorStream, isInteractiveMode, isInteractiveMode);
        System.exit(0);
    }
}
