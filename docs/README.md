#2Player Battleship

##Project goal
2 player battleship that can be player on 2 seperate computers. The goal is to have 2 players in 2 diffrent areas to be able to play using a server

##MVP scope
Have 3 terminals, 2 terminals will be the 2 players playing against each other. The 3rd terminal will be used to start the sever that will comunicate between the 2 players. All the logic for winning, losing, placement and players making correct moves will be managed by the server

##Furture plans
I want to add a GUI to show visually what the client sees for there board. There will also be streaks, win percentage, and leaderboards for each player that logs in and will be set to each players username.

##Tech Stack 
Java for the client, the server, and the game rules
Swing for the game window and GUI
Java sockets, with one thread per client, for networking
A JSON file, read and written with Gson, for player statistics
Git and GitHub for version control