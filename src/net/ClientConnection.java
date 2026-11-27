package net;

import game.GameComponents.GameRules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

// This is the class that handles the client taking to the server
public class ClientConnection {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final Consumer<String> onLine;

    //sets up client connection to server
    public ClientConnection(Consumer<String> onLine) throws IOException {
        this.onLine = onLine;
        socket = new Socket("localhost", GameRules.DEFAULT_SERVER_PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        Thread reader = new Thread(this::readLoop);//This basically creates a seperate thread that just checks to see if the server has send anything
        reader.setDaemon(true);//makes this thread run in the background
        reader.start();//starts the thread
    }

    //sends message to server
    public void send(String line) {
        out.println(line);
    }

    //runs when the server closes or the client want to close the connection
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //reads response from the server
    private void readLoop() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                onLine.accept(line);
            }
        } catch (IOException e) {
            onLine.accept("BYE");
        }
    }
}