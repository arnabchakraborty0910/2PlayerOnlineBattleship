package game.StateMachine;

//a interface that is a state the game can be in
public interface State {
    void enter();
    void update();
    void exit();
}
