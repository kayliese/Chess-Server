package com.chesserver;

import javafx.scene.image.Image;

import java.util.Objects;

public class KnightModel extends PieceModel {
    public KnightModel(int side, int size, String name){
        setName(name);
        setSide(side);
        setSize(size);
        setPieceType("knight");
        setMovable(new String[]{"KNIGHT"});
        Image img;
        if(side == 0) {
            img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("bkn.png")),size,size,true,true);
        }
        else{
            img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("wkn.png")),size,size,true,true);
        }
        setIv(img);

    }

}
