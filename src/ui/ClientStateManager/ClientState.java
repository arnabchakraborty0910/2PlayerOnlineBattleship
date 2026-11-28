package ui.ClientStateManager;

import java.awt.*;

public interface ClientState {
    void enter();
    void exit();
    void onClick(Point panelPoint);
    boolean handleLine(String line); // true if this phase consumed it
    void paint(Graphics g);
}
