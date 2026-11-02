# 2Player Battleship

2 player battleship that can be player on 2 seperate computers. 

## The Problem

Playing battle ship can be difficult, especially between people who are far away. Also, keeping track of the number of wins for each player that plays, and different streaks from games they have won previously.

## How It Works/MVP SCOPE

Have 3 terminals, 2 terminals will be the 2 players playing against each other. The 3rd terminal will be used to start the sever that will comunicate between the 2 players. All the logic for winning, losing, placement and players making correct moves will be managed by the server

![System diagram](docs/diagram.png)

## Tech Stack

- **Language:** Java for the client, the server, and the game rules
- **Libraries:** Swing for the game window and GUI. Java sockets, with one thread per client, for networking. A JSON file, read and written with Gson, for player statistics
- **External tools:** Git and GitHub for version control

## Setup

Step-by-step: how does someone who just cloned your repo get it
running?

```bash
git clone https://github.com/you/your-project
cd your-project
./install.sh   # or whatever
```

## Usage

How to actually run the thing, with examples.

```bash
./backup.sh ~/Documents
```

## Roadmap/Future updates

- [ ] I want to add a GUI to show visually what the client sees for there board.
- [ ] There will also be streaks, win percentage, and leaderboards for each player that logs in and will be set to each players username.

## Author

Arnab Chakraborty
