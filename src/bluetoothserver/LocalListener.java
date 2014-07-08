/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bluetoothserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author Administrator
 */
public class LocalListener {
    ServerSocket listener = null;
    Socket clientSocket = null;
    int port;
    LinkedBlockingQueue<byte[]> messageQueue;
    
    private DataInputStream in = null;
    
    public LocalListener (LinkedBlockingQueue<byte[]> _messageQueue, int _port) {
        messageQueue = _messageQueue;
        port = _port;
    }
    
    public void connect() {
        
        Runnable runnable = new Runnable () {
            
            public void run() {
                System.out.println("Runnable running");
                try {
                    listener = new ServerSocket(2222);
                    //listener.setSoTimeout(15000);
                    System.out.println("Waiting for AIONAV connection...");
                    clientSocket = listener.accept();
                    System.out.println("AIONAV connection started!");
                    in = new DataInputStream(clientSocket.getInputStream());
                    messageQueue.put("LocalListener connected".getBytes());
                } catch (SocketException sockE) {
                    System.out.println("Socket exception: " + sockE.getMessage());
                } catch (InterruptedException interrupt) {
                    System.out.println("interrupted: " + interrupt.getMessage());
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        };
        
        Thread thread = new Thread(runnable);
        thread.start();
        //Thread listenThread = new Thread()
    }
    
    public DataInputStream getInputStream() {
        return in;
    }
    
    public Socket getSocket() {
        return clientSocket;
    }
}
