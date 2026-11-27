package ui;

import game.GameComponents.Cord;
import game.GameComponents.GameRules;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.MouseMotionAdapter;


public class BoardPanel extends JPanel {
    private final String[][] occupants = new String[GameRules.BOARD_SIZE][GameRules.BOARD_SIZE];//the GUIs own board
    private String coverMessage;//this will display on attacker grid when setting up

    public BoardPanel() {
        setOpaque(true);
        setBackground(GameRules.BACKGROUND_COLOR);
        addMouseMotionListener(new MouseMotionAdapter() {});
    }


    public static double widthInCells() {
        return GameRules.ROW_LABEL_CELLS + GameRules.BOARD_SIZE;
    }
    public static double heightInCells() {
        return GameRules.COLUMN_LABEL_CELLS + GameRules.BOARD_SIZE;
    }

    public void setCoverMessage(String message) {
        coverMessage = message;
        repaint();
    }

    public void setOccupant(int row, int col, String name) {
        occupants[row][col] = name;
    }

    public void clearOccupants() {
        for (int row = 0; row < GameRules.BOARD_SIZE; row++) {
            for (int col = 0; col < GameRules.BOARD_SIZE; col++) {
                occupants[row][col] = null;
            }
        }
        repaint();
    }

    //tells if theres a ship here
    public String shipAt(int row, int col) {
        if (row < 0 || col < 0 || row >= GameRules.BOARD_SIZE || col >= GameRules.BOARD_SIZE) {
            return null;
        }
        return occupants[row][col];
    }

    //gets the size off a single cell
    public int cellPx() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return 0;
        }
        double cell = Math.min(getWidth() / widthInCells(), getHeight() / heightInCells());
        return Math.max(1, (int) Math.floor(cell));
    }

    // The cell under a point in this panel. Null when the point is outside the grid.
    public Cord cellAt(int localX, int localY) {
        Metrics m = metrics();
        if (m == null) {
            return null;
        }
        int col = (localX - m.originX - m.rowLabelW) / m.cell;
        int row = (localY - m.originY - m.colLabelH) / m.cell;
        if (row < 0 || col < 0 || row >= GameRules.BOARD_SIZE || col >= GameRules.BOARD_SIZE) {
            return null;
        }
        int x = m.originX + m.rowLabelW + col * m.cell;
        int y = m.originY + m.colLabelH + row * m.cell;
        if (localX < x || localY < y || localX >= x + m.cell || localY >= y + m.cell) {
            return null;
        }
        return new Cord(row, col);
    }

    //top left corner of the panels cordinates
    public Point cellOrigin(int row, int col) {
        Metrics m = metrics();
        if (m == null) {
            return new Point(0, 0);
        }
        return new Point(
                m.originX + m.rowLabelW + col * m.cell,
                m.originY + m.colLabelH + row * m.cell);
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

        //if theirs a cover message
        if (coverMessage != null) {
            paintCover(g2);
        } else {
            paintGrid(g2);
        }
        g2.dispose();


    }

    //creates cover message when the players are placing their ships
    private void paintCover(Graphics2D g2) {
        //splits the message by each line or by \n
        String[] lines = coverMessage.split("\n");
        int size = Math.max(1, getHeight() / 6);//starting height
        Font font = new Font(GameRules.FONT, Font.BOLD, size);
        g2.setFont(font);
        FontMetrics metrics = g2.getFontMetrics();

        //checks to see if the message will fit in the panel. If not, it shrinks it size
        while (size > 1) {
            boolean fits = true;
            for(String line : lines) {
                //gets width of the panel minus the 8 pixel gap between the sides of the box
                if(metrics.stringWidth(line) > getWidth() - 16) {
                    fits = false;
                }
            }

            if(fits) {
                break;
            }

            //decreases the size and starts again
            size--;
            font = new Font(GameRules.FONT, Font.BOLD, size);
            g2.setFont(font);
            metrics = g2.getFontMetrics();
        }

        //sets the text into the panel
        g2.setColor(GameRules.FONT_COLOR);
        int block = metrics.getHeight() * lines.length;
        int y = (getHeight() - block) / 2 + metrics.getAscent();
        for (String line : lines) {
            int x = (getWidth() - metrics.stringWidth(line)) / 2;
            g2.drawString(line, x, y);
            y += metrics.getHeight();
        }

    }

    private void paintGrid(Graphics2D g2) {
        Metrics m = metrics();
        if (m == null) {
            return;
        }

        g2.setColor(GameRules.FONT_COLOR);
        g2.setFont(new Font(GameRules.FONT, Font.PLAIN, Math.max(1, (int) (m.cell * 0.42))));
        FontMetrics metrics = g2.getFontMetrics();//holds the real size of the text

        //sets up columns number for label and draws it to the screen
        for (int col = 0; col < GameRules.BOARD_SIZE; col++) {
            String text = Integer.toString(col);
            int x = m.originX + m.rowLabelW + col * m.cell;
            int textX = x + (m.cell - metrics.stringWidth(text)) / 2;
            int textY = m.originY + (m.colLabelH + metrics.getAscent() - metrics.getDescent()) / 2;
            g2.drawString(text, textX, textY);
        }

        //sets up row number for label and draws it to the screen
        for (int row = 0; row < GameRules.BOARD_SIZE; row++) {
            String text = Integer.toString(row);
            int y = m.originY + m.colLabelH + row * m.cell;
            int textX = m.originX + (m.rowLabelW - metrics.stringWidth(text)) / 2;
            int textY = y + (m.cell + metrics.getAscent() - metrics.getDescent()) / 2;
            g2.drawString(text, textX, textY);
        }

        //sets up the grid itself and draws it to the screen
        for (int row = 0; row < GameRules.BOARD_SIZE; row++) {
            for (int col = 0; col < GameRules.BOARD_SIZE; col++) {
                int x = m.originX + m.rowLabelW + col * m.cell;
                int y = m.originY + m.colLabelH + row * m.cell;
                //basically checks if there's a ship at this spot and makes the cell red if there is
                if (occupants[row][col] != null) {
                    g2.setColor(GameRules.SHIP_COLOR);
                } else {
                    g2.setColor(GameRules.BACKGROUND_COLOR);
                }
                g2.fillRect(x, y, m.cell, m.cell);
                g2.setColor(Color.BLACK);
                g2.drawRect(x, y, m.cell - 1, m.cell - 1);
            }
        }

    }

    //this function is called before every function because the size of the window constantly changes so these values will change
    private Metrics metrics() {
        int cell = cellPx();//gets the size of a cell
        if (cell <= 0) {
            return null;
        }
        int rowLabelW = (int) Math.floor(cell * GameRules.ROW_LABEL_CELLS);//the size of the row labels for the grid
        int colLabelH = (int) Math.floor(cell * GameRules.COLUMN_LABEL_CELLS);//the size of the column labels for the grid
        int gridW = rowLabelW + cell * GameRules.BOARD_SIZE;//grid width
        int gridH = colLabelH + cell * GameRules.BOARD_SIZE;//grid height
        int originX = (getWidth() - gridW) / 2;//grids starting point on the x direction
        int originY = (getHeight() - gridH) / 2;//grid starting point on the y direction
        return new Metrics(cell, originX, originY, rowLabelW, colLabelH);//holds all these values
    }

    //this is a class that only holds fixed values. This is uesd to hold measurements for the whole board which is used to set up all the componets for this JPanel
    private record Metrics(int cell, int originX, int originY, int rowLabelW, int colLabelH) {
    }
}
