package game.GameComponents;

public class Board {
    //0 is empty, 1 is taken
    public Ship[][] board;


    public Board() {
        board = new Ship[GameRules.BOARD_SIZE][GameRules.BOARD_SIZE];
    }

    public boolean setShip(Cord s, Cord e, Ship ship) throws GameExcepton{
        //checks for out of bound points
        if (s.row < 0 || s.row >= GameRules.BOARD_SIZE || s.col < 0 || s.col >= GameRules.BOARD_SIZE) {
            return false;
        }
        if (e.row < 0 || e.row >= GameRules.BOARD_SIZE || e.col < 0 || e.col >= GameRules.BOARD_SIZE) {
            return false;
        }

        if (s.row == e.row) {
            if (e.col < s.col) {
                return false;//make sure right orientation
            }

            //length of the ship
            int length = e.col - s.col + 1;

            //check if ship size matches cords distance
            if (length != ship.size) {
                return false;
            }
            //check if spot is available
            for (int i = 0; i < length; i++) {
                if (board[s.row][s.col + i] != null) {
                    return false;
                }
            }

            //sets ship at those cords
            ship.setCords(s, e);
            for (int i = 0; i < length; i++) {
                board[s.row][s.col + i] = ship;
            }
            return true;
        } else if (s.col == e.col) {
            //same as before but vertical
            if (e.row < s.row) {
                return false;
            }
            int length = e.row - s.row + 1;
            if (length != ship.size) {
                return false;
            }
            for (int i = 0; i < length; i++) {
                if (board[s.row + i][s.col] != null) {
                    return false;
                }
            }
            ship.setCords(s, e);
            for (int i = 0; i < length; i++) {
                board[s.row + i][s.col] = ship;
            }
            return true;
        } else {
            throw new GameExcepton("Col or Row dont equal");
        }


    }



    public Ship checkSpot(int row, int col) {
        return board[row][col];
    }


}