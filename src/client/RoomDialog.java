/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import server.Player;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import server.Invitation;

/**
 *
 * @author Hoang Cao
 */
public class RoomDialog extends javax.swing.JDialog {

    /**
     * Creates new form RoomDialog
     */
    JFrame parent;

    Socket socketRegister;
    Socket socketInvite;

    String playerName;
    String playerId;

    ArrayList<Player> listPlayers;
    ObjectOutputStream oosRegister;
    ObjectInputStream oisRegister;

    ObjectOutputStream oosInvite;
    ObjectInputStream oisInvite;

    public static RoomDialog getRoomDialog(Socket socketRegister, Socket socketInvite, String playerName, ObjectOutputStream oos, javax.swing.JFrame parent, boolean modal) {
        return new RoomDialog(socketRegister, socketInvite, playerName, oos, parent, modal);
    }

    private RoomDialog(Socket socketRegister, Socket socketInvite, String playerName, ObjectOutputStream oos, javax.swing.JFrame parent, boolean modal) {
        super(parent, modal);

        this.parent = parent;
        this.playerName = playerName;

        // Receive the existent socket from LoginForm
        System.out.println("RoomForm starts.");
        this.socketRegister = socketRegister;
        try {
            this.oosRegister = oos;
            this.oosRegister.flush();
            this.oisRegister = new ObjectInputStream(socketRegister.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(RoomDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Check socket: " + socketRegister.getInetAddress() + " " + socketRegister.getPort());

        initComponents();

        lbError.setVisible(false);
        lbYourName.setText("Your name: " + playerName);
        this.setTitle("Tic Tac Toe Waiting Room");

        this.socketInvite = socketInvite;
        try {
            this.oosInvite = new ObjectOutputStream(socketInvite.getOutputStream());
            this.oosInvite.flush();
            this.oisInvite = new ObjectInputStream(socketInvite.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(RoomDialog.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public synchronized void loadPlayersListFromServer() {
        Thread loadPlayersThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (stopToPlay == 0) {
                    while (true) {

                        try {
                            System.out.println("Inside while(true) load players list function");
                            listPlayers = (ArrayList<Player>) oisRegister.readObject();
                            Vector<Player> vecPlayers = new Vector<>(listPlayers);
                            System.out.println("Check how many players in client: " + vecPlayers.size());

                            if (vecPlayers.size() <= 1) {
                                System.out.println("lbError");
                                lbError.setVisible(true);
                                lbError.setText("Error: Not enough players. Please wait for the others.");
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(RoomDialog.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                continue;
                            }

                            // Append current player name on JList
                            for (int i = 0; i < vecPlayers.size(); i++) {
                                if (vecPlayers.get(i).getPlayerName().equals(playerName)) {
                                    Player temp = vecPlayers.get(i);
                                    playerId = temp.getPlayerId();
                                    System.out.println("Check current player: " + temp.getPlayerName());

                                    temp.setPlayerName(playerName + " (You)");
                                    vecPlayers.remove(temp);
                                    vecPlayers.add(0, temp);
                                    System.out.println("Check first player in vecPlayers: " + vecPlayers.get(0).getPlayerName());
                                    break;
                                }
                            }

                            // Display list players on jlistPlayers
                            jlistPlayers.setModel(new DefaultComboBoxModel(vecPlayers));
                            System.out.println("Receive the list of players from the server. Update the list of players.");
                            lbError.setText("");
//                        synchronized (this) {
//                            this.notify();
//                        }
                        } catch (IOException ex) {
                            Logger.getLogger(RoomDialog.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ClassNotFoundException ex) {
                            System.out.println("Not receive the list yet.");
                        }

                    }
                }
            }
        }
        );
        loadPlayersThread.setName("loadPlayers Thread");
        loadPlayersThread.start();

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jlistPlayers = new javax.swing.JList<>();
        btnInvite = new javax.swing.JButton();
        lbYourName = new javax.swing.JLabel();
        lbError = new javax.swing.JLabel();
        btnUpdate = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jScrollPane1.setViewportView(jlistPlayers);

        btnInvite.setText("Invite");
        btnInvite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInviteActionPerformed(evt);
            }
        });

        lbYourName.setText("Your name:");

        lbError.setText("Error: ");

        btnUpdate.setText("Auto Update");
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lbError)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnUpdate))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lbYourName)
                                    .addComponent(btnInvite))
                                .addGap(0, 197, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lbYourName)
                .addGap(7, 7, 7)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnInvite)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbError)
                    .addComponent(btnUpdate))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        btnUpdate.setEnabled(false);
        this.loadPlayersListFromServer();
        this.receiveInvitationFromServer();
//        this.errorHandler();
    }//GEN-LAST:event_btnUpdateActionPerformed

    int stopToPlay = 0;

    public synchronized void receiveInvitationFromServer() {

        System.out.println("Start receive invitation thread.");
        // Receive invitation thread
        new Thread(new Runnable() {
            Object text;
            int optionChoose = 9999;

            @Override
            public void run() {
////                try {
////                    synchronized (this) {
////                        this.wait();
////                    }
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(RoomDialog.class.getName()).log(Level.SEVERE, null, ex);
//                }
                if (stopToPlay == 0) {
                    while (true) {
                        try {
//                        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
//                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

                            try {
                                text = oisInvite.readObject();

                                System.out.println("Invitation or Response received from the server.");

                                if (text instanceof String) {
                                    System.out.println("Option Choose in RoomDiaog");
                                    optionChoose = JOptionPane.showConfirmDialog(null, "Do you want to play with " + (String) text, "Invitation", JOptionPane.YES_NO_OPTION);
                                } else if (text instanceof Integer) {
                                    int port = (Integer) text;
                                    System.out.println("Play game.");
                                    // Method to play
                                    playGame(playerName, port);
                                    continue;
                                }
                                Socket cSocket = new Socket("localhost", 5555);
                                ObjectOutputStream oosC = new ObjectOutputStream(cSocket.getOutputStream());

                                if (optionChoose == JOptionPane.YES_OPTION) {
                                    oosC.writeUnshared("PLAY");
//                                    oosInvite.reset();
//                                    oosInvite.writeObject("PLAY");
//                                    
//                                    // Display dialog to play games between 2 players.
//                                    new PlayDialog(null, true).setVisible(true);

                                }

                                if (optionChoose == JOptionPane.NO_OPTION || optionChoose == JOptionPane.CLOSED_OPTION) {
                                    oosC.writeUnshared("NO PLAY");
//                                 Send back refusal to the sender.
//                                    oosInvite.reset();
//                                    oosInvite.writeObject("NO PLAY");
                                    System.out.println("// Send back refusal to the server.");
                                }
                                oosC.close();
                                cSocket.close();
                            } catch (ClassNotFoundException ex) {
                                System.out.println("Not receive the invitation.");
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException ex1) {
                                    Logger.getLogger(RoomDialog.class.getName()).log(Level.SEVERE, null, ex1);
                                }
                                Logger.getLogger(RoomDialog.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(RoomDialog.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                }
            }

        }).start();
    }

    public void playGame(String playerName, int port) {
        System.out.println("playGame thread starts.");
        stopToPlay = 1;

        new Thread(new Runnable() {
            @Override
            public void run() {
                PlayDialog playDialog = new PlayDialog(null, true, playerName, port);
                playDialog.setVisible(true);
                System.out.println("RoomDialog: Game finish.");
                stopToPlay = 0;
                playDialog = null;
                System.out.println("PlayDialog in RoomDialog: Dispose");
            }

        }).start();
    }

//    public synchronized void errorHandler() {
//        System.out.println("Refusal or Player is ingame thread starts");
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                while (true) {
//                    try {
////                        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
//                        int ingameError = oisInvite.readInt();
//                        System.out.println("Check ingameError: " + ingameError);
//                        if (ingameError == 0) {
//                             JOptionPane.showMessageDialog(null, "Sorry, the player refused your invitation.");
//                             inviteCode = 0;
//                        }
//                        
//                        if (inviteCode == 1) {
//                            JOptionPane.showMessageDialog(null, "Sorry, the player is in game.");
//                            inviteCode = 0;
//                        }
//                    } catch (IOException ex) {
//                        Logger.getLogger(RoomDialog.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//            }
//
//        }).start();
//
//    }

    private void btnInviteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInviteActionPerformed

        try {
            oosInvite.reset();
            oosInvite.writeObject(new Invitation(playerId, playerName, jlistPlayers.getSelectedValue().getPlayerId()));
        } catch (IOException ex) {
            Logger.getLogger(RoomDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Invitation sent.");

    }//GEN-LAST:event_btnInviteActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnInvite;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList<Player> jlistPlayers;
    private javax.swing.JLabel lbError;
    private javax.swing.JLabel lbYourName;
    // End of variables declaration//GEN-END:variables
}
