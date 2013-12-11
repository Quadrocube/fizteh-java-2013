package ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NetworkUtils {
    
    static String queryResponse(Socket connection, String request) throws IOException {
        PrintWriter out = new PrintWriter(connection.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        synchronized (connection) {
            if (request != null) {
                out.println(request);
            }
            return in.readLine();
        }
    }
    
    static String queryResponse(Socket connection, BufferedReader in, PrintWriter out, String request) 
            throws IOException {
        synchronized (connection) {
            if (request != null) {
                out.println(request);
            }
            return in.readLine();
        }
    }
}
