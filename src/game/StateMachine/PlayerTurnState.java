package game.StateMachine;

import game.GameComponents.Cord;
import game.GameComponents.GameRules;
import game.GameComponents.Ship;
import game.Player.Player;

// Player 1 is the only one allowed to fire.
public class PlayerTurnState implements State {
    private StateMachine stateMachine;
    private char[][] guesses;
    private Player attacker;
    private Player defender;
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
        if (guesses != null) {
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

        /*
         * key: guesses board
         * O -> space hasnt been guessed yet
         * X -> hit something
         * _ -> nothing was there
         *
         * key: Own board
         * _ -> empty space
         * # -> a ship takes up that spot
         * X -> ship has been hit
         */
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
        stateMachine.sendMessage(attacker, "Your turn.");

        //shows board to player
        stateMachine.sendOwnBoard(attacker);
        stateMachine.sendGuesses(attacker, guesses, defender.name);

        //asks the player for a guess
        Cord guess = new Cord(-1, -1);
        while(true) {
            try{
                stateMachine.sendMessage(attacker, "Guess a point on the board");
                guess.row = askInt(attacker, "What row would you like to guess: ");
                guess.col = askInt(attacker,"What column would you like to guess: ");

                if (guess.row == -1 || guess.col == -1) {
                    return;
                }

                //check boundaries
                if (guess.row < 0 || guess.row > GameRules.BOARD_SIZE - 1
                        || guess.col < 0 || guess.col > GameRules.BOARD_SIZE - 1) {
                    stateMachine.sendMessage(attacker, "That is not on the board. Try again");
                    continue;
                }

                //check if they have already guessed that spot
                if (guesses[guess.row][guess.col] != 'O') {
                    stateMachine.sendMessage(attacker, "You already guessed that spot. Try again");
                    continue;
                }

                break;

            } catch (Exception e) {
                IO.println("That is not whole number");
            }
        }

        //now check the guess
        Ship potentialShip = defender.board.checkSpot(guess);
        String results= "";
        if(potentialShip == null) {
            //no ship at that coordinate
            guesses[guess.row][guess.col] = '_';
            results = "\nMISS\n";
        } else {
            //hit a ship
            guesses[guess.row][guess.col] = 'X';
            potentialShip.hit(guess);
            results = "\nHIT\n";

            //check if the ship has been sunk
            if(potentialShip.sunk) {
                //ship has been sunk
                results = "\nSHIP SUNK: " + potentialShip.name + "\n";
            }
        }

        stateMachine.sendMessageToBothPlayers(attacker.name + " fired at row " + guess.row + " col " + guess.col + ": " + results);

        //check if player has won
        if(defender.checkIfPlayerLost()) {
            //attacker wins
            stateMachine.gameOverState.setWinner(attacker);
            stateMachine.changeState(stateMachine.gameOverState);
            return;
        }

        if (attacker == stateMachine.player1) {
            stateMachine.changeState(stateMachine.player2TurnState);
        } else {
            stateMachine.changeState(stateMachine.player1TurnState);
        }
    }

    @Override
    public void exit() {
    }

    private void showGuesses() {
        IO.println("Attacks on " + defender.name);
        IO.println("Key: \nO -> space hasn't been guessed yet\nX -> hit something\n_ -> nothing was there");
        for(int i = 0; i < guesses.length; i++) {
            for(int j = 0; j < guesses[i].length; j++) {
                IO.print(guesses[i][j] + " ");
            }
            IO.println("");
        }
    }

    private void showOwnBoard() {
        IO.println(attacker.name + " board");
        IO.println("Key: \n_ -> empty space\n# -> a ship takes up that spot\nX -> ship has been hit");
        Ship[][] board = attacker.board.board;
        for(int i = 0; i < board.length; i++) {
            for(int j = 0; j < board[i].length; j++) {
                if(board[i][j] == null) {
                    IO.print("_ ");
                } else if(board[i][j].isHit(new Cord(i, j))) {
                    IO.print("X ");
                } else {
                    IO.print("# ");
                }
            }
            IO.println("");
        }
    }

    private int askInt(Player player, String messgae) {
        while(stateMachine.running) {
            String line = stateMachine.getMessage(player, messgae);

            if(line == null || !stateMachine.running) {
                return -1;
            }
            try {
                return Integer.parseInt(line.trim());
            } catch (Exception e) {
                stateMachine.sendMessage(player, "Type a whole number.");
            }
        }
        return -1;
    }
}
