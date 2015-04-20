package com.examples.with.different.packagename.mock.java.net;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by arcuri on 12/19/14.
 */
public class ReceiveTcp {

    public boolean getMessage() throws Exception{
        ServerSocket server = new ServerSocket(1234,10, InetAddress.getByName("127.0.0.4"));
        Socket s = server.accept();
        Scanner in = new Scanner(s.getInputStream());
        String msg = in.nextLine();

        if(msg.equals("Got message on TCP connection")){
            return true;
        } else {
            return false;
        }
    }
}
