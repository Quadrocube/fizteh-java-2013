package ru.fizteh.fivt.students.sterzhanovVladislav.shell;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandParsers {

    public static class FileMapPutCommandParser extends CommandParser {
        public String[] parseArgs(String cmdLine) {
            ArrayList<String> args = new ArrayList<String>();
            Matcher nextToken = Pattern.compile("[\t ]*([^\t ]+)").matcher(cmdLine);
            int argsParsed = 0;
            while (nextToken.find() && argsParsed < 3) {
                String token = nextToken.group(1);
                ++argsParsed;
                if (argsParsed == 3) {
                    token += cmdLine.substring(nextToken.end());
                }
                args.add(token);
            }
            return args.toArray(new String[0]);
        }
    }

    public static class DefaultCommandParser extends CommandParser {
        public String[] parseArgs(String cmdLine) {
            return cmdLine.trim().split("[\t ]+");
        }
    }

}
