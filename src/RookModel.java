package com.chesserver;

import javafx.scene.image.Image;

import java.util.Objects;

public class RookModel extends PieceModel{
    public RookModel(int side, int size, String name){
        setName(name);
        setSide(side);
        setSize(size);
        setPieceType("rook");
        setMovable(new String[]{"FORWARD","SIDEWAYS AND BACKWARDS"});
        Image img;
        if(side == 0) {
            img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("br.png")),size,size,true,true);
        }
        else{
            img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("wr.png")),size,size,true,true);
        }
        setIv(img);

    }
}
