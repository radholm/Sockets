/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockets.client.controller;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import sockets.client.net.ServerConnection;
import sockets.client.net.OutputHandler;
        
/**
 *
 * @author Fredrik
 */
public class Controller {
    private final ServerConnection serverConnection = new ServerConnection();
    
    public void connect(String host, int port, OutputHandler outputHandler) {
        CompletableFuture.runAsync(() -> {
            try {
                serverConnection.connect(host, port, outputHandler);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }).thenRun(() -> outputHandler.handleMsg("Connected to " + host + ": " + port + "\n"));
    }
    
    public void disconnect() throws IOException {
        serverConnection.disconnect();
    }
    
    public void sendMsg(String msg) {
        CompletableFuture.runAsync(() -> serverConnection.sendMsgEntry(msg));
    }
}
