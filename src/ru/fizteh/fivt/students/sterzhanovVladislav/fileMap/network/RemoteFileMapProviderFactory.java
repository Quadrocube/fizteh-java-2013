package ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.network;

import java.io.IOException;
import java.util.HashSet;

import ru.fizteh.fivt.storage.structured.RemoteTableProvider;
import ru.fizteh.fivt.storage.structured.RemoteTableProviderFactory;

public class RemoteFileMapProviderFactory implements RemoteTableProviderFactory, AutoCloseable {

    private boolean isClosed = false;
    private HashSet<RemoteFileMapProvider> providers;
    
    @Override
    public RemoteTableProvider connect(String hostname, int port) throws IOException {
        ensureIsOpen();
        RemoteFileMapProvider provider = new RemoteFileMapProvider(hostname, port);
        providers.add(provider);
        return provider;
    }
    
    @Override
    public void close() throws Exception {
        isClosed = true;
        for (RemoteFileMapProvider provider : providers) {
            provider.close();
        }
    }
    
    private void ensureIsOpen() {
        if (isClosed) {
            throw new IllegalStateException("FileMapProviderFactory was closed");
        }
    }

}
