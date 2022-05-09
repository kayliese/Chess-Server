package com.chesserver;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

import java.io.File;
import java.io.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class Game {

    private double moveStartx;
    private double moveStarty;
    private GridPane chessBoard;
    private int playerside;
    private boolean dragged = false;
    private boolean turn = false;
    private PieceModel highlightable;
    private PieceModel currentpiece;
    private ArrayList<PieceModel> pieces = new ArrayList<>();
    private ArrayList<Rectangle> possibilities = new ArrayList<>();
    private ChessController controller;

    Rectangle[][] rec = new Rectangle[8][8];
    PieceModel[][] piece = new PieceModel[8][8];
    private double mouseX, mouseY;
    private double oldX, oldY;
    private double newTranslateX;
    private double newTranslateY;

    // New Game
    public Game(GridPane newChessBoard, int playerside, ChessController controller) {

        chessBoard = newChessBoard;
        this.playerside = playerside;
        this.controller = controller;

    }

    // spawn pieces on the board
    public void spawn(){

        int size = chessBoard.heightProperty().intValue() / 8;
        chessBoard.getColumnConstraints().add(new ColumnConstraints(0));
        chessBoard.getRowConstraints().add(new RowConstraints(0));
        Platform.runLater(() -> {
            spawnPieces(playerside, size);
        });

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Rectangle r = (Rectangle) chessBoard.getChildren().get(i * 8 + j);
                setListener(r);
            }
        }

        if (playerside == 1) turn = true;


        // creates file to save moves and write moves to files
        File gameFile = null;
        try {
            gameFile = new File("gamefile.txt");
            if (gameFile.createNewFile()) {
                System.out.println("File created: " + gameFile.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(gameFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        writer.print("");
        writer.close();
    }

    // Convert the moves to FEN Notation
    // For more information https://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation
    static void convertToFEN(PieceModel[][] pos) {
        String curr = "";
        StringBuilder fen= new StringBuilder();
        int count = 0;
        for (int i = 0; i < 8; i ++) {
            for (int j = 0; j < 8; j ++) {
                if (pos[i][j] != null) {
                    switch (pos[i][j].getPieceType()) {
                        case "king" -> {
                            curr = count == 0 ? "k" : count + "k";
                            count = 0;
                        }
                        case "rook" -> {
                            curr = count == 0 ? "r" : count + "r";
                            count = 0;
                        }
                        case "pawn" -> {
                            curr = count == 0 ? "p" : count + "p";
                            count = 0;
                        }
                        case "queen" -> {
                            curr = count == 0 ? "q" : count + "q";
                            count = 0;
                        }
                        case "bishop" -> {
                            curr = count == 0 ? "b" : count + "b";
                            count = 0;
                        }
                        case "knight" -> {
                            curr = count == 0 ? "n" : count + "n";
                            count = 0;
                        }
                    }
                    if (pos[i][j].getSide() == 0) {
                        curr = curr.toUpperCase();
                    }
                    fen.append(curr);
                } else {
                    count++;
                }
            }
            if (count != 0) {
                fen.append(count);
                count =0;
            }
            fen.append("/");

        }

        // writing to file
        try {
            FileWriter myWriter = new FileWriter("gamefile.txt", true);
            myWriter.write(String.valueOf(fen) + "\n");
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    // Piece listener to highlight valid moves a piece can make
    // As well as moving pieces on the board
    private void setPieceListener(PieceModel piece){
        piece.setOnMousePressed(e -> {
            dragged = false;
            moveStartx = piece.getTranslateX();
            moveStarty = piece.getTranslateY();

            mouseX = e.getSceneX();
            mouseY = e.getSceneY();
            oldX = piece.getTranslateX();
            oldY = piece.getTranslateY();

            piece.setMouseTransparent(true);


            e.consume();
        });

        piece.setOnMouseReleased(e -> {
            Rectangle rec = getRbyC(piece.getTranslateX(),piece.getTranslateY());

            if(!dragged || !possibilities.contains(rec)){
                piece.setTranslateX(moveStartx);
                piece.setTranslateY(moveStarty);
            }
            if(piece.getTranslateX() < -1 || piece.getTranslateX() > 500 || piece.getTranslateY() < -1 || piece.getTranslateY() > 500){
                piece.setTranslateX(moveStartx);
                piece.setTranslateY(moveStarty);
            }

            for(Rectangle r : possibilities){
                r.setStrokeWidth(0);
            }
            piece.setMouseTransparent(false);
            e.consume();

        });
        piece.setOnDragDetected(e -> {
            dragged = true;
            piece.startFullDrag();
            currentpiece = piece;
            ArrayList<Rectangle> canBeMoved = getAvailable(currentpiece);
            possibilities = canBeMoved;
            for(Rectangle rec : possibilities){
                rec.setStrokeType(StrokeType.INSIDE);
                rec.setStrokeWidth(4);
                rec.setStroke(Color.GREEN);
            }

            currentpiece.toFront();
            piece.setMouseTransparent(true);
            e.consume();

        });
        piece.setOnMouseDragged(e -> {

            double offsetX = e.getSceneX() - mouseX;
            double offsetY = e.getSceneY() - mouseY;

            newTranslateX = oldX + offsetX;
            newTranslateY = oldY + offsetY;
            piece.setMouseTransparent(true);

            piece.setTranslateX(newTranslateX);
            piece.setTranslateY(newTranslateY);
            e.consume();

        });

        piece.setOnMouseDragReleased(e -> {
            String side;
            if(playerside == 0) side = "b";
            else side = "w";

            if(possibilities.contains(rec[piece.getY()][piece.getX()]) && currentpiece.getName().startsWith(side) && !piece.getName().startsWith(side)) {

                if(highlightable != null) highlightable.removeHighlight();
                highlightable = currentpiece;
                highlightable.setHighlight();
            }

            if(turn) {
                Platform.runLater(() -> {
                    Rectangle r = getRbyC(piece.getTranslateX(),piece.getTranslateY());
                    if(possibilities.contains(r)) {

                        currentpiece.setTranslateX(piece.getTranslateX());
                        currentpiece.setTranslateY(piece.getTranslateY());
                        int X = 0;
                        int Y = 0;
                        for (int i = 0; i < 8; i++) {
                            for (int j = 0; j < 8; j++) {
                                if (this.rec[i][j] == r) {
                                    X = j;
                                    Y = i;
                                }
                            }
                        }
                        final int fX = X;
                        final int fY = Y;
                        controller.sendMove(fX, fY, currentpiece);


                        this.piece[piece.getY()][piece.getX()] = currentpiece;
                        this.piece[currentpiece.getY()][currentpiece.getX()] = null;
                        currentpiece.setX(piece.getX());
                        currentpiece.setY(piece.getY());
                        currentpiece.setHasMoved();


                        chessBoard.getChildren().remove(piece);
                        if(piece.getPieceType() == "king"){
                            Platform.runLater(()-> {
                                controller.appendInfo("You won the game");
                            });
                        }
                        turn = false;

                        for(Rectangle re : possibilities){
                            re.setStrokeWidth(0);
                        }
                        possibilities = new ArrayList<>();


                    }
                });
            }
        });
    }


    // Piece listener to highlight valid moves a piece can make
    // As well as moving pieces on the board
    private void setListener(Rectangle rec) {
        rec.setOnMouseDragReleased(e -> {
            if(turn && possibilities.contains(rec)) {
                if(highlightable != null)highlightable.removeHighlight();
                int X = 0;
                int Y = 0;
                for(int i = 0; i<8;i++){
                    for(int j = 0; j<8;j++){
                        if(this.rec[i][j] == rec){
                            X = j;
                            Y = i;
                        }
                    }
                }
                final int fX = X;
                final int fY = Y;

                piece[currentpiece.getY()][currentpiece.getX()] = null;
                piece[Y][X] = currentpiece;

                currentpiece.setTranslateX(rec.localToParent(rec.getX(), rec.getY()).getX());
                currentpiece.setTranslateY(rec.localToParent(rec.getX(), rec.getY()).getY());
                currentpiece.setX(fX);
                currentpiece.setY(fY);
                highlightable = currentpiece;
                highlightable.setHighlight();

                if(currentpiece.getPieceType() == "king" && !currentpiece.gethasMoved() && X == 6 && Y == 7 && playerside == 1){
                    PieceModel wrrook = piece[7][7];
                    Rectangle wrcas = this.rec[7][5];
                    piece[7][7] = null;
                    piece[7][5] = wrrook;
                    wrrook.setTranslateX(wrcas.localToParent(wrcas.getX(), wrcas.getY()).getX());
                    wrrook.setTranslateY(wrcas.localToParent(wrcas.getX(), wrcas.getY()).getY());
                    wrrook.setX(5);
                    wrrook.setY(7);
                }

                if(currentpiece.getPieceType() == "king" && !currentpiece.gethasMoved() && X ==5 && Y ==7 && playerside == 0){
                    PieceModel brrook = piece[7][7];
                    Rectangle brcas = this.rec[7][4];
                    piece[7][7] = null;
                    piece[7][4] = brrook;
                    brrook.setTranslateX(brcas.localToParent(brcas.getX(), brcas.getY()).getX());
                    brrook.setTranslateY(brcas.localToParent(brcas.getX(), brcas.getY()).getY());
                    brrook.setX(4);
                    brrook.setY(7);
                }

                if(currentpiece.getPieceType() == "king" && !currentpiece.gethasMoved() && X == 1 && Y == 7 && playerside == 0){
                    PieceModel lrrook = piece[7][0];
                    Rectangle lrcas = this.rec[7][2];
                    piece[7][0] = null;
                    piece[7][2] = lrrook;
                    lrrook.setTranslateX(lrcas.localToParent(lrcas.getX(), lrcas.getY()).getX());
                    lrrook.setTranslateY(lrcas.localToParent(lrcas.getX(), lrcas.getY()).getY());
                    lrrook.setX(2);
                    lrrook.setY(7);
                }

                if(currentpiece.getPieceType() == "king" && !currentpiece.gethasMoved() && X == 2 && Y == 7 && playerside == 1){
                    PieceModel lwrook = piece[7][0];
                    Rectangle blcas = this.rec[7][3];
                    piece[7][0] = null;
                    piece[7][3] = lwrook;
                    lwrook.setTranslateX(blcas.localToParent(blcas.getX(), blcas.getY()).getX());
                    lwrook.setTranslateY(blcas.localToParent(blcas.getX(), blcas.getY()).getY());
                    lwrook.setX(3);
                    lwrook.setY(7);

                }

                currentpiece.setHasMoved();

                for(Rectangle re : possibilities){
                    re.setStrokeWidth(0);
                }

                Platform.runLater(() -> {
                    controller.sendMove(fX, fY, currentpiece);
                });
                turn = false;
            }
            else{
                currentpiece.setTranslateX(moveStartx);
                currentpiece.setTranslateY(moveStarty);
            }
        });
    }


    // Piece move gets the rectangle and piece that is on that rectangle
    private void move(PieceModel piece, Rectangle rec ){


        piece.setTranslateX(rec.localToParent(rec.getX(),rec.getY()).getX());
        piece.setTranslateY(rec.localToParent(rec.getX(),rec.getY()).getY());

        int X = 0;
        int Y = 0;
        for(int i = 0; i<8;i++){
            for(int j = 0; j<8;j++){
                if(this.rec[i][j] == rec){
                    X = j;
                    Y = i;
                }
            }
        }
        piece.setX(X);
        piece.setY(Y);

    }


    // Gets the opponent's move
    public void moveOpponent(PieceModel piece, int xCoord, int yCoord){
        Rectangle rec = this.rec[yCoord][xCoord];
        piece.setTranslateX(rec.localToParent(rec.getX(),rec.getY()).getX());
        piece.setTranslateY(rec.localToParent(rec.getX(),rec.getY()).getY());
        if(highlightable != null)highlightable.removeHighlight();
        highlightable = piece;
        highlightable.setHighlight();


        if(piece.getPieceType() == "king" && !piece.gethasMoved() && playerside == 0 && xCoord == 1 && yCoord == 0 && !this.piece[0][0].gethasMoved()){
            PieceModel lwrook = this.piece[0][0];
            Rectangle lwr = this.rec[0][2];
            lwrook.setTranslateX(lwr.localToParent(lwr.getX(), lwr.getY()).getX());
            lwrook.setTranslateY(lwr.localToParent(lwr.getX(), lwr.getY()).getY());
            this.piece[0][2] = lwrook;
            this.piece[0][0] = null;
            lwrook.setX(2);
            lwrook.setY(0);
        }

        if(piece.getPieceType() == "king" && !piece.gethasMoved() && playerside == 0 && xCoord == 5 && yCoord == 0 && !this.piece[0][7].gethasMoved()){
            PieceModel rwrook = this.piece[0][7];
            Rectangle rwr = this.rec[0][4];
            rwrook.setTranslateX(rwr.localToParent(rwr.getX(), rwr.getY()).getX());
            rwrook.setTranslateY(rwr.localToParent(rwr.getX(), rwr.getY()).getY());
            this.piece[0][4] = rwrook;
            this.piece[0][7] = null;
            rwrook.setX(4);
            rwrook.setY(0);
        }

        if(piece.getPieceType() == "king" && !piece.gethasMoved() && playerside == 1 && xCoord == 2 && yCoord == 0 && !this.piece[0][0].gethasMoved()){
            PieceModel lbrook = this.piece[0][0];
            Rectangle lbr = this.rec[0][3];
            lbrook.setTranslateX(lbr.localToParent(lbr.getX(), lbr.getY()).getX());
            lbrook.setTranslateY(lbr.localToParent(lbr.getX(), lbr.getY()).getY());
            this.piece[0][3] = lbrook;
            this.piece[0][0] = null;
            lbrook.setX(3);
            lbrook.setY(0);
        }

        if(piece.getPieceType() == "king" && !piece.gethasMoved() && playerside == 1 && xCoord == 6 && yCoord == 0 && !this.piece[0][7].gethasMoved()){
            PieceModel rbrook = this.piece[0][7];
            Rectangle rbr = this.rec[0][5];
            rbrook.setTranslateX(rbr.localToParent(rbr.getX(), rbr.getY()).getX());
            rbrook.setTranslateY(rbr.localToParent(rbr.getX(), rbr.getY()).getY());
            this.piece[0][5] = rbrook;
            this.piece[0][7] = null;
            rbrook.setX(5);
            rbrook.setY(0);
        }

        for(PieceModel pi : pieces){
            if(pi.getX() == xCoord && pi.getY() == yCoord && piece != pi){
                Platform.runLater(() -> {
                    chessBoard.getChildren().remove(pi);
                    if(pi.getPieceType() == "king") controller.appendInfo("You lost the game");
                });

            }
        }

        this.piece[piece.getY()][piece.getX()] = null;
        this.piece[yCoord][xCoord] = piece;
        piece.setX(xCoord);
        piece.setY(yCoord);
        piece.setHasMoved();
        turn = true;
       // Converts the opponents move to FEN notation
        convertToFEN(this.piece);

        if (playerside == 1) {
            controller.appendInfo("Black " + piece.getPieceType() + " to " + convertToChar(xCoord) + (8 - yCoord));
            return;
        }
        if (playerside == 0) {
            controller.appendInfo("White " + piece.getPieceType() + " to " + convertToChar(7 - xCoord) + (yCoord + 1));
        }
    }

    // Responsible for displaying moves in the chat box
    char convertToChar(int x) {
        char out=' ';
        switch (x) {
            case (0) -> out = 'A';
            case (1) -> out = 'B';
            case (2) -> out = 'C';
            case (3) -> out = 'D';
            case (4) -> out = 'E';
            case (5) -> out = 'F';
            case (6) -> out = 'G';
            case (7) -> out = 'H';
        }
        return out;
    }

    private Rectangle getRectangle(int row, int column, GridPane gp){
        Node nd = null;
        ObservableList<Node> nds = gp.getChildren();
        for(Node node : nds){
            if(gp.getRowIndex(node) == row && gp.getColumnIndex(node) == column){
                nd = node;
                break;
            }
        }
        return (Rectangle)nd;
    }


    // Spawn pieces on board with side black or white
    private void spawnPieces(int side, int size){

        String[] sides = {"b","w"};
        QueenModel tq = new QueenModel(1-side, size, sides[1-side]+"q");
        KingModel tk = new KingModel(1-side,size, sides[1-side]+"k");
        BishopModel tb1;
        BishopModel tb2;
        RookModel tr1;
        RookModel tr2;
        KnightModel tk1;
        KnightModel tk2;
        PawnModel tp1;
        PawnModel tp2,tp3,tp4,tp5,tp6,tp7,tp8;

        PieceModel[] topPieces = new PieceModel[16];

        tb1 = new BishopModel(1-side,size, sides[1-side]+"b1");
        tb2 = new BishopModel(1-side,size, sides[1-side]+"b2");
        tr1 = new RookModel(1-side,size, sides[1-side]+"r1");
        tr2 = new RookModel(1-side,size, sides[1-side]+"r2");
        tk1 = new KnightModel(1-side,size, sides[1-side]+"k1");
        tk2 = new KnightModel(1-side,size, sides[1-side]+"k2");

        tp1 = new PawnModel(1-side,size, sides[1-side]+"p1");
        tp2 = new PawnModel(1-side,size, sides[1-side]+"p2");
        tp3 = new PawnModel(1-side,size, sides[1-side]+"p3");
        tp4 = new PawnModel(1-side,size, sides[1-side]+"p4");
        tp5 = new PawnModel(1-side,size, sides[1-side]+"p5");
        tp6 = new PawnModel(1-side,size, sides[1-side]+"p6");
        tp7 = new PawnModel(1-side,size, sides[1-side]+"p7");
        tp8 = new PawnModel(1-side,size, sides[1-side]+"p8");

        topPieces[0] = tr1;
        topPieces[1] = tk1;
        topPieces[2] = tb1;

        if(side == 1) {
            topPieces[3] = tq;
            topPieces[4] = tk;
        }
        if(side == 0){

            topPieces[3] = tk;
            topPieces[4] = tq;
        }

        topPieces[5] = tb2;
        topPieces[6] = tk2;
        topPieces[7] = tr2;
        topPieces[8] = tp1;
        topPieces[9] = tp2;
        topPieces[10] = tp3;
        topPieces[11] = tp4;
        topPieces[12] = tp5;
        topPieces[13] = tp6;
        topPieces[14] = tp7;
        topPieces[15] = tp8;


        QueenModel bq = new QueenModel(side,size, sides[side]+"q");
        KingModel bk = new KingModel(side,size, sides[side]+"k");
        BishopModel bb1;
        BishopModel bb2;
        RookModel br1;
        RookModel br2;
        KnightModel bk1;
        KnightModel bk2;
        PawnModel bp1;
        PawnModel bp2,bp3,bp4,bp5,bp6,bp7,bp8;

        PieceModel[] bottomPieces = new PieceModel[16];


        bb1 = new BishopModel(side,size, sides[side]+"b1");
        bb2 = new BishopModel(side,size, sides[side]+"b2");
        br1 = new RookModel(side,size, sides[side]+"r1");
        br2 = new RookModel(side,size, sides[side]+"r2");
        bk1 = new KnightModel(side,size, sides[side]+"k1");
        bk2 = new KnightModel(side,size, sides[side]+"k2");

        bp1 = new PawnModel(side,size, sides[side]+"p1");
        bp2 = new PawnModel(side,size, sides[side]+"p2");
        bp3 = new PawnModel(side,size, sides[side]+"p3");
        bp4 = new PawnModel(side,size, sides[side]+"p4");
        bp5 = new PawnModel(side,size, sides[side]+"p5");
        bp6 = new PawnModel(side,size, sides[side]+"p6");
        bp7 = new PawnModel(side,size, sides[side]+"p7");
        bp8 = new PawnModel(side,size, sides[side]+"p8");

        bottomPieces[0] = br1;
        bottomPieces[1] = bk1;
        bottomPieces[2] = bb1;
        if(side == 0) {
            bottomPieces[3] = bq;
            bottomPieces[4] = bk;
        }
        if(side == 1){
            bottomPieces[3] = bk;
            bottomPieces[4] = bq;
        }

        bottomPieces[5] = bb2;
        bottomPieces[6] = bk2;
        bottomPieces[7] = br2;
        bottomPieces[8] = bp1;
        bottomPieces[9] = bp2;
        bottomPieces[10] = bp3;
        bottomPieces[11] = bp4;
        bottomPieces[12] = bp5;
        bottomPieces[13] = bp6;
        bottomPieces[14] = bp7;
        bottomPieces[15] = bp8;


        for(int i =0; i<16; i++){
            setPieceListener(bottomPieces[i]);
            setPieceListener(topPieces[i]);

        }


        for(int i = 0; i<16; i++){
            chessBoard.add(topPieces[i],0,0);
            chessBoard.add(bottomPieces[i],0,0);
        }




        Platform.runLater(()-> {
            for(int i = 0; i<8; i++){
                for(int j = 0; j<8; j++){
                    Rectangle r = getRectangle(i,j, chessBoard);
                    rec[i][j] = r;
                }
            }

            int n = 0;
            for(int i = 0;i<2;i++){
                for(int j = 0; j<8; j++) {
                    move(topPieces[n], getRectangle(i, j, chessBoard));
                    piece[i][j] = topPieces[n];
                    n++;
                }
            }

            int m = 0;
            for(int i = 7;i>5;i--){
                for(int j = 7; j>-1; j--){
                    move(bottomPieces[m], getRectangle(i,j, chessBoard));
                    piece[i][j] = bottomPieces[m];
                    m++;
                }
            }
        });
        for(int i = 0; i<16;i++){
            pieces.add(bottomPieces[i]);
            pieces.add(topPieces[i]);
        }
    }

    // Get piece name
    public PieceModel getByName(String name){
        PieceModel piece = null;
        for(PieceModel p : pieces){
            if(p.getName().equals(name)) piece = p;
        }
        return piece;
    }


    // Get rectangle
    private Rectangle getRbyC(double x, double y){
        Rectangle rec = null;
        for(int i = 0; i<8; i++) {
            for (int j = 0; j < 8; j++) {
                Rectangle r = this.rec[i][j];

                if (r.localToParent(r.getX(), r.getY()).getX() == x && r.localToParent(r.getX(), r.getY()).getY() == y) {
                    rec = this.rec[i][j];
                }
            }
        }
        return rec;
    }

    // Get available moves that a piece can make
    private ArrayList<Rectangle> getAvailable (PieceModel piece){
        ArrayList<Rectangle> available = new ArrayList<>();
        String side = "";
        if(playerside == 0) side = "b";
        if(playerside == 1) side = "w";

        if(!turn || !piece.getName().startsWith(side)) return available;
        for( String s : piece.getMoves()){
            switch(s){
                case "FORWARD": {
                    int initialX = piece.getX();
                    int initialY = piece.getY();
                    for (int i = initialY - 1; i > -1; i--) {
                        PieceModel inFront = this.piece[i][initialX];
                        Rectangle next = rec[i][initialX];
                        if(inFront == null )available.add(next);
                        if(inFront != null && inFront.getName().startsWith(side)) break;
                        if (piece.getPieceType().equals("pawn")) {
                            if (piece.gethasMoved()) break;
                            if (i - initialY == -2) break;
                        }
                        if(piece.getPieceType().equals("rook") || piece.getPieceType().equals("queen")){
                            if(inFront != null && !inFront.getName().startsWith(side)){
                                available.add(next);
                                break;
                            }
                        }
                    }
                }
                break;

                case "DIAGONAL": {
                    int initialX = piece.getX();
                    int initialY = piece.getY();
                    int pX = initialX;
                    int mX = initialX;
                    if (piece.getPieceType().equals("pawn") && initialY > 1) {
                        if (initialX > 0 && this.piece[initialY - 1][initialX - 1] != null && !this.piece[initialY - 1][initialX - 1].getName().startsWith(side)) {
                            available.add(rec[initialY - 1][initialX - 1]);
                        }
                        if (initialX < 7 && this.piece[initialY - 1][initialX + 1] != null && !this.piece[initialY - 1][initialX + 1].getName().startsWith(side)) {
                            available.add(rec[initialY - 1][initialX + 1]);
                        }

                    }
                    if (piece.getPieceType().equals("bishop") || piece.getPieceType().equals("queen")) {


                        for (int i = initialY - 1; i > -1; i--) {
                            mX = mX - 1;
                            if (mX > -1 && this.piece[i][mX] == null) available.add(rec[i][mX]);
                            if (mX > -1 && this.piece[i][mX] != null && !this.piece[i][mX].getName().startsWith(side)) {
                                available.add(rec[i][mX]);
                                break;
                            }
                            if (mX > -1 && this.piece[i][mX] != null && this.piece[i][mX].getName().startsWith(side)) break;
                        }
                        mX = initialX;


                        for (int i = initialY - 1; i > -1; i--) {
                            pX = pX + 1;
                            if (pX < 8 && this.piece[i][pX] == null) available.add(rec[i][pX]);
                            if (pX < 8 && this.piece[i][pX] != null && !this.piece[i][pX].getName().startsWith(side)) {
                                available.add(rec[i][pX]);
                                break;
                            }
                            if (pX < 8 && this.piece[i][pX] != null && this.piece[i][pX].getName().startsWith(side)) break;
                        }
                        pX = initialX;


                        for (int i = initialY + 1; i < 8; i++) {
                            mX = mX - 1;
                            if (mX > -1 && this.piece[i][mX] == null) available.add(rec[i][mX]);
                            if (mX > -1 && this.piece[i][mX] != null && !this.piece[i][mX].getName().startsWith(side)) {
                                available.add(rec[i][mX]);
                                break;
                            }
                            if (mX > -1 && this.piece[i][mX] != null && this.piece[i][mX].getName().startsWith(side)) break;
                        }
                        mX = initialX;

                        for (int i = initialY + 1; i < 8; i++) {
                            pX = pX + 1;
                            if (pX < 8 && this.piece[i][pX] == null) available.add(rec[i][pX]);
                            if (pX < 8 && this.piece[i][pX] != null && !this.piece[i][pX].getName().startsWith(side)) {
                                available.add(rec[i][pX]);
                                break;
                            }
                            if (pX < 8 && this.piece[i][pX] != null && this.piece[i][pX].getName().startsWith(side)) break;
                        }
                        pX = initialX;
                    }

                }
                break;

                case "KNIGHT": {
                    int initialX = piece.getX();
                    int initialY = piece.getY();
                    if (initialX > 1 && initialY > 0){
                        if(this.piece[initialY - 1][initialX - 2] != null && !this.piece[initialY-1][initialX-2].getName().startsWith(side)) available.add(rec[initialY-1][initialX-2]);
                        if(this.piece[initialY -1][initialX -2 ] == null) available.add(rec[initialY-1][initialX-2]);
                    }

                    if (initialX > 0 && initialY > 1){
                        if(this.piece[initialY - 2][initialX - 1] != null && !this.piece[initialY-2][initialX-1].getName().startsWith(side)) available.add(rec[initialY-2][initialX-1]);
                        if(this.piece[initialY - 2][initialX - 1] == null) available.add(rec[initialY-2][initialX-1]);
                    }

                    if (initialX <7 && initialY >1){
                        if(this.piece[initialY -2][initialX +1] != null && !this.piece[initialY-2][initialX+1].getName().startsWith(side)) available.add(rec[initialY-2][initialX+1]);
                        if(this.piece[initialY -2][initialX +1] == null) available.add(rec[initialY-2][initialX+1]);
                    }
                    if (initialX <6 && initialY >0){
                        if(this.piece[initialY -1][initialX +2] != null && !this.piece[initialY-1][initialX+2].getName().startsWith(side)) available.add(rec[initialY-1][initialX+2]);
                        if(this.piece[initialY -1][initialX +2] == null) available.add(rec[initialY-1][initialX+2]);
                    }

                    if (initialX > 1 && initialY < 7){
                        if(this.piece[initialY + 1][initialX - 2] != null && !this.piece[initialY+1][initialX-2].getName().startsWith(side)) available.add(rec[initialY+1][initialX-2]);
                        if(this.piece[initialY +1][initialX -2 ] == null) available.add(rec[initialY+1][initialX-2]);
                    }
                    if (initialX > 0 && initialY < 6){
                        if(this.piece[initialY + 2][initialX - 1] != null && !this.piece[initialY+2][initialX-1].getName().startsWith(side)) available.add(rec[initialY+2][initialX-1]);
                        if(this.piece[initialY +2][initialX -1 ] == null) available.add(rec[initialY+2][initialX-1]);
                    }

                    if (initialX <7 && initialY < 6){
                        if(this.piece[initialY + 2][initialX +1] != null && !this.piece[initialY+2][initialX+1].getName().startsWith(side)) available.add(rec[initialY+2][initialX+1]);
                        if(this.piece[initialY +2][initialX +1 ] == null) available.add(rec[initialY+2][initialX+1]);
                    }
                    if (initialX <6 && initialY < 7){
                        if(this.piece[initialY + 1][initialX +2] != null && !this.piece[initialY+1][initialX+2].getName().startsWith(side)) available.add(rec[initialY+1][initialX+2]);
                        if(this.piece[initialY +1][initialX +2 ] == null) available.add(rec[initialY+1][initialX+2]);
                    }
                }
                break;

                case "SIDEWAYS AND BACKWARDS": {
                    int initialX = piece.getX();
                    int initialY = piece.getY();



                    for(int i = initialX+1;i<8;i++){
                        if(initialX >6) break;
                        PieceModel toRight = this.piece[initialY][i];
                        if(toRight != null && !toRight.getName().startsWith(side)){
                            available.add(rec[initialY][i]);
                            break;
                        }
                        if(toRight != null && toRight.getName().startsWith(side)) break;
                        if(toRight == null) available.add(rec[initialY][i]);

                    }


                    for(int i = initialX-1; i>-1; i--){
                        if(initialX<1) break;
                        PieceModel toLeft = this.piece[initialY][i];
                        if(toLeft != null && !toLeft.getName().startsWith(side)){
                            available.add(rec[initialY][i]);
                            break;
                        }
                        if(toLeft != null && toLeft.getName().startsWith(side)) break;
                        if(toLeft == null) available.add(rec[initialY][i]);
                    }


                    for(int i = initialY+1;i<8;i++){
                        if(initialY >6) break;
                        PieceModel down = this.piece[i][initialX];
                        if(down != null && !down.getName().startsWith(side)){
                            available.add(rec[i][initialX]);
                            break;
                        }
                        if(down != null && down.getName().startsWith(side)) break;
                        if(down == null) available.add(rec[i][initialX]);
                    }

                }
                break;

                case "KING":{

                    int initialX = piece.getX();
                    int initialY = piece.getY();

                    if(initialX >0 && this.piece[initialY][initialX-1] != null && !this.piece[initialY][initialX-1].getName().startsWith(side)) available.add(rec[initialY][initialX-1]);
                    if(initialY <7 && this.piece[initialY+1][initialX] != null && !this.piece[initialY+1][initialX].getName().startsWith(side)) available.add(rec[initialY+1][initialX]);
                    if(initialX <7 && this.piece[initialY][initialX+1] != null && !this.piece[initialY][initialX+1].getName().startsWith(side)) available.add(rec[initialY][initialX+1]);
                    if(initialY >0 && this.piece[initialY-1][initialX] != null && !this.piece[initialY-1][initialX].getName().startsWith(side)) available.add(rec[initialY-1][initialX]);

                    if(initialX > 0 && this.piece[initialY][initialX-1] == null) available.add(rec[initialY][initialX-1]);
                    if(initialY <7 && this.piece[initialY+1][initialX] == null) available.add(rec[initialY+1][initialX]);
                    if(initialX <7 && this.piece[initialY][initialX+1] == null) available.add(rec[initialY][initialX+1]);
                    if(initialY > 0 && this.piece[initialY-1][initialX] == null) available.add(rec[initialY-1][initialX]);


                    if(initialY >0 && initialX >0 && this.piece[initialY-1][initialX-1] != null && !this.piece[initialY-1][initialX-1].getName().startsWith(side)) available.add(rec[initialY-1][initialX-1]);
                    if(initialY >0 && initialX <7 && this.piece[initialY-1][initialX+1] != null && !this.piece[initialY-1][initialX+1].getName().startsWith(side)) available.add(rec[initialY-1][initialX+1]);
                    if(initialY <7 && initialX <7 && this.piece[initialY+1][initialX+1] != null && !this.piece[initialY+1][initialX+1].getName().startsWith(side)) available.add(rec[initialY+1][initialX+1]);
                    if(initialY <7 && initialX >0 && this.piece[initialY+1][initialX-1] != null && !this.piece[initialY+1][initialX-1].getName().startsWith(side)) available.add(rec[initialY+1][initialX-1]);

                    if(initialY >0 && initialX >0 && this.piece[initialY-1][initialX-1] == null) available.add(rec[initialY-1][initialX-1]);
                    if(initialY >0 && initialX <7 && this.piece[initialY-1][initialX+1] == null) available.add(rec[initialY-1][initialX+1]);
                    if(initialY <7 && initialX <7 && this.piece[initialY+1][initialX+1] == null) available.add(rec[initialY+1][initialX+1]);
                    if(initialY <7 && initialX >0 && this.piece[initialY+1][initialX-1] == null) available.add(rec[initialY+1][initialX-1]);


                    PieceModel rightRook = this.piece[7][7];
                    PieceModel leftRook = this.piece[7][0];
                    boolean rightClear = true;
                    boolean leftClear = true;

                    if(!piece.gethasMoved() && rightRook != null && !rightRook.gethasMoved()){
                        for(int i = piece.getX()+1; i<7; i++){
                            if(this.piece[piece.getY()][i] != null) rightClear = false;
                        }
                        if(rightClear) available.add(rec[7][piece.getX() +2]);
                    }

                    if(!piece.gethasMoved() && leftRook != null && !leftRook.gethasMoved()){
                        for(int i = piece.getX()-1; i>0; i--){
                            if(this.piece[piece.getY()][i] != null) leftClear = false;
                        }
                        if(leftClear) available.add(rec[7][piece.getX() -2]);
                    }

                }
                break;
            }

        }

        return available;
    }

}
