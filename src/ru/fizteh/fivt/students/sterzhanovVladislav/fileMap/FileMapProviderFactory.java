package ru.fizteh.fivt.students.sterzhanovVladislav.fileMap;

import java.io.IOException;
import java.util.HashSet;

import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.storage.structured.TableProviderFactory;

public class FileMapProviderFactory implements TableProviderFactory, AutoCloseable {

    private boolean isClosed = false;
    private HashSet<FileMapProvider> providers;
    
    @Override
    public TableProvider create(String dir) throws IOException {
        ensureIsOpen();
        FileMapProvider provider = new FileMapProvider(dir);
        providers.add(provider);
        return provider;
    }
    
    @Override
    public void close() throws Exception {
        isClosed = true;
        for (FileMapProvider provider : providers) {
            provider.close();
        }
    }
    
    private void ensureIsOpen() {
        if (isClosed) {
            throw new IllegalStateException("FileMapProviderFactory was closed");
        }
    }
    
}
