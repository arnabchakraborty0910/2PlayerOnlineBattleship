package net;

import game.GameComponents.Game;
import game.GameComponents.GameRules;
import game.Player.Player;
import game.StateMachine.StateMachine;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
    public static void main(String args[]) {
        //server set up
        /*
         * Setup server steps
         * Set Server Socket.
         * Accept connections
         * Put each connection onto its own thread so both can run simultaneously
         * Creat the state machine to start game
         * send messages from server to clients(Edit print statements to be Print Writer object)
         * Receive messages from client
         *
         */
        ServerMain main = new ServerMain((new StateMachine()));
        main.setUpServer();
    }

    private StateMachine stateMachine;
    private Thread[] threads = new Thread[GameRules.MAX_PLAYERS];
    private ClientHandler[] clientHandlers = new ClientHandler[GameRules.MAX_PLAYERS];

    public ServerMain(StateMachine stateMachine) {
        this.stateMachine = stateMachine; //check if game is running
    }



    public void setUpServer() {
        try {
            stateMachine.changeState(stateMachine.waitingForPlayersState);
            while (stateMachine.running) {
                stateMachine.update();
            }
        } finally {
            if (stateMachine.player1Client != null) {
                stateMachine.player1Client.close();
            }
            if (stateMachine.player2Client != null) {
                stateMachine.player2Client.close();
            }
            IO.println("Server has Shut down");
        }



    }



}
