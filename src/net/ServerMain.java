package net;

import game.GameComponents.Game;
import game.GameComponents.GameRules;
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
         * Creat the statemachine to start game
         * send messages from server to clients(Edit print statements to be Print Writer object)
         * Receive messages from client(clients will have a while true loop to get as many responses as possible)
         *
         * Current goal
         * set up servers and clients
         * allow server to have multiple clients connected
         */
        ServerMain main = new ServerMain((new StateMachine()));

        main.setUpServer();
    }
    private StateMachine stateMachine;
    private Thread[] t = new Thread[GameRules.MAX_PLAYERS];

    public ServerMain(StateMachine stateMachine) {
        this.stateMachine = stateMachine; //check if game is running
    }



    public void setUpServer() {
        //

        //server connection
        ServerSocket server = null;

        //set up the game
        Game g = new Game();
        try {
            server = new ServerSocket(GameRules.DEFAULT_SERVER_PORT);

            //connect 2 clients
            int i = 0;
            while(true) {
                Socket s = server.accept();
                ClientHandler c = new ClientHandler(s);
                t[i] = new Thread(c);
                t[i].start();
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //run game
        //g.startGame();

        //closes server once complete
        if (server != null) {
            try {
                server.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



}
