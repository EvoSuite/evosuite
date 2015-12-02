package org.evosuite.intellij.util;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Note: this is copy&paste adaptation of the class SpawnProcessKeepAliveChecker.
 * Issue here we cannot reuse the original due to how the IntelliJ plugin is built
 *
 * Created by Andrea Arcuri on 26/11/15.
 */
public class SpawnProcessKeepAliveCheckerIntelliJ {


    private final static SpawnProcessKeepAliveCheckerIntelliJ instance = new SpawnProcessKeepAliveCheckerIntelliJ();

    private final static ExecutorService executor = Executors.newCachedThreadPool();
    private final static String STILL_ALIVE = "still_alive";
    private final static int DELTA_MS = 5_000;

    private volatile ServerSocket server;
    private volatile Thread serverThread;

    public static SpawnProcessKeepAliveCheckerIntelliJ getInstance(){
        return instance;
    }


    public int startServer() throws IllegalStateException{

        if(server != null || serverThread != null){
            throw  new IllegalStateException("Recorder already running");
        }

        try {
            server = new ServerSocket(0, -1, InetAddress.getLoopbackAddress());
        } catch (IOException e) {
            return -1;
        }

        serverThread = new Thread(){
            @Override public void run(){
                while(! isInterrupted() && server!=null && !server.isClosed()){
                    try {
                        Socket socket = server.accept();
                        socket.setKeepAlive(true);
                        executor.submit(new KeepAliveTask(socket));
                        //logger.info("Registered remote process from "+socket.getRemoteSocketAddress());
                    } catch (IOException e) {
                        //fine, expected
                        return;
                    }
                }

            }
        };
        serverThread.start();

        int port = server.getLocalPort();
        //logger.info("Started spawn process manager on port {}", port);

        return port;
    }

    public void stopServer(){
        //logger.info("Stopping spawn process manager");
        try {
            if(server != null) {
                server.close();
                server = null;
            }
        } catch (IOException e) {
            //logger.error(e.toString());
        }

        if(serverThread != null) {
            serverThread.interrupt();
            serverThread = null;
        }
    }


    private static class KeepAliveTask implements Runnable{

        private final Socket socket;

        public KeepAliveTask(Socket s){
            socket = s;
        }

        @Override
        public void run() {
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream());
                while (socket.isConnected()) {
                    out.println(STILL_ALIVE);
                    Thread.sleep(DELTA_MS);
                }
            } catch (Exception e){
                //expected when remote host dies
            }
        }
    }
}
