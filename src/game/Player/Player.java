package game.Player;

import game.GameComponents.Board;
import game.GameComponents.Ship;

//holds all the information for each player in the game
public class Player {
    public Ship Carrier;
    public Ship BattleShip;
    public Ship Destroyer;
    public Ship Submarine;
    public Ship PatrolBoat;
    public Board board;
    public boolean setUp;
    public String name;

    public Player(String name) {
        setUp = false;
        board = new Board();
        this.name = name;
    }

    //checks if player has lost and returns true if all there boats have sank
    public boolean checkIfPlayerLost() {
        if(Carrier.sunk
            && BattleShip.sunk
            && Destroyer.sunk
            && Submarine.sunk
            && PatrolBoat.sunk)
            return true;

        return false;
    }
}


