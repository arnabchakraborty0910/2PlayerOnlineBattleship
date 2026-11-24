package game;

/*
 * This is the ship
 *
 * Contains:
 * the positions the ship is on the board
 * Is it sunk
 * the name
 * the size
 *
 */
public class Ship {
    public enum  orientation {
        horizontal,
        vertical
    }
    public int size;
    public String name;
    orientation shipsOrientation;
    public boolean sunk;
    Cord[] cords;

    public Ship(int size, orientation or, String name) {
        this.size = size;
        this.name = name;
        this.shipsOrientation = or;
        sunk = false;
        cords = new Cord[size];
    }

    //sets the cords of the ship on the baord
    public void setCords(Cord start, Cord end) throws GameExcepton {

        if(cords == null )
            throw new GameExcepton("Cords list of " + this.name + " isnt intialized");
        if(start == null || end == null)
            throw new GameExcepton("Start or End cords are null for " + this.name);

        if(start.row == end.row) {
            //horizontal
           for(int i = 0; i < Math.abs(end.col - start.col) + 1; i++) {
                cords[i] = new Cord(start.row, start.col + i);
           }
        } else {
            //vertical
            for(int i = 0; i < Math.abs(end.row - start.row) + 1; i++) {
                cords[i] = new Cord(start.row + i, start.col);
            }
        }
    }

    //sets one of the cordinates of the ship to hit
    public boolean hit(int row, int col) {
        for(int i = 0; i < cords.length; i++) {
            if(cords[i].row == row && cords[i].col == col) {
                cords[i].hit = true;
                return true;
            }
        }
        return false;
    }



}