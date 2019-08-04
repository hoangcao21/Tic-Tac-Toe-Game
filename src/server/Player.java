/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;


import java.io.Serializable;

/**
 *
 * @author Hoang Cao
 */
public class Player implements Serializable {
    private String playerName;
    private String playerId;

    public Player(String playerId, String playerName) {
        this.playerName = playerName;
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    
    
    @Override
    public String toString() {
        return this.playerName;
    }
}
