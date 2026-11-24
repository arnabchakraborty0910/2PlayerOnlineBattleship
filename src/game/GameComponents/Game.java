package game.GameComponents;

import game.StateMachine.StateMachine;

/*
 * this holds the logic for the game
 */
public class Game {
    public void startGame() {
        //Mile stone 1 code
        /*
        Board b = new Board();
        Cord s = new Cord(0,0), e = new Cord(0,4);
        Ship Carrier = new Ship(GameRules.CARRIER_SIZE, Ship.orientation.horizontal, "Carrier");
        Ship BattleShip = new Ship(GameRules.BATTLESHIP_SIZE, Ship.orientation.horizontal, "BattleShip");
        Ship Destroyer = new Ship(GameRules.DESTROYER_SIZE, Ship.orientation.horizontal, "Destroyer");
        Ship Submarine = new Ship(GameRules.SUBMARINE_SIZE, Ship.orientation.horizontal, "Submarine");
        Ship PatrolBoat = new Ship(GameRules.PATROAL_BOAT_SIZE, Ship.orientation.horizontal, "PatrolBoat");

        try {
            b.setShip(s, e, Carrier);

            s = new Cord(1,0); e = new Cord(1,3);
            b.setShip(s, e, BattleShip);

            s = new Cord(2,0); e = new Cord(2,2);
            b.setShip(s, e, Destroyer);

            s = new Cord(3,0); e = new Cord(3,2);
            b.setShip(s, e, Submarine);

            s = new Cord(4,0); e = new Cord(4,1);
            b.setShip(s, e, PatrolBoat);


        } catch (Exception ex) {
            ex.printStackTrace();
        }

        while(true) {
            //check hit
            String c = IO.readln("Do you want to check a cord(y/n): ");
            if(c.equals("n")){
                IO.println("Goodbye");
                return;
            }

            if(c.equals("y")) {
                int row = Integer.parseInt(IO.readln("Type in a row: "));
                int col = Integer.parseInt((IO.readln("Type in a col: ")));

                Ship g = b.checkSpot(row, col);
                if(g == null) {
                    IO.println("no ship");
                } else {
                    IO.println("Ship found: " + g.name);
                }
            }
        }

         */

        //set to board state machine
        StateMachine GameStateMacheine = new StateMachine();
        GameStateMacheine.changeState(GameStateMacheine.boardSetupState);

        while(GameStateMacheine.running) {
            GameStateMacheine.update();
        }
        
    }
}
