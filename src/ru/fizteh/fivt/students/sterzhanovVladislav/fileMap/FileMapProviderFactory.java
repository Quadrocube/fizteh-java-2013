package ru.fizteh.fivt.students.sterzhanovVladislav.fileMap;

import java.io.IOException;

import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.storage.structured.TableProviderFactory;

public class FileMapProviderFactory implements TableProviderFactory {

    @Override
    public TableProvider create(String dir) throws IOException {
        return new FileMapProvider(dir);
    }

}
