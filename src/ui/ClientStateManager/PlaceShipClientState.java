package ui.ClientStateManager;

import game.GameComponents.Cord;
import game.GameComponents.GameRules;
import ui.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;


public class PlaceShipClientState implements ClientState{
    ClientStateMachine clientStateMachine;

    private final LinkedHashMap<String, Placement> placed = new LinkedHashMap<>();
    private Ghost ghost;//ghost ship that moves with the cursor
    private boolean submitted;
    private boolean waitingForPlaceReply;
    private int version;//number of times board has been cleared
    private int currentVersion = -1;

    private GamePanel panel;

    public PlaceShipClientState(ClientStateMachine clientStateMachine) {
        this.clientStateMachine = clientStateMachine;
    }



    @Override
    public void enter() {
        panel = clientStateMachine.panel;
        showSetup();
    }

    @Override
    public void exit() {
        //setup done
        ghost = null;
        panel.setSetupMode(false);
        panel.attacks.setCoverMessage(null);
    }

    @Override
    public void onClick(Point panelPoint) {
//checks to make sure there is a connection, ships are already submited, and set up buttons are hidden
        if (panel.connection == null || submitted || !panel.showSetupButtons) {
            return;
        }


        Point local = SwingUtilities.convertPoint(panel, panelPoint, panel.ships);
        //checks if the click happend on the grid
        boolean inside = local.x >= 0 && local.y >= 0 && local.x < panel.ships.getWidth() && local.y < panel.ships.getHeight();
        //sets a cell if the click happened in the grid
        Cord cell = inside ? panel.ships.cellAt(local.x, local.y) : null;
        if (cell != null) {
            //checks if theres a ship there
            String name = panel.ships.shipAt(cell.row, cell.col);
            if (name != null) {
                pickUp(name);
                return;
            }
        }

        //if there's no ghost and waiting for reply, no ship is placed
        if (ghost == null || waitingForPlaceReply) {
            return;
        }

        //if cell is null, that means that this isnt a good place to put this ship
        if (cell == null) {
            panel.appendServer("This isn't a possible place to put this ship.");
            return;
        }

        //asks server if it can place a ship in those spots
        waitingForPlaceReply = true;
        currentVersion =version;
        String direction = ghost.horizontal ? "H" : "V";
        panel.connection.send("PLACE " + ghost.name + " " + cell.row + " " + cell.col + " " + direction);
    }

    @Override
    public boolean handleLine(String line) {
        if (clientStateMachine.gameOverClientState.takeWinner(line)) {
            return true;
        }
        if (line.equals("SETUP_DONE")) {
            //change state to player turn
            clientStateMachine.changeState(clientStateMachine.playerTurnClientState);
            return true;
        }
        if(line.equals("WAITING")) {
            //watiting for other player
            ghost = null;
            panel.setSetupMode(false);
            return true;
        }
        if(line.equals("CLEARED")) {
            //pressed cleared
            resetBoard();
            return true;

        }
        if(line.equals("DONE_REJECT")) {
            //
            submitted = false;
            return true;
        }
        if(line.startsWith("REJECT ")) {
            //
            waitingForPlaceReply = false;
            panel.repaint();
            return true;
        }
        if(line.startsWith("REMOVED ")) {
            //removed ship
            return true;
        }
        if((line.startsWith("PLACED "))) {
            applyPlaced(line);
            return true;
        }
        return false;
    }

    @Override
    public void paint(Graphics g) {

        //paints the ship moving with the cursor
        if (ghost == null || panel.mousePoint == null || !panel.showSetupButtons) {
            return;
        }
        int cell = panel.ships.cellPx();
        if (cell <= 0) {
            return;
        }
        Point local = SwingUtilities.convertPoint(panel, panel.mousePoint, panel.ships);
        boolean inside = local.x >= 0 && local.y >= 0 && local.x < panel.ships.getWidth() && local.y < panel.ships.getHeight();
        Cord snapped = inside ? panel.ships.cellAt(local.x, local.y) : null;
        int x;
        int y;
        if (snapped != null) {
            Point origin = panel.ships.cellOrigin(snapped.row, snapped.col);
            Point onPanel = SwingUtilities.convertPoint(panel.ships, origin, panel);
            x = onPanel.x;
            y = onPanel.y;
        } else if (ghost.horizontal) {
            x = panel.mousePoint.x;
            y = panel.mousePoint.y - cell / 2;
        } else {
            x = panel.mousePoint.x - cell / 2;
            y = panel.mousePoint.y;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        for (int i = 0; i < ghost.size; i++) {
            int sx = x + (ghost.horizontal ? i * cell : 0);
            int sy = y + (ghost.horizontal ? 0 : i * cell);
            g2.setColor(GameRules.SHIP_COLOR);
            g2.fillRect(sx, sy, cell, cell);
            g2.setColor(GameRules.FONT_COLOR);
            g2.drawRect(sx, sy, cell - 1, cell - 1);
        }
        g2.dispose();
    }

    private void showSetup() {
        submitted = false;
        panel.setSetupMode(true);
        panel.attacks.setCoverMessage("Not used when\npicking ships");
        refreshShipsPlacedButton();
    }

    //every time ship is placed
    public void onShipButton(String name) {
        //checks for server connection, is ship placed, the set up buttons are greyed out, or the client isnt waiting for a reply
        if (panel.connection == null || submitted || waitingForPlaceReply || !panel.showSetupButtons)
            return;

        //stores the current position of the mouse
        rememberMouse();

        //checks if the ship is already placed
        if(placed.containsKey(name)) {
            pickUp(name);
            return;
        }

        //otherwise ship hasent been placed so ghost is made
        ghost = new Ghost(name, GameRules.shipSize(name));
        panel.repaint();
    }

    //clearing the board
    public void onClear() {
        //add comments from here on out
        if (panel.connection == null || submitted || !panel.showSetupButtons)
            return;

        version++;
        resetBoard();
        panel.connection.send("CLEAR");
    }

    //this function picks up ships that have already been placed
    private void pickUp(String name) {
        if (panel.connection == null || submitted || waitingForPlaceReply) {
            return;
        }

        Placement placement = placed.get(name);
        if (placement == null) {
            return;
        }
        erase(placement);
        placed.remove(name);
        ghost = new Ghost(name, placement.size);
        ghost.horizontal = placement.horizontal;
        refreshShipButton(name);
        refreshShipsPlacedButton();
        panel.connection.send("REMOVE " + name);
        panel.repaint();
    }

    //this actually puts a ship onto the board when the server says it can be placed there
    private void applyPlaced(String line) {
        waitingForPlaceReply = false;
        String[] parts = line.split(" ");
        if (parts.length != 5 || currentVersion !=version) {
            panel.repaint();
            return;
        }
        try {
            String name = parts[1];
            int row = Integer.parseInt(parts[2]);
            int col = Integer.parseInt(parts[3]);
            boolean horizontal = parts[4].equals("H");
            int size = GameRules.shipSize(name);
            Placement placement = new Placement(name, row, col, horizontal, size);
            placed.put(name, placement);
            for (int i = 0; i < size; i++) {
                int r = row + (horizontal ? 0 : i);
                int c = col + (horizontal ? i : 0);
                panel.ships.setOccupant(r, c, name);
            }
            ghost = null;
            panel.ships.repaint();
            refreshShipButton(name);
            refreshShipsPlacedButton();
            panel.repaint();
        } catch (NumberFormatException e) {
            panel.repaint();
        }
    }

    //this resets the board clearing what the user has
    private void resetBoard() {
        ghost = null;
        waitingForPlaceReply = false;
        placed.clear();
        panel.ships.clearOccupants();
        panel.resetShipButtonFills();
        refreshShipsPlacedButton();
        panel.repaint();
    }
    private void erase(Placement placement) {
        for (int i = 0; i < placement.size; i++) {
            int row = placement.row + (placement.horizontal ? 0 : i);
            int col = placement.col + (placement.horizontal ? i : 0);
            panel.ships.setOccupant(row, col, null);
        }
        panel.ships.repaint();
    }

    //recolors a ship button
    private void refreshShipButton(String name) {
        if (placed.containsKey(name)) {
            panel.setShipButtonFill(name, GameRules.PLACED_BUTTON_COLOR);
        } else {
            panel.setShipButtonFill(name, GameRules.BUTTON_COLOR);
        }
    }

    //this is for the shipsPlaced button. Checks when all ships have been placed
    private void refreshShipsPlacedButton() {
        if (placed.size() == 5) {
            panel.setShipsPlacedFill(GameRules.BUTTON_COLOR);
        } else {
            panel.setShipsPlacedFill(GameRules.PLACED_BUTTON_COLOR);
        }
    }

    //code for after ship has been placed
    public void onShipsPlaced() {
        if (panel.connection == null || submitted || placed.size() != 5) {
            return;
        }
        submitted = true;
        ghost = null;
        panel.connection.send("DONE");
        panel.repaint();
    }

    public void rotate() {
        if (ghost == null) {
            return;
        }
        ghost.horizontal = !ghost.horizontal;
        panel.repaint();
    }

    //gets mouse positon
    private void rememberMouse() {
        PointerInfo info = MouseInfo.getPointerInfo();
        if (info == null) {
            return;
        }
        Point screen = info.getLocation();
        SwingUtilities.convertPointFromScreen(screen, panel);
        panel.mousePoint = screen;
    }

    private final class Ghost {
        final String name;
        final int size;
        boolean horizontal = true;
        Ghost(String name, int size) {
            this.name = name;
            this.size = size;
        }
    }
    private final class Placement {
        final String name;
        final int row;
        final int col;
        final boolean horizontal;
        final int size;
        Placement(String name, int row, int col, boolean horizontal, int size) {
            this.name = name;
            this.row = row;
            this.col = col;
            this.horizontal = horizontal;
            this.size = size;
        }
    }

}
