package game.StateMachine;

import game.GameComponents.GameRules;
import game.Player.Player;
import net.ClientHandler;

import java.net.ServerSocket;
import java.net.Socket;

// Players are joining the server. The match has not started.
public class WaitingForPlayersState implements State {
    private StateMachine stateMachine;
    private Thread[] threads = new Thread[GameRules.MAX_PLAYERS];
    private ClientHandler[] clientHandlers = new ClientHandler[GameRules.MAX_PLAYERS];

    public WaitingForPlayersState(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }
    @Override
    public void enter() {
        setUpServer();
    }

    @Override
    public void update() {
    }

    @Override
    public void exit() {
    }

    public void setUpServer() {
        //server connection
        ServerSocket server = null;

        try {
            server = new ServerSocket(GameRules.DEFAULT_SERVER_PORT);
            IO.println("Server Started");
            IO.println("Listening on port " + GameRules.DEFAULT_SERVER_PORT);

            //connect 2 clients
            for(int i = 0; i < GameRules.MAX_PLAYERS; i++) {
                IO.println("Waiting for player " + (i + 1) + "...");
                Socket client = server.accept();//accepts clients
                Player player = (i == 0) ? stateMachine.player1: stateMachine.player2;
                clientHandlers[i] = new ClientHandler(client, player);//sets the client with player and socket
                threads[i] = new Thread(clientHandlers[i]);
                threads[i].start();
                stateMachine.setHandler(player, clientHandlers[i]);
                clientHandlers[i].sendMessage("You are " + player.name + ".");
                IO.println(player.name + " connected");
            }

            //run game here
            stateMachine.changeState(stateMachine.boardSetupState);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
