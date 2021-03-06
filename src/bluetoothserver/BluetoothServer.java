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
    StreamConnection connection;
    Communication communication;
    static LocalListener localListener;
    static Thread localListenerThread;
    
    AtomicBoolean running = new AtomicBoolean();
    
    static LinkedBlockingQueue<byte[]> messageQueue = new LinkedBlockingQueue<byte[]>();
    
    BluetoothServer() {
    }
        //start server
    
    private void startServer() throws IOException {
        
        
        
        // Create a UUID for SPP
        UUID uuid = new UUID("0001", true);
        System.out.println(uuid.toString());
        // Create the service url
        String connectionString = "btspp://localhost:" + uuid + ";name=Sample SPP Server";
        
        // open server url
        StreamConnectionNotifier streamConnNotifier = (StreamConnectionNotifier)Connector.open(connectionString);
        
        // Wait for client connection
        System.out.println("\nServer started. Waiting for clients to connect...");
        connection = streamConnNotifier.acceptAndOpen();
        
        dev = RemoteDevice.getRemoteDevice(connection);
        System.out.println("Remote device address: " + dev.getBluetoothAddress());
       // System.out.println("Remote device name: " + dev.getFriendlyName(true));
        
        //InputStream inStream = connection.openInputStream();
        bluetoothInputStream = new DataInputStream(connection.openInputStream());
        bluetoothOutputStream = new DataOutputStream(connection.openDataOutputStream());
    }
    
    
    private void listenToMessage() {
        System.out.println("In listenToMessage");
        byte[] messageByte = new byte[1000];
        System.out.println("In the listenToMessage");
        running.set(true);
        while (running.get()) {
            try {
                String message;
                System.out.println("Taking a message from queue");
                messageByte = messageQueue.take();
                message = new String(messageByte, "UTF-8");
                if (message.equals("Done.")) {
                    running.set(false);
                }
                else if (message.equals("LocalListener connected")) {
                    System.out.println("LocalListener connted");
                    communication = new Communication(bluetoothOutputStream, localListener.getSocket(), messageQueue);
                    (new Thread(communication)).start();
                }
                else if (message.equals("Disconnected")) {
                    System.out.println("Got the disconnected message");
                    running.set(false);
                    localListener.disconnect();
                    connection.close();
                }
                System.out.println("Printout out message");
                System.out.println(message);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                } catch (UnsupportedEncodingException codingException) {
                    System.out.println("Unsupported Coding Exception");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
        }
        System.out.println("listenToMessage done");
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
        Boolean run = true;
        while (run) {
            BluetoothServer server = new BluetoothServer();
            server.startServer();
            localListener = new LocalListener(messageQueue, 2222);
            localListenerThread = new Thread(localListener);
            localListenerThread.start();
            server.listenToMessage();
        }
    }
    
}
