package ui.ClientStateManager;

import java.awt.*;

public class WaitingForOtherPlayerClientState implements ClientState{
    ClientStateMachine clientStateMachine;
    public WaitingForOtherPlayerClientState(ClientStateMachine clientStateMachine) {
        this.clientStateMachine = clientStateMachine;
    }

    @Override
    public void enter() {

    }

    @Override
    public void exit() {

    }

    @Override
    public void onClick(Point panelPoint) {

    }

    //set up is given to show the set up for placing ships. This code will switch the state to the appropiate stata
    @Override
    public boolean handleLine(String line) {
        if (line.equals("SETUP")) {
            clientStateMachine.changeState(clientStateMachine.placeShipClientState);
            return true;
        }
        return false;
    }

    @Override
    public void paint(Graphics g) {

    }
}
