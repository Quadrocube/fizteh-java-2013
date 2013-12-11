package ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.network;

import ru.fizteh.fivt.students.sterzhanovVladislav.shell.Command;
import ru.fizteh.fivt.students.sterzhanovVladislav.shell.CommandParser;
import ru.fizteh.fivt.students.sterzhanovVladislav.shell.CommandParsers;

public abstract class TelnetCommand extends Command {
    TelnetServerContext context;
    
    public TelnetCommand setContext(TelnetServerContext context) {
        this.context = context;
        return this;
    }
    
    TelnetCommand(int argCount) {
        super(argCount);
    }
    
    public CommandParser getParser() {
        return new CommandParsers.DefaultCommandParser();
    }
}
