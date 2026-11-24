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
    public boolean turn;
    public boolean setUp;

    public Player() {
        turn = false;
        setUp = false;
        board = new Board();
    }
}


