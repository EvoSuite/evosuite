package com.examples.with.different.packagename.mock.java.net;

import java.net.Socket;

/**
 * Created by arcuri on 12/19/14.
 */
public class SendTcp {

    public boolean send(int x) throws Exception{
        Socket s = new Socket("234.0.42.0", 12345);
        s.getOutputStream().write(x);

        return x > 0;
    }
}
