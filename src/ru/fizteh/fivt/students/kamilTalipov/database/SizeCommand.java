package ru.fizteh.fivt.students.kamilTalipov.database;

import ru.fizteh.fivt.students.kamilTalipov.shell.Shell;
import ru.fizteh.fivt.students.kamilTalipov.shell.SimpleCommand;

public class SizeCommand extends SimpleCommand {
    public SizeCommand(TransactionDatabase database) {
        super("size", 0);
        this.database = database;
    }

    @Override
    public void run(Shell shell, String[] args) throws IllegalArgumentException {
        if (numberOfArguments != args.length) {
            throw new IllegalArgumentException(name + ": expected " + numberOfArguments
                    + " but " + args.length + " got");
        }

        try {
            System.out.println(database.size());
        } catch (NoTableSelectedException e) {
            System.err.println("no table");
        }
    }

    private final TransactionDatabase database;
}
