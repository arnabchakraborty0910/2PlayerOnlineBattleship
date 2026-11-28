package ui.ClientStateManager;

import java.awt.*;

public class ConnectClientState implements ClientState{
    ClientStateMachine clientStateMachine;
    public ConnectClientState(ClientStateMachine clientStateMachine) {
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

    @Override
    public boolean handleLine(String line) {
        return false;
    }

    @Override
    public void paint(Graphics g) {

    }
}
