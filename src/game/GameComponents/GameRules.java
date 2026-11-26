package game.GameComponents;

import java.awt.*;

/*
 * defines the size of the board and the size of the ships, and number of players
 */
public class GameRules {
    public static final int BOARD_SIZE = 10;
    public static final int CARRIER_SIZE = 5;
    public static final int BATTLESHIP_SIZE = 4;
    public static final int DESTROYER_SIZE = 3;
    public static final int SUBMARINE_SIZE = 3;
    public static final int PATROAL_BOAT_SIZE = 2;
    public static final int MAX_PLAYERS = 2;
    public static final int DEFAULT_SERVER_PORT = 5050;

    //GUI constants
    public static final int MIN_WINDOW_SIZE = 700;
    public static final Color BACKGROUND_COLOR = Color.WHITE;
    public static final Color FONT_COLOR = Color.BLACK;
    public static final String FONT = Font.SANS_SERIF;

    public static final double ROW_LABEL_CELLS = 0.9;
    public static final double COLUMN_LABEL_CELLS = 0.75;

    //these are in terms of cell size(1 cell = 1)
    public static final Color MESSAGE_COLOR = Color.GRAY;
    public static final double SIDE_CELLS = 3.4;
    public static final double GAP_CELLS = 0.35;
    public static final double HEADER_CELLS = 0.85;
    public static final double BUTTON_W_CELLS = 2.8;
    public static final double BUTTON_H_CELLS = 0.7;
    public static final double AFTER_BUTTON_CELLS = 0.45;
    public static final double AFTER_HEADER_CELLS = 0.2;

}