package com.chesserver;

import javafx.scene.image.Image;

import java.util.Objects;

public class PawnModel extends PieceModel{


    public PawnModel(int side, int size, String name){
        setName(name);
        setSide(side);
        setSize(size);
        setPieceType("pawn");
        setMovable(new String[]{"FORWARD","DIAGONAL"});

        Image img;
        if(side == 0) {
            img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("bp.png")),size,size,true,true);
        }
        else{
            img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("wp.png")),size,size,true,true);
        }
        setIv(img);

    }

}