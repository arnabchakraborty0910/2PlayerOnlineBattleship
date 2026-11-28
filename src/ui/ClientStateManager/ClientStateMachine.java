package ui.ClientStateManager;

import ui.GamePanel;

import java.awt.*;

public class ClientStateMachine  {
    private ClientState currentState;

    //different states
    public ConnectClientState connectClientState = new ConnectClientState(this);
    public GameOverClientState gameOverClientState = new GameOverClientState(this);
    public PlaceShipClientState placeShipClientState = new PlaceShipClientState(this);
    public PlayerTurnClientState playerTurnClientState = new PlayerTurnClientState(this);
    public WaitingForOtherPlayerClientState waitingForOtherPlayerClientState = new WaitingForOtherPlayerClientState(this);

    //game panel
    public GamePanel panel;

    public void changeState(ClientState newState) {

        // Exit previous state
        if (currentState != null) {
            currentState.exit();
        }

        // Change state
        currentState = newState;

        // Enter new state
        currentState.enter();
    }

    //these methods allow for the panel to reach the state
    public void onClick(Point panelPoint) {
        if (currentState != null) {
            currentState.onClick(panelPoint);
        }
    }
    public boolean handleLine(String line) {
        if (currentState == null) {
            return false;
        }
        return currentState.handleLine(line);
    }
    public void paint(Graphics g) {
        if (currentState != null) {
            currentState.paint(g);
        }
    }
}
