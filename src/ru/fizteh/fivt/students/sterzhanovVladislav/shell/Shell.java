package ru.fizteh.fivt.students.sterzhanovVladislav.shell;

import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Shell {
    public PrintStream out = System.out;
    public PrintStream err = System.err;
    private Path workingDir;
    
    private HashMap<String, Command> cmdMap;
    
    public void execCommandStream(InputStream cmdStream, PrintStream output, PrintStream error,
            boolean isInteractiveMode, boolean doPrintPrompt) throws Exception {
        out = output;
        err = error;
        Scanner cmdReader = new Scanner(cmdStream);
        try {
            maybePrintPrompt(doPrintPrompt);
            while (cmdReader.hasNextLine()) {
                try {
                    String[] cmdList = cmdReader.nextLine().split(";");
                    for (String cmdString : cmdList) {
                        Command cmd = parseNextCommand(cmdString);
                        if (cmd != null) {
                            cmd.execute(cmd.getParser().parseArgs(cmdString));
                        }
                    }
                } catch (Exception e) {
                    if (isInteractiveMode) {
                        out.println(e.getMessage());
                    } else {
                        throw e;
                    }
                }
                maybePrintPrompt(doPrintPrompt);
            }
        } finally {
            cmdReader.close();
        }
    }
    
    public void exit(int exitCode) {
        System.exit(exitCode);
    }
    
    public Path getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(Path newWorkingDir) {
        workingDir = newWorkingDir.normalize();
    }

    public Path getAbsolutePath(Path path) {
        return getWorkingDir().resolve(path).normalize();
    }

    public static boolean isSubdirectory(Path dirToCheck, Path relativeTo) {
        dirToCheck = dirToCheck.normalize().getParent();
        relativeTo = relativeTo.normalize();
        while (!dirToCheck.equals(dirToCheck.getRoot())) {
            if (dirToCheck.equals(relativeTo)) {
                return true;
            }
            dirToCheck = dirToCheck.getParent();
        }
        return false;
    }
    
    private Command parseNextCommand(String cmdLine) {
        if (cmdLine.trim().isEmpty()) {
            return null;
        }
        Matcher cmdNameMatcher = Pattern.compile("[\t ]*([^\t ]+)[\t ]*").matcher(cmdLine);
        String cmdName = null;
        if (cmdNameMatcher.find()) {
            cmdName = cmdNameMatcher.group(1);
        }
        if (cmdName == null || !cmdMap.containsKey(cmdName)) {
            throw new IllegalArgumentException("Illegal command");
        }
        Command cmd = cmdMap.get(cmdName);
        return cmd;
    }
    
    private void maybePrintPrompt(boolean doPrintPrompt) {
        if (doPrintPrompt) {
            out.print("$ ");
        }
    }
    
    public Shell(HashMap<String, Command> commandMap) {
        cmdMap = commandMap;
        for (Command cmd : cmdMap.values()) {
            cmd.setShell(this);
        }
        workingDir = Paths.get(System.getProperty("user.dir"));
    }
}
