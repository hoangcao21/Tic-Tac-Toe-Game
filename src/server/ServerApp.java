/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hoang Cao
 */
public class ServerApp {

    private ArrayList<Player> listPlayers = new ArrayList<Player>();
    private ArrayList<PlayerAndSocketInfo> listPlaySockets = new ArrayList<PlayerAndSocketInfo>();
    private ArrayList<String> listPlaying = new ArrayList<String>();
    private ArrayList<PlayerAndSocketInfo> listInvites = new ArrayList<>();
    private ArrayList<Integer> listPortExist = new ArrayList<>();

    {
        listPortExist.add(3333);
        listPortExist.add(4444);
    }

    // "Join" request from the client.
    public synchronized void handleRegisterRequest() {
        Thread registerThread = new Thread(new Runnable() {
            int num = 1;

            @Override
            public void run() {
                System.out.println("Handle Register Request thread starts.");
                try {
                    // This socket is just for listening requests.
                    ServerSocket serverSocketRegister = new ServerSocket(3333);
                    ServerSocket serverSocketInvite = new ServerSocket(4444);
                    while (true) {
                        Socket socketRegister = serverSocketRegister.accept();
                        Socket socketInvite = serverSocketInvite.accept();

                        System.out.println("Register: Accept client request from " + socketRegister.getInetAddress() + ":" + socketRegister.getPort());
                        ObjectInputStream oisRegister = new ObjectInputStream(socketRegister.getInputStream());
                        String playerId = "PL" + num++;
                        Player player = new Player(playerId, (String) oisRegister.readObject());
                        System.out.println("Register: Object read success.");
                        System.out.println("Register: Check new player info, ID: " + player.getPlayerId() + ", Name: " + player.getPlayerName());

                        // Add new player to ArrayList<Player>
                        listPlayers.add(player);
                        System.out.println("Check number of players: " + listPlayers.size());

                        // Add socket to interact with new player
                        listPlaySockets.add(new PlayerAndSocketInfo(player.getPlayerId(), socketRegister, oisRegister, new ObjectOutputStream(socketRegister.getOutputStream())));
                        PlayerAndSocketInfo p = new PlayerAndSocketInfo(player.getPlayerId(), socketInvite, new ObjectInputStream(socketInvite.getInputStream()), new ObjectOutputStream(socketInvite.getOutputStream()));
                        listInvites.add(p);
                        System.out.println("Register: Check number of sockets interact with players: " + listPlaySockets.size());
                        System.out.println("Invite: Check number of sockets interact with players: " + listInvites.size());

//                         Spawn each thread for handling invitation
                        handleInvitations(p);

                    }

                } catch (IOException ex) {
                    Logger.getLogger(ServerApp.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ServerApp.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

        });
        registerThread.setName("handleRegisterRequest Thread");
        registerThread.start();
    }

    int sizeOfPlayers = 0;

    // If a new player join the game, the server write back the list of players to all clients.
    public synchronized void writePlayersListToClient() {
        Thread writePlayersListThread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Write Players List thread starts.");
                try {
                    while (true) {
                        if (listPlaySockets.size() <= 1) {
                            System.out.println("listPlaySockets doesn't have enough players (<= 1).");
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(ServerApp.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            continue;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ServerApp.class.getName()).log(Level.SEVERE, null, ex);
                        }
//                        System.out.print("sizeOfPlayers: " + sizeOfPlayers + ", listPlaySockets.size(): " + listPlaySockets.size());

                        if (sizeOfPlayers < listPlaySockets.size()) {
                            sizeOfPlayers = listPlaySockets.size();
                            for (int i = 0; i < listPlaySockets.size(); i++) {
//                                Socket socket = listPlaySockets.get(i).getSocket();
                                ObjectOutputStream oos = listPlaySockets.get(i).getOos();
                                oos.reset();
                                oos.writeObject(listPlayers);
                                System.out.println("Write the list of players back to client.");
                            }
                        }
                    }

                } catch (IOException ex) {
                    Logger.getLogger(ServerApp.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        writePlayersListThread.setName("writePlayersList Thread");
        writePlayersListThread.start();
    }

    public ObjectOutputStream findOosById(String playerId) {
        for (int i = 0; i < listInvites.size(); i++) {
            if (listInvites.get(i).getPlayerId().equals(playerId)) {
                return listInvites.get(i).getOos();
            }
        }
        return null;
    }

    public ObjectInputStream findOisById(String playerId) {
        for (int i = 0; i < listInvites.size(); i++) {
            if (listInvites.get(i).getPlayerId().equals(playerId)) {
                return listInvites.get(i).getOis();
            }
        }
        return null;
    }

    public boolean checkPlayerIsPlaying(String receiverId) {

        for (String playerId : listPlaying) {
            if (playerId.equals(receiverId)) {
                return true;
            }
        }

        return false;
    }

    public boolean isPortExist(int port) {
        for (Integer p : listPortExist) {
            if (p == port) {
                return true;
            }
        }
        return false;
    }

    public int createRandomPort() {
        while (true) {
            int port = (int) (Math.random() * (65536 - 1024 + 1) + 1024);
            if (!isPortExist(port)) {
                listPortExist.add(port);
                return port;
            }
        }
    }

    public void handleInvitations(PlayerAndSocketInfo p) {

        new Thread(new Runnable() {
//            Invitation invite;
            ObjectInputStream ois = p.getOis();

            @Override
            public void run() {

                System.out.println("Handle Invitations thread starts.");
                while (true) {
                    System.out.println("Server: inside while invitation handler.");
                    Object invit;
                    Invitation invi;
                    try {

//                        ObjectOutputStream oos = p.getOos();
                        try {
                            if ((invit = ois.readObject()) instanceof Invitation) {
                                System.out.println("THis is a invitation.");
                            }
                            System.out.println("ivnit: " + invit);
                            invi = (Invitation) invit;
                            System.out.println("Server: read invitation.");
                            System.out.println("Check Invitation, receiverId: " + invi.getReceiverId() + ", senderId: " + invi.getSenderId());
                            if (checkPlayerIsPlaying(invi.getReceiverId()) == true) {
                                System.out.println("Player is playing. Server not handle.");
                                continue;
                            }

                        } catch (ClassNotFoundException ex) {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException ex1) {
                                Logger.getLogger(ServerApp.class.getName()).log(Level.SEVERE, null, ex1);
                            }
                            System.out.println("Not receive the invitation object.");
                            continue;
                        }

                        // Send invitation to receiver and receive YES or NO from receiver to redirect to sender
                        Invitation invite = invi;

//                        while (true) {
                        ObjectOutputStream oosReceiver = null;
                        ObjectInputStream oisReceiver = null;
                        ObjectOutputStream oosSender = null;
                        ObjectInputStream oisSender = null;

                        try {
                            oosReceiver = findOosById(invi.getReceiverId());
                            oisReceiver = findOisById(invi.getReceiverId());
                            oosSender = findOosById(invi.getSenderId());

                            if (ois == oisReceiver) {
                                System.out.println("True yeah");
                            }

                            oosReceiver.reset();
                            oosReceiver.writeObject(invite.getSenderName());
                            System.out.println("Send invitation.");

                            String optionChoose = null;
                            try {
                                synchronized (oisReceiver) {
                                    ServerSocket sSocket = new ServerSocket(5555);
                                    Socket cSocket = sSocket.accept();
                                    ObjectInputStream oisC = new ObjectInputStream(cSocket.getInputStream());
                                    optionChoose = (String) oisC.readObject();
                                    sSocket.close();
                                    oisC.close();
                                    cSocket.close();
                                }
                            } catch (ClassNotFoundException ex) {
                                System.out.println("TEST EXCEPTION");
                                Logger.getLogger(ServerApp.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            System.out.println("optionChoose: " + optionChoose);

//                                 If NO
                            if (optionChoose.equals("NO PLAY")) {
                                System.out.println("NO PLAY");
                            }

                            // If YES
                            if (optionChoose.equals("PLAY")) {
                                int port = createRandomPort();
                                System.out.println("PORT create for the Game class: " + port);

                                playGame(port, invite);

                                oosSender.reset();
                                oosSender.writeObject(new Integer(port));

                                oosReceiver.reset();
                                oosReceiver.writeObject(new Integer(port));

                                listPlaying.add(invi.getReceiverId());
                                listPlaying.add(invi.getSenderId());

                            }

                        } catch (IOException ex) {
                            System.out.println("Option not choose. Wait.");
                            Logger.getLogger(ServerApp.class.getName()).log(Level.SEVERE, null, ex);
                        }
//                        }

                    } catch (IOException ex) {
                        System.err.println("This exception is thrown because of using two reference (not two unique object) ObjectInputStream on two threads cocurrently.");
                        Logger.getLogger(ServerApp.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }
        }).start();

    }

    public void playGame(int port, Invitation invi) {
        new Thread(new Runnable() {
            Game game = new Game(port);
            Invitation invit = invi;

            @Override
            public void run() {
                // Maybe bring Invitation object and delete two obj from listPlaying to notify the player finish game
                System.out.println("Server: PlayGame Thread starts.");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        game.initConnection();
                    }
                
                }).start(); 
                while (game.endGame == false) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ServerApp.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                System.out.println("game ? null ->" + game);
                game = null;
                System.out.println("game ? nullx2 ->" + game);
                System.out.println("Game finish.");
                listPlaying.remove(invit.getReceiverId());
                listPlaying.remove(invit.getSenderId());

            }

        }).start();

    }

//    public void startToPlay() {
//        new Thread(new Runnable() {
//            ArrayList<Socket> listSocket = new ArrayList<>();
//            ObjectOutputStream oosPlayer1;
//            ObjectOutputStream oosPlayer2;
//            ObjectInputStream oisPlayer1;
//            ObjectInputStream oisPlayer2;
//            String[] board;
//            int num;
//
//            @Override
//            public void run() {
//
//                while (true) {
//                    try {
//                        ServerSocket serverSocket = new ServerSocket(5555);
//                        Socket socket = serverSocket.accept();
//                        System.out.println("Accept connection.");
//                        listSocket.add(socket);
//                        if (listSocket.size() == 2) {
//                            System.out.println("Break out the loop.");
//                            break;
//                        }
//                    } catch (IOException ex) {
//                        Logger.getLogger(ServerApp.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//
//                try {
//                    // Stream for player 1
//                    oosPlayer1 = new ObjectOutputStream(listSocket.get(0).getOutputStream());
//                    oisPlayer1 = new ObjectInputStream(listSocket.get(0).getInputStream());
//
//                    // Stream for player 2
//                    oosPlayer2 = new ObjectOutputStream(listSocket.get(1).getOutputStream());
//                    oisPlayer2 = new ObjectInputStream(listSocket.get(1).getInputStream());
//                } catch (IOException ex) {
//                    Logger.getLogger(ServerApp.class.getName()).log(Level.SEVERE, null, ex);
//                }
//
//                // Random select who will play first
//                int randNum = (int) (Math.random() * (100 - 1 + 1) + 1);
//
//                if ((randNum % 2) == 0) {
//                    try {
//                        // Player 1 will be the "X"
//                        oosPlayer1.writeUnshared("X");
//                        oosPlayer2.writeUnshared("O");
//                        num = 1;
//                    } catch (IOException ex) {
//                        Logger.getLogger(ServerApp.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                } else {
//                    try {
//                        // Player 2 will be the "X"
//                        oosPlayer2.writeUnshared("X");
//                        oosPlayer1.writeUnshared("O");
//                        num = 2;
//                    } catch (IOException ex) {
//                        Logger.getLogger(ServerApp.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//
//                // isBoardFilled()
//                // whoIsWinner()
////                while (true) {
////                    board =
////                    
////                }
//            }
//
//        }).start();
//    }

    public static void main(String[] args) {
        ServerApp server = new ServerApp();
        System.out.println("Start the server.");

        // Start register request handler thread
        server.handleRegisterRequest();

        // Start write players to client thead
        server.writePlayersListToClient();

//        // Start handle invitation thread
//        server.handleInvitations();
        Thread.currentThread().getStackTrace();
    }

}

// handleRegisterRequest
//                        new Thread(new Runnable() {
////            Invitation invite;
//
//                            @Override
//                            public void run() {
//                                System.out.println("Handle Invitations thread starts.");
//                                while (true) {
//                                    System.out.println("Server: inside while invitation handler.");
//                                    Invitation invi;
//                                    try {
//                                        ObjectInputStream ois = p.getOis();
//                                        ObjectOutputStream oos = p.getOos();
//                                        try {
//                                            invi = (Invitation) ois.readObject();
//                                            System.out.println("Server: read invitation.");
//                                            System.out.println("Check Invitation, receiverId: " + invi.getReceiverId() + ", senderId: " + invi.getSenderId());
//                                            if (checkPlayerIsPlaying(invi.getReceiverId()) == true) {
//                                                System.out.println("Player is playing. Server not handle.");
//                                                oos.reset();
//                                                oos.writeInt(2);
//                                                continue;
//                                            }
//
//                                        } catch (ClassNotFoundException ex) {
//                                            try {
//                                                Thread.sleep(5000);
//                                            } catch (InterruptedException ex1) {
//                                                Logger.getLogger(ServerApp.class.getName()).log(Level.SEVERE, null, ex1);
//                                            }
//                                            System.out.println("Not receive the invitation object.");
//                                            continue;
//                                        }
//
//                                        // Send invitation to receiver and receive YES or NO from receiver to redirect to sender
//                                        Invitation invite = invi;
//
//                                        while (true) {
//                                            ObjectOutputStream oosReceiver = null;
//                                            ObjectInputStream oisReceiver = null;
//                                            ObjectOutputStream oosSender = null;
//                                            ObjectInputStream oisSender = null;
//
//                                            try {
//                                                oosReceiver = findOosById(invi.getReceiverId());
//                                                oisReceiver = findOisById(invi.getReceiverId());
//                                                oosSender = findOosById(invi.getSenderId());
//
//                                                oosReceiver.reset();
//                                                oosReceiver.writeObject(invite.getSenderName());
//                                                System.out.println("Send invitation.");
//
//                                                int optionChoose = ois.readInt();
//                                                System.out.println("optionChoose: " + optionChoose);
//
//                                                // If NO
//                                                if (optionChoose == 0) {
//                                                    oosSender.reset();
//                                                    oosSender.writeInt(0);
//                                                }
//
//                                                // If YES
//                                                if (optionChoose == 1) {
//                                                    oosSender.reset();
//                                                    oosSender.writeInt(1);
//                                                    listPlaying.add(invi.getReceiverId());
//                                                    listPlaying.add(invi.getSenderId());
//                                                }
//
//                                            } catch (IOException ex) {
//                                                System.out.println("Option not choose. Wait.");
//                                                Logger.getLogger(ServerApp.class.getName()).log(Level.SEVERE, null, ex);
//                                            }
//                                        }
//
//                                    } catch (IOException ex) {
//                                        Logger.getLogger(ServerApp.class.getName()).log(Level.SEVERE, null, ex);
//                                    }
//
//                                }
//                            }
//                        }).start();
// handleInvitations
//        new Thread(new Runnable() {
////            Invitation invite;
//            
//            @Override
//            public void run() {
//                System.out.println("Handle Invitations thread starts.");
//                while (true) {
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException ex) {
//                        Logger.getLogger(ServerApp.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                    System.out.println("Server: inside while invitation handler.");
//                    for (int i = 0; i < listInvites.size(); i++) {
//                        System.out.println("Server: inside for loop invitation handler.");
//                        Invitation invi;
//                        
////                        Socket soc = listPlaySockets.get(i).getSocket();
//
//                        try {
//                            ObjectInputStream ois = listPlaySockets.get(i).getOis();
//                            ObjectOutputStream oos = listPlaySockets.get(i).getOos();
//                            try {
//                                invi = (Invitation) ois.readObject();
//                                System.out.println("Server: read invitation.");
//                                System.out.println("Check Invitation, receiverId: " + invi.getReceiverId() + ", senderId: " + invi.getSenderId());
//                                if (checkPlayerIsPlaying(invi.getReceiverId()) == true) {
//                                    System.out.println("Player is playing. Server not handle.");
//                                    oos.reset();
//                                    oos.writeInt(2);
//                                    continue;
//                                }
//
//                            } catch (ClassNotFoundException ex) {
//                                try {
//                                    Thread.sleep(5000);
//                                } catch (InterruptedException ex1) {
//                                    Logger.getLogger(ServerApp.class.getName()).log(Level.SEVERE, null, ex1);
//                                }
//                                System.out.println("Not receive the invitation object.");
//                                continue;
//                            }
//
//                            // Send invitation to receiver and receive YES or NO from receiver to redirect to sender
//                            new Thread(new Runnable() {
//                                Invitation invite = invi;
////                                Socket socketSender = soc;
////                                Socket socketReceiver;
//
//                                @Override
//                                public void run() {
//                                    while (true) {
//                                        ObjectOutputStream oosReceiver = null;
//                                        ObjectInputStream oisReceiver = null;
//                                        ObjectOutputStream oosSender = null;
//                                        ObjectInputStream oisSender = null;
//
//                                        try {
//                                            oosReceiver = findOosById(invi.getReceiverId());
//                                            oisReceiver = findOisById(invi.getReceiverId());
//                                            oosSender = findOosById(invi.getSenderId());
//
//                                            
//                                            oosReceiver.reset();
//                                            oosReceiver.writeObject(invite.getSenderName());
//                                            System.out.println("Send invitation.");
//                                            
//                                            int optionChoose = ois.readInt();
//                                            System.out.println("optionChoose: " + optionChoose);
//
//                                            // If NO
//                                            if (optionChoose == 0) {
//                                                oosSender.reset();
//                                                oosSender.writeInt(0);
//                                            }
//
//                                            // If YES
//                                            if (optionChoose == 1) {
//                                                oosSender.reset();
//                                                oosSender.writeInt(1);
//                                                listPlaying.add(invi.getReceiverId());
//                                                listPlaying.add(invi.getSenderId());
//                                            }
//
//                                        } catch (IOException ex) {
//                                            System.out.println("Option not choose. Wait.");
//                                            Logger.getLogger(ServerApp.class.getName()).log(Level.SEVERE, null, ex);
//                                        }
//                                    }
//                                }
//
//                            }).start();
//
//                        } catch (IOException ex) {
//                            Logger.getLogger(ServerApp.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                    }
//                }
//            }
//        }).start();
