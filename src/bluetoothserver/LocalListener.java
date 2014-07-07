/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bluetoothserver;

import java.net.ServerSocket;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author Administrator
 */
public class LocalListener {
    ServerSocket listener = null;
    LinkedBlockingQueue<byte[]> messageQueue;
    
    public LocalListener (LinkedBlockingQueue<byte[]> _messageQueue) {
        messageQueue = _messageQueue;
    }
    
    public void connect() {
        Runnable runnable = new Runnable () {
            
            public void run() {
                System.out.println("Runnable running");
                try {
                    messageQueue.put("Hello".getBytes());
                    messageQueue.put("Another line".getBytes());
                    messageQueue.put("Third line.".getBytes());
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        };
        
        Thread thread = new Thread(runnable);
        thread.start();
        //Thread listenThread = new Thread()
    }
}
