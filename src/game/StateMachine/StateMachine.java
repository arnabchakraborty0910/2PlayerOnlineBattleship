package game.StateMachine;

import game.GameComponents.Cord;
import game.GameComponents.Ship;
import game.Player.Player;
import net.ClientHandler;

public class StateMachine {
    //GameStateMachine
    public Player player1 = new Player("Player1");
    public Player player2 = new Player("Player2");

    //Clients. Used for communication
    public ClientHandler player1Client;
    public ClientHandler player2Client;

    private State currentState;
    public BoardSetupState boardSetupState = new BoardSetupState(this);
    public GameOverState gameOverState = new GameOverState(this);
    public PlayerTurnState player1TurnState = new PlayerTurnState(player1, player2, this);
    public PlayerTurnState player2TurnState = new PlayerTurnState(player2, player1, this);
    public WaitingForPlayersState waitingForPlayersState = new WaitingForPlayersState(this);

    public boolean running = true; //this turns false when the game stops



    public void changeState(State newState) {

        // Exit previous state
        if (currentState != null) {
            currentState.exit();
        }

        // Change state
        currentState = newState;

        // Enter new state
        currentState.enter();
    }

    public void update() {
        if (currentState != null) {
            currentState.update();
        }
    }
    //server set up
    public void setHandler(Player player, ClientHandler handler) {
        if(player == player1)
            player1Client = handler;
        else
            player2Client = handler;

    }

    //server and client communication
    private ClientHandler handlerFor(Player player) {
        if(player == player1) {
            return player1Client;
        }
        return player2Client;
    }

    public void sendMessage(Player player, String message) {
        ClientHandler handler = handlerFor(player);
        if(handler == null) {
            IO.println(message);
        } else {
            handler.sendMessage(message);
        }
    }

    public void sendMessageToBothPlayers(String message) {
        sendMessage(player1, message);
        sendMessage(player2, message);
    }

    public String getMessage(Player player, String message) {
        ClientHandler handler = handlerFor(player);
        if(handler == null) {
            return IO.readln(message);
        }
        String ans = handler.askMessage(message);
        if(ans == null) {
            running = false;
            sendMessageToBothPlayers(player.name + " disconnected. Game Over");
        }
        return ans;
    }

    public void byeBoth() {
        if (player1Client != null) {
            player1Client.bye();
        }
        if (player2Client != null) {
            player2Client.bye();
        }

    }

    //draws boards(Used in Player Turn state and Board Set Up State
    public void sendOwnBoard(Player player) {
        sendMessage(player, player.name + " board");
        sendMessage(player, "Key: _  -> empty, # -> ship, X -> hit");
        sendMessage(player, "Rows go Up to down(0 - 9), Columns go left to right(0 - 9)");
        sendMessage(player, "  0 1 2 3 4 5 6 7 8 9");
        Ship[][] board = player.board.board;
        for (int i = 0; i < board.length; i++) {
            String rowText = i + " ";
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == null) {
                    rowText += "_ ";
                } else if (board[i][j].isHit(new Cord(i, j))) {
                    rowText += "X ";
                } else {
                    rowText += "# ";
                }
            }
            sendMessage(player, rowText);
        }
    }
    public void sendGuesses(Player player, char[][] guesses, String defenderName) {
        sendMessage(player, "Attacks on " + defenderName);
        sendMessage(player, "Key: O -> not guessed, X -> hit, _ -> miss");
        sendMessage(player, "Rows go Up to down(0 - 9), Columns go left to right(0 - 9)");
        sendMessage(player, "  0 1 2 3 4 5 6 7 8 9");
        for (int i = 0; i < guesses.length; i++) {
            String rowText = i + " ";
            for (int j = 0; j < guesses[i].length; j++) {
                rowText += guesses[i][j] + " ";
            }
            sendMessage(player, rowText);
        }
    }
}
