package com.chesserver;

import javafx.application.Platform;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Client extends Thread {
    public int port;
    public InetAddress ia;
    public trafficIn t_in;
    public trafficOut t_out;
    public ChessController controller;
    private boolean running = true;

    // Makes new client with IP address, and port
    public Client(InetAddress ina, int port, ChessController controller){
        this.ia = ina;
        this.port = port;
        this.controller = controller;
    }

    // Stop running threads
    public void stopRunning(){
        this.running = false;
        t_in.stopRunning();
        t_out.stopRunning();

    }

    // Sending messages
    public void send(String message) {

        if(running)t_out.out(message);

    }

    // Sending moves
    public void sendMove(int x, int y, PieceModel piece){
        String pieceName = piece.getName();
        t_out.out("@" + " " +x +" "+ y+" "+pieceName);
    }

    // Starts new socket
    @Override
    public void run() {
        try {
            Socket soc = new Socket(ia,port);
            t_in = new trafficIn(soc);
            t_out = new trafficOut(soc);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Thread for incoming traffic
    private class trafficIn extends Thread{
        private Socket ssock;
        private String received;
        private boolean running = true;

        // Thread for incoming traffic
        private trafficIn(Socket client) {
            this.ssock = client;
            this.start();
        }

        // stop running and closes socket
        private void stopRunning(){
            try {
                ssock.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.running = false;

        }

        @Override
        public void run() {
            try {
                BufferedReader inp = new BufferedReader(new InputStreamReader(ssock.getInputStream(),"UTF-8"));
                while(running) {
                    received = inp.readLine();
                    // if opponent closes the game
                    if(received == null) {
                        Platform.runLater(()->{
                            controller.appendInfo("The opponent closed the game");
                        });
                        this.stopRunning();
                        break;
                    }
                    // Successful join of the chess game
                    if(received.contains("//0")){
                        Platform.runLater(()-> {
                            controller.appendInfo("You have successfully joined the game" + ssock.getInetAddress().toString() );
                            controller.startGame("black");
                        });
                    }
                    if(received.contains("//1")){
                        Platform.runLater(()-> {
                            controller.appendInfo("You have successfully joined the game" + ssock.getInetAddress().toString() );
                            controller.startGame("white");
                        });
                    }
                    if(received.contains("@")){
                        controller.interpretMove(received);
                    }

                    if(!received.contains("//0") && !received.contains("//1") && !received.contains("@") && received.length() >2) {
                        controller.appendText(received);
                    }
                }
            } catch (IOException e) {
                //e.printStackTrace();
                this.stopRunning();
            }
        }
    }

    // Thread of outbound traffic
    private class trafficOut extends Thread {
        private Socket csock;
        private DataOutputStream out;

        private trafficOut(Socket client) {
            this.csock = client;
            this.start();
        }
        private void stopRunning(){
            try {
                csock.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        // outputs message
        private void out(String msg){
            try {
                out.writeUTF(msg + "\n");

                out.flush();

            } catch (IOException e) {
                this.stopRunning();
            }
        }

        // runs new stream
        @Override
        public void run() {
            try {
                out = new DataOutputStream(csock.getOutputStream());

            } catch (IOException e) {
                this.stopRunning();
            }
        }
    }
}
