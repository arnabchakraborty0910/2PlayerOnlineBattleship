package ui;

import game.GameComponents.GameRules;

import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Dimension;

public class ClientWindow extends JFrame {

    public ClientWindow() {
        //set up JFrame called ClientWindow
        super("BattleShip");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(GameRules.MIN_WINDOW_SIZE, GameRules.MIN_WINDOW_SIZE));
        setSize(GameRules.MIN_WINDOW_SIZE, GameRules.MIN_WINDOW_SIZE);
        setLocationRelativeTo(null);
        setContentPane(new GamePanel());

    }
}
