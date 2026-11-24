package game;

public class Board {
    //0 is empty, 1 is taken
    public Ship[][] board;


    public Board() {
        board = new Ship[GameRules.BOARD_SIZE][GameRules.BOARD_SIZE];
    }

    public boolean setShip(Cord s, Cord e, Ship ship) throws GameExcepton{
        if(s.row == e.row) {
            for (int i = 0; i < Math.abs(e.col - s.col) + 1; i++) {
                if (board[s.row][i] != null)
                    return false;
            }

            ship.setCords(s, e);
            for (int i = 0; i < Math.abs(e.col - s.col) + 1; i++) {
                board[s.row][i] = ship;
            }
            return true;

        } else if (s.col == e.col) {
            for (int i = 0; i < Math.abs(e.row - s.row) + 1; i++) {
                if (board[i][s.row] != null)
                    return false;
            }

            for (int i = 0; i < Math.abs(e.row - s.row) + 1; i++) {
                board[i][s.row] = ship;
            }
            ship.setCords(s, e);
            return true;
        } else {
            throw new GameExcepton("Col or Row dont equal");
        }


    }



    public Ship checkSpot(int row, int col) {
        return board[row][col];
    }


}