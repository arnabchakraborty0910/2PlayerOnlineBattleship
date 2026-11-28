package ui.ClientStateManager;

import java.awt.*;
import game.GameComponents.GameRules;
import ui.GamePanel;

public class GameOverClientState implements ClientState{
    ClientStateMachine clientStateMachine;
    private GamePanel panel;
    private String winnerName = "";
    private String loserName = "";

    public GameOverClientState(ClientStateMachine clientStateMachine) {
        this.clientStateMachine = clientStateMachine;
    }

    public boolean takeWinner(String line) {
        if(!line.startsWith("SEND WINNER:")) {
            return false;
        }
        winnerName = line.substring("SEND WINNER:".length()).trim();
        loserName = "";
        clientStateMachine.changeState(this);
        return true;
    }

    @Override
    public void enter() {
        panel = clientStateMachine.panel;
        panel.setGameOverMode(true);
    }

    @Override
    public void exit() {
        panel.setGameOverMode(false);
    }

    @Override
    public void onClick(Point panelPoint) {

    }

    @Override
    public boolean handleLine(String line) {
        if (line.startsWith("SEND LOSER:")) {
            loserName = line.substring("SEND LOSER:".length()).trim();
            panel.repaint();
            return true;
        }
        //the match is already over, so keep BYE off the hidden server box
        if (line.equals("BYE")) {
            return true;
        }
        return false;
    }
    @Override
    public void paint(Graphics g) {
        if (panel == null || panel.getWidth() <= 0 || panel.getHeight() <= 0) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        String[] lines = {
                "Game Over",
                "Winner: " + winnerName,
                "Loser: " + loserName
        };
        //sets the size of each line
        int size = Math.max(1, panel.getHeight() / 14);
        Font font = new Font(GameRules.FONT, Font.PLAIN, size);
        g2.setFont(font);
        FontMetrics metrics = g2.getFontMetrics();
        int limit = panel.getWidth() - 32;
        while (size > 1) {
            boolean fits = true;
            for (String text : lines) {
                if (metrics.stringWidth(text) > limit) {
                    fits = false;
                }
            }
            if (fits) {
                break;
            }
            size--;
            font = new Font(GameRules.FONT, Font.PLAIN, size);
            g2.setFont(font);
            metrics = g2.getFontMetrics();
        }
        //sets up game over button
        Rectangle button = panel.gameOverButtonBounds();
        int gap = Math.max(metrics.getHeight(), panel.getHeight() / 12);
        int block = metrics.getHeight() * lines.length + gap * (lines.length - 1);
        int top = button.y - gap - block;
        if (top < 8) {
            top = 8;
        }
        g2.setColor(GameRules.FONT_COLOR);
        int y = top + metrics.getAscent();
        for (String text : lines) {
            int x = (panel.getWidth() - metrics.stringWidth(text)) / 2;
            g2.drawString(text, x, y);
            y += metrics.getHeight() + gap;
        }
        g2.dispose();
    }

}
