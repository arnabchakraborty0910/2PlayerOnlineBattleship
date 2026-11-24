package game.StateMachine;

import game.Player.Player;

public class StateMachine {
    private State currentState;
    public BoardSetupState boardSetupState = new BoardSetupState(this);
    public GameOverState gameOverState = new GameOverState();
    public Player1TurnState player1TurnState = new Player1TurnState();
    public Player2TurnState player2TurnState = new Player2TurnState();
    public WaitingForPlayersState waitingForPlayersState = new WaitingForPlayersState();

    //GameStateMachine
    public Player player1 = new Player();
    public Player player2 = new Player();

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
