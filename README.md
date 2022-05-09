# Chess-Server-
A two player chess game

## Project Summary

The player will be able to place pieces on a specific square on the main UI in order to play. 
Additionally, there will be a chat feature for the players to communicate or discuss the game and dedicated to seeing all of the past moves from the game. 

Server:
	The server will be what communicates the game position back and forth. Any time a player makes a move, that move should be reflected on the other players screen. The server will also make the chat possible, and communicate the players messages to each other in the chat box. 

Client:
	On the client side, the client will transmit the moves that it makes to the server. It will also transmit any messages that the player types in the chat box.


Rules (for chess pieces) - legal and illegal : 
      Piece class - each chess piece (Queen, Bishop, King, Pawn, etc) extends the general piece class and within each of the chess pieces there will be a function that keeps track of the legal and illegal moves and capture that a piece can make. 

Data Structures:
	List data structure will be used specifically an array list to represent the board (8x8 two-dimensional array) each element in the array will identify what piece is occupied on a given square or if that square is empty. 
We will use a file to store the positions of the game. The file will also be used to read and write the moves that a player makes and display them on the UI 



### How do I get set up? ###

1. Download Repository and open folder in IntelliJ IDE 

2. Go to Edit configurations 

3. Make 2 Applications and name them Main and Main2

4. Under Build and run make sure that it says 'com.chesserver.Main' 

5. Click Apply and OK 

6. Run Main and put in a simple port number '123'

7. Run Main2 and just press ok 

8. In Main2 application at the top where it says IP type in '127.0.0.1' and Port type in your port number, for example '123'

9. Press Join 

10. You should be able to play a game of chess now 
