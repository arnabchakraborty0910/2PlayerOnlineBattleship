package game;

/*
 * this is the coordinates
 * It's used by ship to hold the cords that the ship occupies and
 * whether that position has been hit
 */
public class Cord {
    public int row;
    public int col;
    public boolean hit;

    public Cord(int row, int col) {
        this.row = row;
        this.col = col;
        this.hit = false;
    }
}