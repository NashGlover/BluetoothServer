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

import java.net.ServerSocket;
import java.net.Socket;

import javax.bluetooth.*;
import javax.microedition.io.*;

/**
 *
 * @author Administrator
 */
public class BluetoothServer {

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
        
        RemoteDevice dev = RemoteDevice.getRemoteDevice(connection);
        System.out.println("Remote device address: " + dev.getBluetoothAddress());
        System.out.println("Remote device name: " + dev.getFriendlyName(true));
        
        //InputStream inStream = connection.openInputStream();
        DataInputStream inputStream = new DataInputStream(connection.openInputStream());
        DataOutputStream outputStream = new DataOutputStream(connection.openDataOutputStream());
        
        String message = "Hello from computer";
        System.out.println("About to write...");
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
        }
        byte[] outputMessage = message.getBytes(); 
        //outputMessage = message.getBytes();
        outputStream.write(outputMessage);
        System.out.println("Wrote");
        
        //BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
        byte[] messageByte = new byte[1000];
        System.out.println("Reading line...");
        //String lineRead = bReader.readLine();
        inputStream.read(messageByte);
        //System.out.println(messageByte.toString());
        String finalMsg = new String(messageByte, "UTF-8");
        System.out.println(finalMsg);
        System.out.println("Read");
        
        
        //read string form spp client
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
    }
    
}
