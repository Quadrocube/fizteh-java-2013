package ru.fizteh.fivt.students.sterzhanovVladislav.fileMap;

import ru.fizteh.fivt.students.sterzhanovVladislav.shell.Command;

public abstract class FileMapCommand extends Command {
    DatabaseContext dbContext;
    
    public FileMapCommand setContext(DatabaseContext context) {
        dbContext = context;
        return this;
    }
    
    FileMapCommand(int argCount) {
        super(argCount);
    }
}
