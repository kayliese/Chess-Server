package com.chesserver;

import javafx.scene.image.Image;

import java.util.Objects;

public class KingModel extends PieceModel {
    public KingModel(int side, int size, String name){
        setName(name);
        setSide(side);
        setSize(size);
        setPieceType("king");
        setMovable(new String[]{"KING"});
        Image img;
        if(side == 0) {
            img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("bk.png")),size,size,true,true);
        }
        else{
            img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("wk.png")),size,size,true,true);
        }
        setIv(img);
    }
}
