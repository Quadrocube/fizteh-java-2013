package ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.DatabaseContext;
import ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.FileMapProvider;
import ru.fizteh.fivt.students.sterzhanovVladislav.shell.ShellUtility;
import ru.fizteh.fivt.students.sterzhanovVladislav.shell.Command;

class Server implements Runnable {

    final ServerSocket socket;
    final FileMapProvider provider;
    final TelnetServerContext serverContext;
    private Set<String> activeUsers = new HashSet<String>();

    @Override
    public void run() {
        Socket client = null;
        while (!Thread.interrupted()) {
            try {
                client = socket.accept();
            } catch (SocketTimeoutException e) {
                continue;
            } catch (IOException e) {
                return;
            }
            Thread serveThread = new Thread(new ClientConnection(client));
            serveThread.start();
        }
        try {
            socket.close();
        } catch (IOException e) {
            // Ignore
        }
    }
    
    List<String> listUsers() {
        List<String> userList = new ArrayList<String>();
        synchronized (activeUsers) {
            for (String user: activeUsers) {
                userList.add(user);
            }
        }
        return userList;
    }
    
    int getPort() {
        return socket.getLocalPort();
    }
    
    Server(ServerSocket socket, FileMapProvider provider, TelnetServerContext serverContext) throws SocketException {
        this.socket = socket;
        this.socket.setSoTimeout(300);
        this.provider = provider;
        this.serverContext = serverContext;
    }
    
    private class ClientConnection implements Runnable {
        Socket client;
        
        @Override
        public void run() {
            String user = "";
            synchronized (activeUsers) {
                user = "" + client.getInetAddress() + ":" + client.getPort();
                activeUsers.add(user);
            }
            InputStream inStream = null;
            PrintStream outStream = null;
            try {
                inStream = client.getInputStream();
                outStream = new PrintStream(client.getOutputStream());
                HashMap<String, Command> cmdMap = ShellUtility.initCmdMap(new DatabaseContext(provider), serverContext);
                ShellUtility.execShell(cmdMap, inStream, outStream, outStream, true, false);
            } catch (Exception e) {
                if (outStream != null) {
                    outStream.println(e.getMessage());
                }
            } finally {
                synchronized (activeUsers) {
                    activeUsers.remove(user);
                }
            }
        }
        
        ClientConnection(Socket client) {
            this.client = client;
        }
    }
}
