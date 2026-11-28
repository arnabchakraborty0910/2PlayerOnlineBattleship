package ui.ClientStateManager;


import game.GameComponents.Cord;
import game.GameComponents.GameRules;
import ui.GamePanel;

import javax.swing.SwingUtilities;
import java.awt.*;

public class PlayerTurnClientState implements ClientState{
    ClientStateMachine clientStateMachine;
    private GamePanel panel;
    private boolean myTurn;
    private boolean waitingForShotReply;
    private Cord pendingShot;

    public PlayerTurnClientState(ClientStateMachine clientStateMachine) {
        this.clientStateMachine = clientStateMachine;
    }

    @Override
    public void enter() {
        panel = clientStateMachine.panel;
        myTurn = false;
        waitingForShotReply = false;
        pendingShot = null;
        panel.setPlayLegend(true);
    }

    @Override
    public void exit() {
        pendingShot = null;
        panel.setPlayLegend(false);
    }

    @Override
    public void onClick(Point panelPoint) {
        //check if player is connected
        if (panel.connection == null) {
            return;
        }

        //check if its not your turn
        if (!myTurn) {
            panel.appendServer("It is not your turn.");
            return;
        }

        //if waiting for a reply
        if (waitingForShotReply) {
            return;
        }

        //check if cell is inside grid
        Point local = SwingUtilities.convertPoint(panel, panelPoint, panel.attacks);
        boolean inside = local.x >= 0 && local.y >= 0 && local.x < panel.attacks.getWidth()  && local.y < panel.attacks.getHeight();
        Cord cell = inside ? panel.attacks.cellAt(local.x, local.y) : null;
        if (cell == null) {
            return;
        }

        //fires shot
        waitingForShotReply = true;
        pendingShot = cell;
        panel.connection.send("FIRE " + cell.row + " " + cell.col);
        panel.repaint();
    }

    @Override
    public boolean handleLine(String line) {
        if (clientStateMachine.gameOverClientState.takeWinner(line)) {
            return true;
        }
        if (line.equals("TURN")) {
            //is player turn
            myTurn = true;
            waitingForShotReply = false;
            pendingShot = null;
            panel.repaint();
            return true;
        }
        if (line.equals("WAIT")) {
            //not player turn
            myTurn = false;
            waitingForShotReply = false;
            pendingShot = null;
            panel.repaint();
            return true;
        }
        if (line.startsWith("OWN_HIT ")) {
            //other player hit own ship
            int[] cell = readCell(line);
            if (cell != null) {
                panel.ships.markHit(cell[0], cell[1]);
            }
            return true;
        }
        if (line.startsWith("SHOT HIT ")) {
            //shot was a hit
            int[] cell = readCell(line);
            if (cell != null) {
                panel.attacks.markHit(cell[0], cell[1]);
            }
            waitingForShotReply = false;
            pendingShot = null;
            panel.repaint();
            return true;
        }
        if (line.startsWith("SHOT MISS ")) {
            //shot was a miss
            int[] cell = readCell(line);
            if (cell != null) {
                panel.attacks.markMiss(cell[0], cell[1]);
            }
            waitingForShotReply = false;
            pendingShot = null;
            panel.repaint();
            return true;
        }
        if (line.equals("SHOT_REJECT")) {
            //cant shoot here
            waitingForShotReply = false;
            pendingShot = null;
            panel.repaint();
            return true;
        }
        return false;
    }

    @Override
    public void paint(Graphics g) {
        if (panel == null || !myTurn) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        if (pendingShot != null) {
            outline(g2, pendingShot);
        } else {
            outline(g2, hoveredCell());
        }
        g2.dispose();
    }

    private int[] readCell(String line) {
        //reads own hit, hit other players ship, or miss
        String[] parts = line.split(" ");
        try {
            if (parts[0].equals("OWN_HIT") && parts.length == 3) {
                return new int[]{Integer.parseInt(parts[1]), Integer.parseInt(parts[2])};
            }
            if (parts[0].equals("SHOT") && parts.length == 4) {
                return new int[]{Integer.parseInt(parts[2]), Integer.parseInt(parts[3])};
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    //cell mouse is hovering over
    private Cord hoveredCell() {
        if (panel.mousePoint == null) {
            return null;
        }
        Point local = SwingUtilities.convertPoint(panel, panel.mousePoint, panel.attacks);
        boolean inside = local.x >= 0 && local.y >= 0 && local.x < panel.attacks.getWidth() && local.y < panel.attacks.getHeight();
        if (!inside) {
            return null;
        }
        return panel.attacks.cellAt(local.x, local.y);
    }

    //outline for where the players cursor is on the board
    private void outline(Graphics2D g2, Cord cell) {
        if (cell == null) {
            return;
        }
        int cellPx = panel.attacks.cellPx();
        if (cellPx <= 0) {
            return;
        }
        Point origin = panel.attacks.cellOrigin(cell.row, cell.col);
        Point onPanel = SwingUtilities.convertPoint(panel.attacks, origin, panel);
        int inset = Math.max(2, cellPx / 8);
        g2.setColor(GameRules.FONT_COLOR);
        g2.setStroke(new BasicStroke(Math.max(2f, cellPx / 12f)));
        g2.drawRect(onPanel.x + inset, onPanel.y + inset, cellPx - inset * 2, cellPx - inset * 2);
    }
}
