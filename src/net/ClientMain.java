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

            String line;
            //response and call to show communication
            while((line = in.readLine()) != null) {
                //when server says bye, the game is over
                if(line.equals("BYE")) {
                    IO.println("Game Over");
                    break;
                }

                //messages form the server
                if(line.startsWith("ASK ")) {
                    //ask something from the client
                    out.println(IO.readln(line.substring(4)));
                } else if (line.startsWith("SEND ")) {
                    //server is saying something to the client
                    IO.println(line.substring(5));
                } else {
                    //either a issue or a dirrect message from the server
                    IO.println(line);
                }


            }
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
