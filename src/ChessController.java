package com.chesserver;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.util.ResourceBundle;
import java.util.Optional;
import javafx.scene.control.TextInputDialog;
import java.io.File;
import java.io.FileWriter;

public class ChessController implements Initializable{
    private Game game;
    private boolean isServer = true;
    private Server server;
    private Client client;
    private String player;
    private String IPString;
    private String PortString;
    private String localPort;
    @FXML private TextField fxIP;
    @FXML private TextField fxPorts;
    @FXML private TextField fxChatbox;
    @FXML private TextArea fxChatfield;
    @FXML private GridPane fxChessgrid;

    @FXML void handleJoin(){
        isServer = false;
        try {
            client = new Client(InetAddress.getByName(IPString), Integer.parseInt(PortString), this);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            fxChatfield.appendText("Unable to join IP address" +"\n");
        }
        client.start();
    }

    @FXML void handleChat(){
        String msg = fxChatbox.getText();
        fxChatfield.appendText(msg + "\n");
        if(isServer) server.send(msg);
        if(!isServer) client.send(msg);
        fxChatbox.clear();
    }


    @FXML void handleStop(){
        if(!isServer) client.stopRunning();
        if(isServer) server.stopRunning();
        fxChessgrid.getChildren().clear();
        fxChatfield.appendText("Let's stop the game"+ "\n");

    }


    public void chat(){

        fxIP.textProperty().addListener((observable, f, newIP) -> {
            IPString = newIP;
        });

        fxPorts.textProperty().addListener((observable, f, newPort) -> {
            if(newPort.matches("[0-9]+"))PortString = newPort;

        });

        TextInputDialog f = new TextInputDialog("");
        f.setResizable(true);
        Platform.runLater(() -> {
            f.setResizable(false);
        });
        f.setTitle("Chess");
        f.setHeaderText("Port, which allows another player to join");
        Optional<String> result = f.showAndWait();


        result.ifPresent(name -> {
            if(name.matches("[0-9]+")){
                localPort = name;
                startServer();
            }
            else fxChatfield.appendText("The port you entered is invalid, please restart the game if you want the game to be able to join" + "\n");
        });

    }


    public void startServer(){
        server = new Server(Integer.parseInt(localPort), this);
        server.start();
    }


    public void startGame(String port){
        createBoard();
        setPlayer(port);
        fxChatfield.appendText("You are a player: " + port + "\n");
        int playerPort = 0;
        if(port.contains("black")) playerPort = 0;
        if(port.contains("white")) playerPort = 1;

        game = new Game(fxChessgrid,playerPort,this);
        game.spawn();

    }

    public TextArea getFxChatfield(){
        return fxChatfield;
    }

    public void setPlayer(String player){
        this.player = player;
        if(isServer) {
            if (player == "black") server.send("//1");
            else server.send("//0");
        }
    }


    public void appendText(String text){
        Platform.runLater(() -> {
            if (player == "white") fxChatfield.appendText("Black: " + text + "\n");
            else fxChatfield.appendText("White: " + text + "\n");
        });
    }


    public void appendInfo(String text){
        Platform.runLater(()-> {
            fxChatfield.appendText(text + "\n");
        });
    }


    public void createBoard(){

        for (int i = 0; i<8; i++) {
            for (int j = 0; j<8; j++) {
                Rectangle square = new Rectangle();
                Color color;
                if ((i+j) %2 == 0) {
                    color = Color.valueOf("#eeeeee");
                } else {
                    color = Color.valueOf("#82747e");
                }

                square.setFill(color);

                GridPane.setConstraints(square, i,j);
                fxChessgrid.add(square, i, j);
                square.widthProperty().bind(fxChessgrid.widthProperty().divide(8));
                square.heightProperty().bind(fxChessgrid.heightProperty().divide(8));

            }
        }
    }


    public void sendMove(int xcoord, int ycoord, PieceModel ps){
        int mX = 7-xcoord;
        int mY = 7-ycoord;


        if(isServer) server.sendMove(mX, mY, ps);
        if(!isServer) client.sendMove(mX, mY, ps);
        if (player == "white") {
            appendInfo("White " + ps.getPieceType() + " to " + game.convertToChar(xcoord) + (8 - ycoord));
            return;
        }
        if (player=="black") {
            appendInfo("Black " + ps.getPieceType() + " to " + game.convertToChar(mX) + (8 - mY));
        }
    }


    public void interpretMove(String move){
        String[] split = move.split(" ");
        String xCoord = split[1];
        String yCoord = split[2];
        String piecename = split[3];;
        game.moveOpponent(game.getByName(piecename), Integer.parseInt(xCoord), Integer.parseInt(yCoord));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chat();

    }

}