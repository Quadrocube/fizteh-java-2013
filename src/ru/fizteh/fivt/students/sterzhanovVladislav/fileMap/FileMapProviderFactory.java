package ru.fizteh.fivt.students.sterzhanovVladislav.fileMap;

import java.io.IOException;

public class FileMapProviderFactory implements AtomicTableProviderFactory {

    @Override
    public AtomicTableProvider create(String dir) throws IOException {
        return new FileMapProvider(dir);
    }

}
