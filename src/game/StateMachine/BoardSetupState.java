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
        if (!stateMachine.running) {
            return;
        }
        if (!stateMachine.player1.setUp) {
            setPlayerShips(stateMachine.player1);
        } else if (!stateMachine.player2.setUp) {
            setPlayerShips(stateMachine.player2);
        } else {
            stateMachine.sendMessageToBothPlayers("Ships are placed. " + stateMachine.player1.name + " shoots first.");
            stateMachine.changeState(stateMachine.player1TurnState);
        }


    }

    @Override
    public void exit() {
    }

    private void setPlayerShips(Player p) {
        Player other = (p == stateMachine.player1) ? stateMachine.player2 : stateMachine.player1;

        if (p.Carrier == null) {
            stateMachine.sendMessage(other, "Waiting for " + p.name + " to place ships.");
            p.Carrier = placeShip(p, "Carrier", GameRules.CARRIER_SIZE);
        } else if (p.BattleShip == null) {
            p.BattleShip = placeShip(p, "BattleShip", GameRules.BATTLESHIP_SIZE);
        } else if (p.Destroyer == null) {
            p.Destroyer = placeShip(p, "Destroyer", GameRules.DESTROYER_SIZE);
        } else if (p.Submarine == null) {
            p.Submarine = placeShip(p, "Submarine", GameRules.SUBMARINE_SIZE);
        } else if (p.PatrolBoat == null) {
            p.PatrolBoat = placeShip(p, "Patrol Boat", GameRules.PATROAL_BOAT_SIZE);
        } else {
            stateMachine.sendMessage(p, "All ships placed.");
            p.setUp = true;
        }
    }

    public Ship placeShip(Player player, String name, int size) {
        stateMachine.sendMessage(player, "Creating " + name + " (size: " + size + "). Rows and columns are 0 to " + (GameRules.BOARD_SIZE - 1) + ".");
        int rowS = askInt(player, "Type in a row for start: ");
        int colS = askInt(player, "Type in a column for start: ");
        int rowE = askInt(player, "Type in a row for end: ");
        int colE = askInt(player, "Type in a column for end: ");

        String orientation = stateMachine.getMessage(player, "Orientation(h/v): ");

        if (orientation == null || rowS == -1 || colS == -1 || rowE == -1 || colE == -1) {
            return null;
        }

        Ship ship;
        if(orientation.equals("h")) {
            ship = new Ship(size, Ship.orientation.horizontal, name);
        } else {
            ship = new Ship(size, Ship.orientation.vertical, name);
        }

        Ship placed = setIndiviualShip(ship, new Cord(rowS, colS), new Cord(rowE, colE), player);
        if (placed != null) {
            stateMachine.sendOwnBoard(player);
        }
        return placed;
    }

    private int askInt(Player player, String messgae) {
        while(stateMachine.running) {
            String line = stateMachine.getMessage(player, messgae);

            if(line == null || !stateMachine.running) {
                return -1;
            }
            try {
                return Integer.parseInt(line.trim());
            } catch (Exception e) {
                stateMachine.sendMessage(player, "Type a whole number.");
            }
        }
        return -1;
    }

    private Ship setIndiviualShip(Ship s, Cord start, Cord end, Player p) {
        try{
            if(p.board.setShip(start, end, s)) {
                return s;
            }
        } catch(Exception e) {
            stateMachine.sendMessage(p, "try another cordinate");
            return null;
        }
        stateMachine.sendMessage(p, "try another cordinate");
        return null;
    }

}
