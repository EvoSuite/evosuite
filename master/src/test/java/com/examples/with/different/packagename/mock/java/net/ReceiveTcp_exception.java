package com.examples.with.different.packagename.mock.java.net;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by arcuri on 12/28/14.
 */
public class ReceiveTcp_exception {


    public void getMessage() throws Exception{
        ServerSocket server = new ServerSocket(1234,10, InetAddress.getByName("127.0.0.4"));

        Socket s = server.accept();

        System.out.println("Accepted connection");
    }
}
