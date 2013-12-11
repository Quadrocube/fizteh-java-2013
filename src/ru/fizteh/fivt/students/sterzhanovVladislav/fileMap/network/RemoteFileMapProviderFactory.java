package ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.network;

import java.io.IOException;

import ru.fizteh.fivt.storage.structured.RemoteTableProvider;
import ru.fizteh.fivt.storage.structured.RemoteTableProviderFactory;

public class RemoteFileMapProviderFactory implements RemoteTableProviderFactory {

    @Override
    public RemoteTableProvider connect(String hostname, int port)
            throws IOException {
        return new RemoteFileMapProvider(hostname, port);
    }

}
