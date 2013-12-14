package ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

import ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.FileMapProvider;

public class TelnetServerContext implements AutoCloseable {
    
    Thread serverThread = null;
    Server server = null;
    final FileMapProvider provider;
    final TelnetServerContext serverContext;
    
    public void start(int portNumber) throws IllegalStateException, IOException {
        if (isRunning()) {
            throw new IllegalStateException("already started");
        }
        ServerSocket serverSocket = new ServerSocket(portNumber);
        server = new Server(serverSocket, provider, serverContext);
        serverThread = new Thread(server);
        serverThread.start();
    }
    
    public int stop() throws IllegalStateException, InterruptedException {
        if (!isRunning()) {
            throw new IllegalStateException("not started");
        }
        int oldPort = server.getPort();
        serverThread.interrupt();
        try {
            serverThread.join();
        } finally {
            server = null;
            serverThread = null;
        }
        return oldPort;
    }

    public List<String> getActiveUsers() {
        if (!isRunning()) {
            throw new IllegalStateException("not started");
        }
        return server.listUsers();
    }
    
    public boolean isRunning() {
        return serverThread != null && serverThread.isAlive();
    }
    
    public void close() throws IllegalStateException, InterruptedException {
        if (isRunning()) {
            stop();
        }
    }
    
    public TelnetServerContext(FileMapProvider provider) {
        this.provider = provider;
        this.serverContext = this;
    }
}
