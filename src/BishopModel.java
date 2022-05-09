package com.chesserver;

import javafx.scene.image.Image;

import java.util.Objects;

public class BishopModel extends PieceModel {

    public BishopModel(int side, int size, String name){
        setName(name);
        setSide(side);
        setSize(size);
        setPieceType("bishop");
        setMovable(new String[]{"DIAGONAL"});
        Image img;
        if(side == 0) {
            img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("bb.png")),size,size,true,true);
        }
        else{
            img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("wb.png")),size,size,true,true);
        }
        setIv(img);
    }
}
