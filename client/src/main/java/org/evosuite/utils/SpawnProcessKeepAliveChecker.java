/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * When using CTG, might end up with many, many processes that are spawn.
 * However, if a controlling process fails (eg Maven) or is killed, it might
 * not properly shut down the spawn processes (JVM hooks only apply if JVM terminates
 * normally). As such, each spawn process should query at regular intervals if it
 * still can keep running.
 *
 * Created by Andrea Arcuri on 26/11/15.
 */
public class SpawnProcessKeepAliveChecker {

    private static final Logger logger = LoggerFactory.getLogger(SpawnProcessKeepAliveChecker.class);

    private final static SpawnProcessKeepAliveChecker instance = new SpawnProcessKeepAliveChecker();

    private final static ExecutorService executor = Executors.newCachedThreadPool();
    private final static String STILL_ALIVE = "still_alive";
    private final static int DELTA_MS = 5_000;

    private volatile ServerSocket server;
    private volatile Thread serverThread;
    private volatile Thread clientThread;

    public static SpawnProcessKeepAliveChecker getInstance(){
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
                        logger.info("Registered remote process from "+socket.getRemoteSocketAddress());
                    } catch (IOException e) {
                        //fine, expected
                        return;
                    }
                }

            }
        };
        serverThread.start();

        int port = server.getLocalPort();
        logger.info("Started spawn process manager on port {}", port);

        return port;
    }

    public void stopServer(){
        logger.info("Stopping spawn process manager");
        try {
            if(server != null) {
                server.close();
                server = null;
            }
        } catch (IOException e) {
            logger.error(e.toString());
        }

        if(serverThread != null) {
            serverThread.interrupt();
            serverThread = null;
        }
    }

    public void registerToRemoteServerAndDieIfFails(final int port) throws IllegalStateException{
        if(clientThread != null){
            throw new IllegalStateException("Already registered");
        }

        clientThread = new Thread(){
          @Override public void run(){

              boolean failed = false;

              try {
                  Socket socket = new Socket(InetAddress.getLoopbackAddress(), port);
                  Scanner in = new Scanner(socket.getInputStream());

                  sleep(DELTA_MS);

                  while(! isInterrupted()){
                      if(! in.hasNext()){
                          failed = true;
                          break;
                      } else {
                          in.nextLine();
                      }

                      sleep(DELTA_MS);
                  }

              } catch (IOException e) {
                  failed = true;
              } catch (InterruptedException e) {
                  //this is fine, and expected when process ends
                  failed = false;
              }

              if(failed){
                  logger.error("Failed to receive keep alive message. Going to shutdown the process.");
                  try { sleep(200); } catch (InterruptedException e) {}

                  System.exit(1);
              }
          }
        };
        clientThread.start();
    }

    public void unRegister(){
        if(clientThread != null){
            clientThread.interrupt();
            clientThread = null;
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
