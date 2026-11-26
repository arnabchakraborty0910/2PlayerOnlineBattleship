package ui;

import game.GameComponents.GameRules;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class BoardPanel extends JPanel {
    public BoardPanel() {
        setOpaque(true);
        setBackground(GameRules.BACKGROUND_COLOR);
    }


    public static double widthInCells() {
        return GameRules.ROW_LABEL_CELLS + GameRules.BOARD_SIZE;
    }
    public static double heightInCells() {
        return GameRules.COLUMN_LABEL_CELLS + GameRules.BOARD_SIZE;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        //if window is too small, don't draw anything
        if(getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        //stops things from looking jagged(corners of grids, text, etc)
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        //size of cell
        double cell = Math.min(getWidth()/widthInCells(), getHeight() / heightInCells());

        //figures out location of grid, cell size in pixels, and how to se the grid up
        int cellPx = Math.max(1, (int) Math.floor(cell));
        int rowLabelW = (int) Math.floor(cellPx * GameRules.ROW_LABEL_CELLS);//label size for row label
        int colLabelH = (int) Math.floor(cellPx * GameRules.COLUMN_LABEL_CELLS);//label size for column label

        int gridW = rowLabelW + cellPx * GameRules.BOARD_SIZE;
        int gridH = colLabelH + cellPx * GameRules.BOARD_SIZE;
        int originX = (getWidth() - gridW) / 2;
        int originY = (getHeight() - gridH) / 2;

        g2.setColor(GameRules.FONT_COLOR);
        g2.setFont(new Font(GameRules.FONT, Font.PLAIN, Math.max(1, (int) (cellPx * 0.42))));
        FontMetrics metrics = g2.getFontMetrics();//holds the real size of the text

        //sets up columns number for label and draws it to the screen
        for (int col = 0; col < GameRules.BOARD_SIZE; col++) {
            String text = Integer.toString(col);
            int x = originX + rowLabelW + col * cellPx;
            int textX = x + (cellPx - metrics.stringWidth(text)) / 2;
            int textY = originY + (colLabelH + metrics.getAscent() - metrics.getDescent()) / 2;
            g2.drawString(text, textX, textY);
        }

        //sets up row number for label and draws it to the screen
        for (int row = 0; row < GameRules.BOARD_SIZE; row++) {
            String text = Integer.toString(row);
            int y = originY + colLabelH + row * cellPx;
            int textX = originX + (rowLabelW - metrics.stringWidth(text)) / 2;
            int textY = y + (cellPx + metrics.getAscent() - metrics.getDescent()) / 2;
            g2.drawString(text, textX, textY);
        }

        //sets up the grid itself and draws it to the screen
        for (int row = 0; row < GameRules.BOARD_SIZE; row++) {
            for (int col = 0; col < GameRules.BOARD_SIZE; col++) {
                int x = originX + rowLabelW + col * cellPx;
                int y = originY + colLabelH + row * cellPx;
                g2.setColor(Color.WHITE);
                g2.fillRect(x, y, cellPx, cellPx);
                g2.setColor(Color.BLACK);
                g2.drawRect(x, y, cellPx - 1, cellPx - 1);
            }
        }

        g2.dispose();

    }
}
