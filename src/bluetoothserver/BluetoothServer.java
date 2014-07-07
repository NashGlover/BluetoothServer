/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bluetoothserver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.bluetooth.*;
import javax.microedition.io.*;

/**
 *
 * @author Administrator
 */
public class BluetoothServer {

    RemoteDevice dev = null;
    DataInputStream bluetoothInputStream = null;
    DataOutputStream bluetoothOutputStream = null;
    
    static LocalListener localListener;
    
    AtomicBoolean running = new AtomicBoolean();
    
    static LinkedBlockingQueue<byte[]> messageQueue = new LinkedBlockingQueue<byte[]>();
    
    BluetoothServer() {
    }
        //start server
    
    private void startServer() throws IOException {
        
        
        
        // Create a UUID for SPP
        UUID uuid = new UUID("1101", true);
        // Create the service url
        String connectionString = "btspp://localhost:" + uuid + ";name=Sample SPP Server";
        
        // open server url
        StreamConnectionNotifier streamConnNotifier = (StreamConnectionNotifier)Connector.open(connectionString);
        
        // Wait for client connection
        System.out.println("\nServer started. Waiting for clients to connect...");
        StreamConnection connection = streamConnNotifier.acceptAndOpen();
        
        dev = RemoteDevice.getRemoteDevice(connection);
        System.out.println("Remote device address: " + dev.getBluetoothAddress());
        System.out.println("Remote device name: " + dev.getFriendlyName(true));
        
        //InputStream inStream = connection.openInputStream();
        bluetoothInputStream = new DataInputStream(connection.openInputStream());
        bluetoothOutputStream = new DataOutputStream(connection.openDataOutputStream());
        
        /*String message = "Hello from computer";
        
        System.out.println("About to write...");
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
        }
        byte[] outputMessage = message.getBytes(); 
        //outputMessage = message.getBytes();
        bluetoothOutputStream.write(outputMessage);
        System.out.println("Wrote");
        
        //BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
        
        //read string form spp client*/
    }
    
    public void readMessagesFromClient() {
        byte[] messageByte = new byte[1000];
        try {
            bluetoothInputStream.read(messageByte);
            String message = new String(messageByte, "UTF-8");
            while (!message.equals("Done.")) {
                System.out.println(message);
                messageByte = new byte[1000];
                bluetoothInputStream.read(messageByte);
                message = "";
                message = new String(messageByte, "UTF-8");
                message = message.trim();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Done");
        messageByte = "Done.".getBytes();
        try {
            messageQueue.put(messageByte);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        running.set(false);
    }
    
    private void listenToMessage() {
        System.out.println("In listenToMessage");
        Runnable runnable = new Runnable() {
            byte[] messageByte = new byte[1000];
            public void run() {
                System.out.println("In the listenToMessage");
                running.set(true);
                while (running.get()) {
                    try {
                        System.out.println("Before message");
                        String message;
                        System.out.println("Taking a message from queue");
                        messageByte = messageQueue.take();
                        message = new String(messageByte, "UTF-8");
                        if (message.equals("Done.")) {
                            running.set(false);
                        }
                        System.out.println("Printout out message");
                        System.out.println(message);
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                    } catch (UnsupportedEncodingException codingException) {
                        System.out.println("Unsupported Coding Exception");
                    }
                }
                System.out.println("listenToMessage done");
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
    
    private void listen() {
        byte[] messageByte = new byte[1000];
        System.out.println("Reading line...");
        //String lineRead = bReader.readLine();
        try {
            bluetoothInputStream.read(messageByte);
            //System.out.println(messageByte.toString());
            String finalMsg = new String(messageByte, "UTF-8");
            System.out.println(finalMsg);
            System.out.println("Read");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        System.out.println("Address: " + localDevice.getBluetoothAddress());
        System.out.println("Name: " + localDevice.getFriendlyName());
        
        BluetoothServer server = new BluetoothServer();
        server.startServer();
        server.listenToMessage();
        localListener = new LocalListener(messageQueue);
        localListener.connect();
        server.readMessagesFromClient();
        System.out.println("DONE WITH THE MAIN THREAD!");
    }
    
}
