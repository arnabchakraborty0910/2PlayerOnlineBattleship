package net;

import game.GameComponents.GameRules;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientMain {
    void main() {
        try {
            Socket socket = new Socket("localhost", GameRules.DEFAULT_SERVER_PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);


            //response and call to show communication
            while(true) {
                out.println(IO.readln("Send a message: "));
                String l = in.readLine();
                IO.println("Response from server: " + l);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
