/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author Hoang Cao
 */
public class PlayerAndSocketInfo {
    private String playerId;
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    
    public PlayerAndSocketInfo(String playerId, Socket socket, ObjectInputStream ois, ObjectOutputStream oos) {
        this.playerId = playerId;
        this.socket = socket;
        this.ois = ois;
        this.oos = oos;
    }
    
    public PlayerAndSocketInfo(Socket socket, ObjectInputStream ois, ObjectOutputStream oos) {
        this.socket = socket;
        this.ois = ois;
        this.oos = oos;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public ObjectInputStream getOis() {
        return ois;
    }

    public void setOis(ObjectInputStream ois) {
        this.ois = ois;
    }

    public ObjectOutputStream getOos() {
        return oos;
    }

    public void setOos(ObjectOutputStream oos) {
        this.oos = oos;
    }
    
    
    
}
