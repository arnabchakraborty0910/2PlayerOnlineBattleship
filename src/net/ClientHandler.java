package net;

import game.Player.Player;
import game.StateMachine.StateMachine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    //holds information for each client that connects to the server
    private Socket client;
    private Player player;
    private StateMachine stateMachine;
    private BufferedReader in;
    private PrintWriter out;


    public ClientHandler(Socket s) {
        this.client = s;

        //set up communication between player and server
        try {
             in = new BufferedReader(new InputStreamReader(client.getInputStream()));
             out = new PrintWriter(client.getOutputStream(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        //start player code

        while(true) {
            //constantly check if its the players turn and what the player needs to do

            //Check if its there turn(boolean on the player)
            //execute there turn(take the code from the Player Turn State and run it here or statemahcine.update)

            //for now, check the lines sent by the client and respond to it
            try {
                String l = in.readLine();
                IO.println("Response from " + client + ": " + l);
                String response = IO.readln("Send a response: ");
                out.println(response);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        }
    }
}
