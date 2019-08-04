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
public class Game {

    private ServerSocket serverSocket;
    public boolean endGame = false;
    public ArrayList<PlayerAndSocketInfo> listSocket = new ArrayList<>();
    public PlayerAndSocketInfo playerOne;
    public PlayerAndSocketInfo playerTwo;
    public ObjectOutputStream oosX;
    public ObjectOutputStream oosO;
    public PlayerAndSocketInfo currentPlayer;
    public PlayerAndSocketInfo anotherPlayer;
    public String[] board = new String[9];

    public Game(int port) {
        try {
            System.out.println("Game: open ServerSocket.");
            serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean hasWinner(String mark) {
        return (board[0].equals(mark) && board[0].equals(board[1]) && board[0].equals(board[2]))
                || (board[3].equals(mark) && board[3].equals(board[4]) && board[3].equals(board[5]))
                || (board[6].equals(mark) && board[6].equals(board[7]) && board[6].equals(board[8]))
                || (board[0].equals(mark) && board[0].equals(board[3]) && board[0].equals(board[6]))
                || (board[1].equals(mark) && board[1].equals(board[4]) && board[1].equals(board[7]))
                || (board[2].equals(mark) && board[2].equals(board[8]) && board[2].equals(board[5]))
                || (board[0].equals(mark) && board[0].equals(board[4]) && board[0].equals(board[8]))
                || (board[2].equals(mark) && board[2].equals(board[4]) && board[2].equals(board[6]));
    }

    public boolean hasBoardBeenFilledUp() {
        for (String s : board) {
            if (s.equals(".")) {
                return false;
            }
        }
        return true;
    }

    int playerNum = 1;

    public void initConnection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    System.out.println("Game: init connection.");
                    try {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("Game: accept connection.");
                        System.out.println("Check socket of player, port: " + clientSocket.getPort());
                        try {
                            ObjectOutputStream oosClient = new ObjectOutputStream(clientSocket.getOutputStream());
                            oosClient.flush();
                            ObjectInputStream oisClient = new ObjectInputStream(clientSocket.getInputStream());

                            PlayerAndSocketInfo playerTemp = new PlayerAndSocketInfo(clientSocket, oisClient, oosClient);
                            listSocket.add(playerTemp);
                            System.out.println("Game: Check size of listSocket: " + listSocket.size());
                            System.out.println("Set up DONE for player");
                        } catch (IOException ex) {
                            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                        }
//                            num = 2;
//                        }
                        System.out.println("Game: end of init connection.");

                        if (listSocket.size() == 2) {
                            playerOne = listSocket.get(0);
                            playerTwo = listSocket.get(1);
                            System.out.println("Game: Enough player.");
                            break;
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                // Create a random number to decide who is the first to play
                randomPlayer();

                // Start play REALLY
                System.out.println("Game: Start handle game!");
                handleGame();
            }

        }).start();
    }

    public void randomPlayer() {
        int randNum = (int) (Math.random() * (100 - 1 + 1) + 1);

        if ((randNum % 2) == 0) {
            try {
                // Player 1 will be the "X"
                currentPlayer = playerOne;
                anotherPlayer = playerTwo;
                oosX = playerOne.getOos();
                oosO = playerTwo.getOos();
                System.out.println("Game: Check oosX, oosO: " + oosX + " " + oosO);
                playerOne.getOos().writeUnshared(new String("X"));
                playerTwo.getOos().writeUnshared(new String("O"));
                System.out.println("Game: Random who be play first!");
            } catch (IOException ex) {
                Logger.getLogger(ServerApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                // Player 2 will be the "X"
                currentPlayer = playerTwo;
                anotherPlayer = playerOne;
                oosX = playerTwo.getOos();
                oosO = playerOne.getOos();
                System.out.println("Game: Check oosX, oosO: " + oosX + " " + oosO);
                playerTwo.getOos().writeUnshared(new String("X"));
                playerOne.getOos().writeUnshared(new String("O"));
                System.out.println("Game: Random who be play first!");
            } catch (IOException ex) {
                Logger.getLogger(ServerApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    int count500 = 0;

    public void handleGame() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Object obj = currentPlayer.getOis().readObject();
                        System.out.println("Receive the board from player.");

                        if (obj instanceof String[]) {
                            board = (String[]) obj;
                        } else if (obj instanceof Integer) {
                            int temp = (Integer) obj;

                            if (temp == 400) {
                                System.out.println("Game: stop.");
                                anotherPlayer.getOos().writeUnshared(new Integer(1000));
                                endGame = true;
//                                // TEST
//                                playerOne.getSocket().close();
//                                playerTwo.getSocket().close();
                                break;
                            }
                            if (temp == 500) {
                                count500++;
                                System.out.println("Check count500: " + count500);
                                if (count500 == 2) {
                                    randomPlayer();
                                    count500 = 0;
                                    continue;
                                }
                                PlayerAndSocketInfo temp2 = currentPlayer;
                                currentPlayer = anotherPlayer;
                                anotherPlayer = temp2;
                                continue;

                            }

                        }

                        System.out.println("Send the board to another player to update his/her board.");
                        anotherPlayer.getOos().writeUnshared(board);

                        // 100 = WIN, 80 = LOSE, 50 = TIE
                        if (hasWinner("X")) {
                            System.out.println("SERVER: X WON");
                            oosX.writeUnshared(new Integer(100));
                            oosO.writeUnshared(new Integer(80));

                        } else if (hasWinner("O")) {
                            System.out.println("SERVER: O WON");
                            oosX.writeUnshared(new Integer(80));
                            oosO.writeUnshared(new Integer(100));

                        } else if (hasBoardBeenFilledUp()) {
                            System.out.println("SERVER: TIE");
                            oosX.writeUnshared(new Integer(50));
                            oosO.writeUnshared(new Integer(50));

                        }

                        PlayerAndSocketInfo temp = currentPlayer;
                        currentPlayer = anotherPlayer;
                        anotherPlayer = temp;

                    } catch (IOException | ClassNotFoundException ex) {
                        Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
                System.out.println("Game: end HANDLE GAME.");
                try {
                    serverSocket.close();
                    playerOne.getOis().close();
                    playerTwo.getOis().close();
                    playerOne.getOos().close();
                    playerTwo.getOos().close();
                    playerOne.getSocket().close();
                    playerTwo.getSocket().close();
                } catch (IOException ex) {
                    Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

        }).start();
    }
}
