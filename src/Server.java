package com.chesserver;

import javafx.application.Platform;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

// The Server class is the server, that is, the part for the other party in the game. When opening the program, the user is asked for the port to which
// opens a server socket that allows the player to join the game
// Like the Client, the Server also opens new threads for inbound and outbound traffic.
public class Server extends Thread {
    private int port;
    private trafficIn t_in;
    private trafficOut t_out;
    private ChessController chessServer;
    private boolean running = true;

    public Server(int port, ChessController chessServer){
        this.port = port;
        this.chessServer = chessServer;
    }
    public
    void stopRunning(){
        t_in.stopRunning();
        t_out.stopRunning();

    }


    public void send(String message){
        t_out.out(message);
    }


    public void sendMove(int x, int y, PieceModel pc){
        String pieceName = pc.getName();
        t_out.out("@" + " " +x +" "+ y+" "+pieceName);
    }

    @Override
    public void run() {
        try {
            ServerSocket s = new ServerSocket(port);
            String[] side ={"black", "white"};
            final String player;
            Random random = new Random();
            player = side[random.nextInt(side.length)];

            while(running) {
                Socket sock = s.accept();
                chessServer.getFxChatfield().appendText("The player joined from "+ sock.getInetAddress().toString() + "\n");

                t_in = new trafficIn(sock);
                t_in.start();
                t_out = new trafficOut(sock);
                t_out.start();

                Platform.runLater(()-> {
                    chessServer.startGame(player);
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private class trafficIn extends Thread{
        private Socket ssock;
        private boolean running = true;



        public void stopRunning(){
            t_out.out("Finished the game");
            try {
                ssock.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public trafficIn(Socket client) {
            this.ssock = client;
        }

        @Override
        public void run() {
            String line;
            try{
                BufferedReader inp = new BufferedReader(new InputStreamReader(ssock.getInputStream(), "UTF-8"));

                while(running){
                    try{
                        line = inp.readLine();
                        if(line == null) break;
                        if(line.contains("@")){
                            chessServer.interpretMove(line);
                        }
                        if(!line.contains("@") && line.length() > 2) chessServer.appendText(line);

                    }catch (IOException e){
                        ssock.close();
                        break;
                    }
                }
            }catch (IOException e){
                System.out.println("went wrong " +e);
            }
        }
    }

    public static void main(String[] args) {

    }


    private class trafficOut extends Thread {
        private Socket csock;
        private DataOutputStream out;

        public trafficOut(Socket client) {
            this.csock = client;
        }

        public void out(String msg){
            try {
                out.writeUTF(msg + "\n");
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void stopRunning(){
            try {
                csock.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                out = new DataOutputStream(csock.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

