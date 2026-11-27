package game.StateMachine;

import game.GameComponents.Cord;
import game.GameComponents.GameRules;
import game.GameComponents.Ship;
import game.Player.Player;

// this will controll how the players will set up there ships at the start of each game
public class BoardSetupState implements State {
    private StateMachine stateMachine;
    private boolean leaving;


    public BoardSetupState(StateMachine s) {
        this.stateMachine = s;
    }
    @Override
    public void enter() {
        leaving = false;
        stateMachine.sendRawMessage(stateMachine.player1, "SETUP");
        stateMachine.sendRawMessage(stateMachine.player2, "SETUP");
        stateMachine.sendMessageToBothPlayers("Place your ships.");
    }

    @Override
    public void update() {
        //ask each player where they would like to put each ship
        //evaulte if they can place it there
        //then move to the next ship

        //check if the statemachine is still running and its not leaving this state
        if (!stateMachine.running || leaving) {
            return;
        }

        //check if either player has disconnected
        if (stateMachine.lostConnection(stateMachine.player1)) {
            disconnect(stateMachine.player1);
            return;
        }
        if (stateMachine.lostConnection(stateMachine.player2)) {
            disconnect(stateMachine.player2);
            return;
        }


        boolean got = false;
        got |= drain(stateMachine.player1);
        got |= drain(stateMachine.player2);
        if (!got && stateMachine.running && !leaving) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void exit() {
    }

    //checks if the player has sent a line
    private boolean drain(Player player) {
        boolean got = false;
        while (stateMachine.running && !leaving) {
            String line = stateMachine.pollMessage(player);
            if (line == null) {
                break;
            }
            got = true;
            apply(player, line);
        }
        return got;
    }

    private void apply(Player player, String message) {
        String[] parts = message.split(" ");
        if(parts.length == 0 || parts[0].isEmpty())
            return;

        if(player.setUp)//if already set up, then dont do anything
            return;

        if(parts[0].equals("PLACE")) {
            place(player, parts);
        } else if (parts[0].equals("REMOVE")) {
            remove(player, parts);
        } else if (parts[0].equals("CLEAR")) {
            clear(player);
        } else if (parts[0].equals("DONE")) {
            done(player);
        }
    }

    private void place(Player player, String[] parts) {
        if (parts.length != 5) {
            reject(player, "");
            return;
        }
        String name = parts[1];
        int size = GameRules.shipSize(name);
        int row = parse(parts[2]);
        int col = parse(parts[3]);

        boolean horizontal = parts[4].equals("H");
        //rejects player if not in right spot, getShip is null, and if its not horiznotal or vertiacl(server sends something wrong)
        if (size < 0 || row < 0 || col < 0 || getShip(player, name) != null
                || (!horizontal && !parts[4].equals("V"))) {
            reject(player, name);
            return;
        }

        Cord start = new Cord(row, col);
        Cord end = horizontal
                ? new Cord(row, col + size - 1)
                : new Cord(row + size - 1, col);

        Ship ship = new Ship(size, horizontal ? Ship.orientation.horizontal : Ship.orientation.vertical, name);

        try {
            if(!player.board.setShip(start, end, ship)) {
                reject(player, name);
                return;
            }
        } catch (Exception e) {
            reject(player, name);
            return;
        }

        setShip(player, name, ship);
        stateMachine.sendRawMessage(player, "PLACED " + name + " " + row + " " + col + " " + parts[4]);
        stateMachine.sendMessage(player, GameRules.shipLabel(name) + " placed.");

    }

    private void remove(Player player, String[] parts) {
        if(parts.length != 2)
            return;

        String name = parts[1];
        Ship ship = getShip(player, name);
        if(ship != null) {
            player.board.removeShip(ship);
            setShip(player, name, null);
        }

        stateMachine.sendRawMessage(player, "REMOVED " + name);
        stateMachine.sendMessage(player, GameRules.shipLabel(name) + " removed.");
    }

    private void clear(Player player) {
        player.board.clearShips();
        player.Carrier = null;
        player.BattleShip = null;
        player.Destroyer = null;
        player.Submarine = null;
        player.PatrolBoat = null;
        player.setUp = false;
        stateMachine.sendRawMessage(player, "CLEARED");
        stateMachine.sendMessage(player, "Ships cleared.");
    }

    private void done(Player player) {
        //if player is done setting up there ships
        if (player.Carrier == null || player.BattleShip == null || player.Destroyer == null || player.Submarine == null || player.PatrolBoat == null) {
            stateMachine.sendRawMessage(player, "DONE_REJECT");
            stateMachine.sendMessage(player, "Place all 5 ships before pressing Ships Placed.");
            return;
        }
        player.setUp = true;
        Player other = (player == stateMachine.player1) ?  stateMachine.player2 : stateMachine.player1;
        //if the other player isnt set up
        if(!other.setUp) {
            stateMachine.sendRawMessage(player, "WAITING");
            stateMachine.sendMessage(player, "Waiting for " + other.name + " to finish placing ships.");
            return;
        }
        finish();
    }

    private void finish() {
        stateMachine.sendRawMessage(stateMachine.player1, "SETUP_DONE");
        stateMachine.sendRawMessage(stateMachine.player2, "SETUP_DONE");
        stateMachine.sendMessageToBothPlayers(
                "Ships are placed. " + stateMachine.player1.name + " shoots first.");
        leaving = true;
        stateMachine.changeState(stateMachine.player1TurnState);
    }

    private void reject(Player player, String name) {
        stateMachine.sendRawMessage(player, "REJECT " + name);
        stateMachine.sendMessage(player, "This isn't a possible place to put this ship.");
    }

    private void disconnect(Player player) {
        stateMachine.running = false;
        stateMachine.sendMessageToBothPlayers(player.name + " disconnected. Game Over");
        stateMachine.byeBoth();
    }

    private int parse(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private Ship getShip(Player player, String name) {
        if (name.equals("Carrier")) return player.Carrier;
        if (name.equals("BattleShip")) return player.BattleShip;
        if (name.equals("Destroyer")) return player.Destroyer;
        if (name.equals("Submarine")) return player.Submarine;
        if (name.equals("PatrolBoat")) return player.PatrolBoat;
        return null;
    }

    private void setShip(Player player, String name, Ship ship) {
        if (name.equals("Carrier")) player.Carrier = ship;
        else if (name.equals("BattleShip")) player.BattleShip = ship;
        else if (name.equals("Destroyer")) player.Destroyer = ship;
        else if (name.equals("Submarine")) player.Submarine = ship;
        else if (name.equals("PatrolBoat")) player.PatrolBoat = ship;
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
