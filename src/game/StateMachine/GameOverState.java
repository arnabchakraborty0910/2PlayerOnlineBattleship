package game.StateMachine;

import game.Player.Player;

// The match is finished. This is the ending screen.
public class GameOverState implements State {
    private StateMachine stateMachine;
    private Player winner;
    private Player losser;

    public GameOverState(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    @Override
    public void enter() {
        IO.println("WINNER: " + winner.name);
        IO.println("LOSSER: " + losser.name);

        //stops the game
        stateMachine.running = false;
    }

    @Override
    public void update() {
    }

    @Override
    public void exit() {
    }

    public void setWinner(Player winner) {
        this.winner = winner;
        if(winner == stateMachine.player1)
            losser = stateMachine.player2;
        else
            losser = stateMachine.player1;
    }
}
