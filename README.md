# Tic Tac Toe game for Multiplayer

This is an draft application based on Java Swing to stimulate a multiplayer tic-tac-toe game for my practical exercise when I was learning at the FPT laboratory. The full version was done at the lab, this is just an incomplete version of the application.

This application allows player to register the name he/she wants with the server, see who is online right now, invite the player he/she wants to play with and play the game. All players communicate passively with each other through the server. The server will handle all plays, invites, the online players' list.

Please note that with this version, each player can invite with the same opponent for one time only. If you insits on playing again, there will be exceptions to be thrown.

![](https://i.imgur.com/n63Qfts.png)

![](https://i.imgur.com/q0AaNFo.png)

![](https://i.imgur.com/uuA268e.png)

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

What things you need to install to run the application.

```
Java JDK 1.8 (Java Swing included)
Netbeans IDE newest version
```

## Deployment

### Method 01: 
Open Netbeans IDE, open the project, run ```ServerApp.java``` first to make the server running, then run two ```LoginForm.java``` to stimulate 2-player tic tac toe game. You can run many ```LoginForm.java``` as you want.

## Built With

* Java JDK 1.8 - Java Swing, Java Sockets, Java Core
* Netbeans IDE

## Authors

* **Cao Huy Hoàng**

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments
* Inspiration for this app: [https://playtictactoe.org/](https://playtictactoe.org/)
