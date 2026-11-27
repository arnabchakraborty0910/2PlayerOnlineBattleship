package net;

import game.Player.Player;
import game.StateMachine.StateMachine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ClientHandler implements Runnable {
    //holds information for each client that connects to the server
    private Socket client;
    private Player player;
    private StateMachine stateMachine;
    private BufferedReader in;
    private PrintWriter out;
    public boolean connected = true;
    private final BlockingQueue<String> inbox = new LinkedBlockingQueue<>();


    public ClientHandler(Socket s, Player p) {
        this.client = s;
        this.player = p;

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
        //this code only checks items when the client sends something
       try {
           String line;
           //checks if connected is true and whether the client sent a actual messgae
           while(connected && (line = in.readLine()) != null) {
               inbox.put(line);
           }
       } catch (Exception e) {
           IO.println(player.name  + " disconnected");
       } finally {
           connected = false;
       }



    }

    //communication methods

    //send message to client
    public void sendMessage(String message) {
        if (out != null) {
            out.println("SEND " + message);
        } else {
            connected = false;
            IO.println(player.name + " isn't connected");
        }
    }

    //asks for something from the client
    public String askMessage(String message) {
        if(out == null) {
            connected = false;
            IO.println(player.name + " isn't connected");
            return null;
        }
        out.println("ASK " + message);
        try {
            while(connected) {
                //checks every 500 milliseconds if the player has made a reponse. If no response, checks to see if player is connected. Then repeat
                String line = inbox.poll(500, TimeUnit.MILLISECONDS);
                if( line != null ) {
                    return line;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    //whne the server disconnects with the client
    public void bye() {
        if(out != null) {
            out.println("BYE");
        }
    }


    //closes the client socket
    public void close() {
        connected = false;
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //send a specific message
    public void sendRaw(String line) {
        if (out != null) {
            out.println(line);
        } else {
            connected = false;
            IO.println(player.name + " isn't connected");
        }
    }

    //returns the oldest line that THIS player has send
    //basically checks if a message has already been sent
    public String pollMessage() {
        return inbox.poll();
    }
}
