package com.chesserver;

import javafx.scene.image.Image;

import java.util.Objects;

public class QueenModel extends PieceModel {
    public QueenModel(int side, int size, String name){
        setName(name);
        setSide(side);
        setSize(size);
        setPieceType("queen");
        setMovable(new String[]{"FORWARD","DIAGONAL","SIDEWAYS AND BACKWARDS"});
        Image img;
        if(side == 0) {
            img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("bq2.png")),size,size,true,true);
        }
        else{
            img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("wq.png")),size,size,true,true);
        }
        setIv(img);

    }




}