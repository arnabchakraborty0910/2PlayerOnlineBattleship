package game.StateMachine;

import game.GameComponents.Cord;
import game.GameComponents.GameRules;
import game.GameComponents.Ship;
import game.Player.Player;

// Player 1 is the only one allowed to fire.
public class PlayerTurnState implements State {
    private StateMachine stateMachine;
    private char[][] guesses;//check if this is needed
    private Player attacker;
    private Player defender;
    private boolean leaving;

    public PlayerTurnState(Player attacker, Player defender, StateMachine stateMachine) {
        //sets the attacker and defenders
        this.attacker = attacker;
        this.defender = defender;

        //sets the stateMachine
        this.stateMachine = stateMachine;
    }
    @Override
    public void enter() {
        //if guesses board has already been made, don't do anything
        leaving = false;
        if (guesses != null) {
            announceTurn();
            return;
        }

        //sets the board of guess for the attacker
        guesses = new char[GameRules.BOARD_SIZE][GameRules.BOARD_SIZE];

        //sets the board the user sees to
        for(int i = 0; i < guesses.length; i++) {
            for(int j = 0; j < guesses[i].length; j++) {
                guesses[i][j] = 'O';
            }
        }

        announceTurn();
    }

    @Override
    public void update() {
        //ask the user where they would like to attack
        //show there board of guesses and the enemy board
        //ask for a guess
        //see if they hit or miss
        //send to next player

        if (!stateMachine.running) {
            return;
        }

        //check if attacker and deffender are still connected
        if (stateMachine.lostConnection(attacker)) {
            disconnect(attacker);
            return;
        }
        if (stateMachine.lostConnection(defender)) {
            disconnect(defender);
            return;
        }

        boolean got = false;
        if (drain(attacker)) {
            got = true;//this takes out any messages from the attacker and handles it. If there is a message return true
        }
        if (drain(defender)) {
            got = true;//this takes out any messages from the defender and handles it. If there is a message return true
        }
        if (!got && stateMachine.running && !leaving) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void exit() {

    }

    // Runs every time this player's turn starts,
    private void announceTurn() {
        sendOwnHits();
        stateMachine.sendRawMessage(attacker, "TURN");
        stateMachine.sendMessage(attacker, "Your turn.");
        stateMachine.sendRawMessage(defender, "WAIT");
        stateMachine.sendMessage(defender, "It is " + attacker.name + "'s turn.");
    }

    // Gives all the hits for the defender. Runs when this player starts his turn
    private void sendOwnHits() {
        Ship[][] board = attacker.board.board;
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                Ship ship = board[row][col];
                if (ship != null && ship.isHit(new Cord(row, col))) {
                    stateMachine.sendRawMessage(attacker, "OWN_HIT " + row + " " + col);
                }
            }
        }
    }

    private boolean drain(Player player) {
        boolean got = false;
        while (stateMachine.running && !leaving) {
            String line = stateMachine.pollMessage(player);
            if (line == null) {
                break;
            }
            got = true;
            apply(player, line);
        }
        return got;
    }

    private void apply(Player player, String line) {
        String[] parts = line.split(" ");
        //checks for right command at start
        if (parts.length == 0 || parts[0].isEmpty() || !parts[0].equals("FIRE")) {
            return;
        }

        //checks if player is attacker
        if (player != attacker) {
            stateMachine.sendMessage(player, "It is not your turn.");
            return;
        }

        //checks if the message is the right length
        if (parts.length != 3) {
            rejectShot("Click a cell on the Attacks board.");
            return;
        }

        //checks if position is on the baord
        int row = parse(parts[1]);
        int col = parse(parts[2]);
        if (row < 0 || row >= GameRules.BOARD_SIZE || col < 0 || col >= GameRules.BOARD_SIZE) {
            rejectShot("That is not on the board. Try again.");
            return;
        }

        //checks if spot has already been guessed
        if (guesses[row][col] != 'O') {
            rejectShot("You already guessed that spot. Try again.");
            return;
        }

        //checks if there's a ship on that spot
        Cord guess = new Cord(row, col);
        Ship potentialShip = defender.board.checkSpot(guess);
        String results;
        if (potentialShip == null) {
            guesses[row][col] = '_';
            stateMachine.sendRawMessage(attacker, "SHOT MISS " + row + " " + col);
            results = "MISS";
        } else {
            guesses[row][col] = 'X';
            potentialShip.hit(guess);
            stateMachine.sendRawMessage(attacker, "SHOT HIT " + row + " " + col);
            results = "HIT";
            if (potentialShip.sunk) {
                results = "SHIP SUNK: " + potentialShip.name;
            }
        }
        stateMachine.sendMessageToBothPlayers(attacker.name + " fired at row " + row + " col " + col + ": " + results);

        //check if the defender has lost
        if (defender.checkIfPlayerLost()) {
            leaving = true;
            stateMachine.gameOverState.setWinner(attacker);
            stateMachine.changeState(stateMachine.gameOverState);
            return;
        }
        leaving = true;
        if (attacker == stateMachine.player1) {
            stateMachine.changeState(stateMachine.player2TurnState);
        } else {
            stateMachine.changeState(stateMachine.player1TurnState);
        }
    }

    //handles rejected shots
    private void rejectShot(String reason) {
        stateMachine.sendRawMessage(attacker, "SHOT_REJECT");
        stateMachine.sendMessage(attacker, reason);
    }

    //handles player disconecting
    private void disconnect(Player player) {
        leaving = true;
        Player winner = (player == stateMachine.player1) ? stateMachine.player2 : stateMachine.player1;
        stateMachine.gameOverState.setWinner(winner);
        stateMachine.changeState(stateMachine.gameOverState);
    }

    //converts texts to ints
    private int parse(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
