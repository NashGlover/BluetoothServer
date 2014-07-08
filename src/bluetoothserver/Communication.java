/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bluetoothserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.nio.ByteBuffer;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.bluetooth.*;
import javax.microedition.io.*;

/**
 *
 * @author Administrator
 */
public class Communication {
    Socket aionavSocket;
    RemoteDevice dev;
    DataInputStream in;
    AtomicBoolean tracking;
    DataOutputStream out;
    StreamConnection bluetoothConnection;
    
    public Communication(DataOutputStream _out, Socket _aionavSocket) {
      //  bluetoothConnection = _bluetoothConnection;
        aionavSocket = _aionavSocket;
        try {
            in = new DataInputStream(aionavSocket.getInputStream());
            out = _out;
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        tracking = new AtomicBoolean();
    }
    
    public void start() {
        System.out.println("In start!");
        tracking.set(true);
        byte[] messageBytes = new byte[1000];
        Runnable runnable = new Runnable() {
            public void run() {
                int length;
                int packetType;
                long timestamp = 0;
                double longitude, latitude, altitude;
                double x, y, z;
                while (tracking.get()) {
                    try {
                        int bytesRead;
                        bytesRead = in.read(messageBytes);
                        ByteBuffer buffer = ByteBuffer.wrap(messageBytes);
                        if (bytesRead == 32 || bytesRead == 56) {
                            length = buffer.getInt();
                            packetType = buffer.getInt();
                            buffer.getLong(8);
                            buffer.getLong(16);
                            if (bytesRead == 32) {
                                timestamp = buffer.getLong(24);
                                System.out.println("Heartbeat!");
                            } else if (bytesRead == 56) {
                                System.out.println("Location update!");
                                if (packetType == 1) {
                                    out.write(messageBytes);
                                }
                            }
                        }
                    } catch (IOException ioException) {
                        System.out.println(ioException.getMessage());
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
}
