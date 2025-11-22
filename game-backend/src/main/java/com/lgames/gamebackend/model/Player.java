package com.lgames.gamebackend.model;

import java.util.UUID;

public class Player {
    private String id;
    private String name;
    private PlayerColor color;

    public Player(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public PlayerColor getColor() {
        return color;
    }

    public void setColor(PlayerColor color) {
        this.color = color;
    }
}
