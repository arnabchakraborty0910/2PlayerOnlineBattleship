package game.StateMachine;

import game.Player.Player;

public class StateMachine {
    //GameStateMachine
    public Player player1 = new Player("Player1");
    public Player player2 = new Player("Player2");

    private State currentState;
    public BoardSetupState boardSetupState = new BoardSetupState(this);
    public GameOverState gameOverState = new GameOverState(this);
    public PlayerTurnState player1TurnState = new PlayerTurnState(player1, player2, this);
    public PlayerTurnState player2TurnState = new PlayerTurnState(player2, player1, this);
    public WaitingForPlayersState waitingForPlayersState = new WaitingForPlayersState();

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
}
