# Chess-Server

Chess-Server is a two-player chess game that allows players to make moves and communicate with each other through a chat feature.

## Project Summary

The game allows players to place pieces on the board using the main UI. The server communicates the game position between the players and updates the UI with each move. The chat feature allows players to communicate or discuss the game with each other.

### Server

The server is responsible for communicating the game position between the players. Whenever a player makes a move, the server updates the board and sends the new position to the other player. The server also handles the chat feature by transmitting the players' messages to each other.

### Client

The client sends moves and chat messages to the server. It also updates the UI with the new position of the board after each move.

### Rules

Each chess piece (Queen, Bishop, King, Pawn, etc) extends the general Piece class, which keeps track of the legal and illegal moves that a piece can make. The game uses a list data structure, specifically a two-dimensional array, to represent the board. Each element in the array identifies the piece on a given square or if the square is empty.

### How to Play

1. Download the repository and open the folder in IntelliJ IDE.
2. Create two applications named "Main" and "Main2" in the "Edit configurations" menu.
3. Make sure "com.chesserver.Main" is selected under "Build and run".
4. Run "Main" and input a simple port number, e.g., "123".
5. Run "Main2" and press "OK".
6. In the "Main2" application, enter "127.0.0.1" for IP and the port number entered in step 4 (e.g., "123") for Port.
7. Press "Join".
8. Start playing!

## User Interface

![Chess-Server UI](https://user-images.githubusercontent.com/49692061/180057176-87f28f09-03b1-4cf5-8a66-d2a1317c4473.png)

## Dependencies

This project was developed using Java and IntelliJ IDE.

## Conclusion

Chess-Server is a simple and fun way to play chess with friends online. It demonstrates the use of Java to create a networked multiplayer game with a graphical user interface.

