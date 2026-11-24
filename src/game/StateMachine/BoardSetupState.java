package game.StateMachine;

import game.GameComponents.Cord;
import game.GameComponents.GameRules;
import game.GameComponents.Ship;
import game.Player.Player;

// this will controll how the players will set up there ships at the start of each game
public class BoardSetupState implements State {
    private StateMachine stateMachine;

    public BoardSetupState(StateMachine s) {
        this.stateMachine = s;
    }
    @Override
    public void enter() {
        //do nothing right now
        //later will have networking code to see if theres 2 clients
    }

    @Override
    public void update() {
        //ask each player where they would like to put each ship
        //evaulte if they can place it there
        //then move to the next ship
        if(!stateMachine.player1.setUp)
            setPlayerShips(stateMachine.player1);
        else if (!stateMachine.player2.setUp)
            setPlayerShips(stateMachine.player2);
        else {
            IO.print("done move to player 1 turn");
            stateMachine.changeState(stateMachine.player1TurnState);
        }


    }

    @Override
    public void exit() {
    }

    private void setPlayerShips(Player p) {
        if(p.Carrier == null) {
            IO.println("Creating Carrier\n");
            //cords for start and end
            int rowS = Integer.parseInt(IO.readln("Type in a row for start: "));
            int colS = Integer.parseInt((IO.readln("Type in a col for start: ")));

            int rowE = Integer.parseInt(IO.readln("Type in a row for end: "));
            int colE = Integer.parseInt((IO.readln("Type in a col for end: ")));
            String orientation = IO.readln("Orientation(h/v): ");
            Cord s = new Cord(rowS, colS);
            Cord e = new Cord(rowE, colE);

            //sets the potential ship
            Ship ship = null;
            if(orientation.equals("h"))
                ship = new Ship(GameRules.CARRIER_SIZE, Ship.orientation.horizontal, "Carrier");
            else
                ship = new Ship(GameRules.CARRIER_SIZE, Ship.orientation.vertical, "Carrier");

            //check with board to see if this ship can be placed at these cords
            p.Carrier = setIndiviualShip(ship, s, e, p);

        } else if(p.BattleShip == null) {
            IO.println("Creating BattleShip\n");
            //cords for start and end
            int rowS = Integer.parseInt(IO.readln("Type in a row for start: "));
            int colS = Integer.parseInt((IO.readln("Type in a col for start: ")));

            int rowE = Integer.parseInt(IO.readln("Type in a row for end: "));
            int colE = Integer.parseInt((IO.readln("Type in a col for end: ")));
            String orientation = IO.readln("Orientation(h/v): ");
            Cord s = new Cord(rowS, colS);
            Cord e = new Cord(rowE, colE);

            //sets the potential ship
            Ship ship = null;
            if(orientation.equals("h"))
                ship = new Ship(GameRules.BATTLESHIP_SIZE, Ship.orientation.horizontal, "BattleShip");
            else
                ship = new Ship(GameRules.BATTLESHIP_SIZE, Ship.orientation.vertical, "BattleShip");

            //check with board to see if this ship can be placed at these cords
            p.BattleShip = setIndiviualShip(ship, s, e, p);

        } else if(p.Destroyer == null) {
            IO.println("Creating Destroyer\n");
            //cords for start and end
            int rowS = Integer.parseInt(IO.readln("Type in a row for start: "));
            int colS = Integer.parseInt((IO.readln("Type in a col for start: ")));

            int rowE = Integer.parseInt(IO.readln("Type in a row for end: "));
            int colE = Integer.parseInt((IO.readln("Type in a col for end: ")));
            String orientation = IO.readln("Orientation(h/v): ");
            Cord s = new Cord(rowS, colS);
            Cord e = new Cord(rowE, colE);

            //sets the potential ship
            Ship ship = null;
            if(orientation.equals("h"))
                ship = new Ship(GameRules.DESTROYER_SIZE, Ship.orientation.horizontal, "Destroyer");
            else
                ship = new Ship(GameRules.DESTROYER_SIZE, Ship.orientation.vertical, "Destroyer");

            //check with board to see if this ship can be placed at these cords
            p.Destroyer = setIndiviualShip(ship, s, e, p);

        } else if(p.Submarine == null) {
            IO.println("Creating Submarine\n");
            //cords for start and end
            int rowS = Integer.parseInt(IO.readln("Type in a row for start: "));
            int colS = Integer.parseInt((IO.readln("Type in a col for start: ")));

            int rowE = Integer.parseInt(IO.readln("Type in a row for end: "));
            int colE = Integer.parseInt((IO.readln("Type in a col for end: ")));
            String orientation = IO.readln("Orientation(h/v): ");
            Cord s = new Cord(rowS, colS);
            Cord e = new Cord(rowE, colE);

            //sets the potential ship
            Ship ship = null;
            if(orientation.equals("h"))
                ship = new Ship(GameRules.SUBMARINE_SIZE, Ship.orientation.horizontal, "Submarine");
            else
                ship = new Ship(GameRules.SUBMARINE_SIZE, Ship.orientation.vertical, "Submarine");

            //check with board to see if this ship can be placed at these cords
            p.Submarine = setIndiviualShip(ship, s, e, p);

        } else if(p.PatrolBoat == null) {
            IO.println("Creating PatrolBoat\n");
            //cords for start and end
            int rowS = Integer.parseInt(IO.readln("Type in a row for start: "));
            int colS = Integer.parseInt((IO.readln("Type in a col for start: ")));

            int rowE = Integer.parseInt(IO.readln("Type in a row for end: "));
            int colE = Integer.parseInt((IO.readln("Type in a col for end: ")));
            String orientation = IO.readln("Orientation(h/v): ");
            Cord s = new Cord(rowS, colS);
            Cord e = new Cord(rowE, colE);

            //sets the potential ship
            Ship ship = null;
            if(orientation.equals("h"))
                ship = new Ship(GameRules.PATROAL_BOAT_SIZE, Ship.orientation.horizontal, "Patrol Boat");
            else
                ship = new Ship(GameRules.PATROAL_BOAT_SIZE, Ship.orientation.vertical, "Patrol Boat");

            //check with board to see if this ship can be placed at these cords
            p.PatrolBoat = setIndiviualShip(ship, s, e, p);

        } else {
            IO.println("Complete");
            p.setUp = true;

        }
    }

    private Ship setIndiviualShip(Ship s, Cord start, Cord end, Player p) {
        try{
            if(p.board.setShip(start, end, s)) {
                return s;
            }
        } catch(Exception e) {
            IO.println("try another cordinate");
            return null;
        }
        IO.println("try another cordinate");
        return null;
    }

}
