package ui;


import game.GameComponents.Cord;
import game.GameComponents.GameRules;
import net.ClientConnection;

import javax.swing.*;

import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.LinkedHashMap;

public class GamePanel extends JPanel{
    private static final String[] SHIP_NAMES = {
            "Carrier", "BattleShip", "Destroyer", "Submarine", "PatrolBoat"
    };
    private static final String[] SHIP_LABELS = {
            "Carrier", "BattleShip", "Destroyer", "Submarine", "Patrol Boat"
    };

    //all the components for the UI
    private final JButton disconnect = new ColorButton("Disconnect", GameRules.BUTTON_COLOR);
    private final JLabel serverLabel = title("Server");
    private final JLabel shipsLabel = title("Ships");
    private final JLabel attacksLabel = title("Attacks");
    private final JLabel chatLabel = title("Chat");
    private final JTextArea serverText = messageArea();
    private final JTextArea chatText = messageArea();
    private final JScrollPane serverScroll = scroll(serverText);
    private final JScrollPane chatScroll = scroll(chatText);
    private final BoardPanel ships = new BoardPanel();
    private final BoardPanel attacks = new BoardPanel();
    private final ColorButton[] shipButtons = new ColorButton[SHIP_NAMES.length];
    private final ColorButton clearButton = new ColorButton("Clear", GameRules.BUTTON_COLOR);
    private final ColorButton shipsPlaced = new ColorButton("Ships Placed", GameRules.PLACED_BUTTON_COLOR);
    private final LinkedHashMap<String, Placement> placed = new LinkedHashMap<>();

    private ClientConnection connection;//used for communicating with server
    private Ghost ghost;
    private Point mousePoint;//mouse position
    private boolean showSetupButtons;
    private boolean submitted;
    private boolean waitingForPlaceReply;
    private int version;//number of times board has been cleared
    private int currentVersion = -1;//which version of the board were on
    private boolean watchingMouse;

    //mouse listener to get mouse position
    private final AWTEventListener mouseWatcher = event -> {
        if(!(event instanceof MouseEvent mouse)) {
            return;
        }

        int id = mouse.getID();//gets what the mouse did(pressed right click, moved, etc)

        //checks if its these 3 things
        if(id != MouseEvent.MOUSE_MOVED && id != MouseEvent.MOUSE_DRAGGED && id != MouseEvent.MOUSE_PRESSED) {
            return;
        }

        //mouse.getComponent() is component the mouse landed on (button, label, text, etc)
        //this checks if theres something inside that component(Ex: text field in a panel)
        if(!SwingUtilities.isDescendingFrom(mouse.getComponent(), this)) {
            return;
        }

        //mouse.getPoint() gets the point that the mouse is on
        //convertPoint turns the position of the mouse into pixels
        mousePoint = SwingUtilities.convertPoint(mouse.getComponent(), mouse.getPoint(), this);

        //checks if left click is clicked
        if(id == MouseEvent.MOUSE_PRESSED && mouse.getButton() == GameRules.MOUSE_BUTTON && !isOnButton(mousePoint)) {
            //checks if mouse has been clicked
            onGameClick(mousePoint);
        }

        //this is for preview ships that haven't been placed yet
        if (ghost != null) {
            repaint();
        }

    };

    public GamePanel() {
        setLayout(null);
        setBackground(GameRules.BACKGROUND_COLOR);

        //set mouse listener for all components
        MouseMotionAdapter trackMouse = new MouseMotionAdapter() {};
        addMouseMotionListener(trackMouse);
        serverLabel.addMouseMotionListener(trackMouse);
        shipsLabel.addMouseMotionListener(trackMouse);
        attacksLabel.addMouseMotionListener(trackMouse);
        chatLabel.addMouseMotionListener(trackMouse);

        //action listener for button that disconnects the player from the match
        disconnect.addActionListener(event -> {
            if (connection != null) {
                connection.close();
            }
            System.exit(0);
        });
        add(disconnect);
        add(serverLabel);
        add(shipsLabel);
        add(attacksLabel);
        add(chatLabel);
        add(serverScroll);
        add(chatScroll);
        add(ships);
        add(attacks);

        //adds all the ship buttons
        for (int i = 0; i < SHIP_NAMES.length; i++) {
            String name = SHIP_NAMES[i];
            ColorButton button = new ColorButton(SHIP_LABELS[i], GameRules.BUTTON_COLOR);
            button.setVisible(false);
            button.addActionListener(event -> onShipButton(name));
            shipButtons[i] = button;
            add(button);
        }

        //these buttons start off a false because they are activated after certain conditions
        clearButton.setVisible(false);
        clearButton.addActionListener(event -> onClear());
        add(clearButton);
        shipsPlaced.setVisible(false);
        shipsPlaced.addActionListener(event -> onShipsPlaced());
        add(shipsPlaced);

        //sets the keyboard inputs(R for rotate0
        InputMapBindings();

        //sets up a line to the server so that the player can talk to the server
        try {
            connection = new ClientConnection(this::onServerLine);
        } catch (IOException e) {
            appendServer("Could not connect to the server.");
        }
    }

    private void InputMapBindings() {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "rotate");//adds r as a button

        //makes pressing r do something. In this case, rotate the ship
        getActionMap().put("rotate", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(ghost == null)
                    return;
                ghost.horizontal = !ghost.horizontal;
                repaint();
            }
        });
    }

    //turns on the ability to find the cursors position on the JPanel
    @Override
    public void addNotify() {
        super.addNotify();
        if (!watchingMouse) {
            Toolkit.getDefaultToolkit().addAWTEventListener(mouseWatcher, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
            watchingMouse = true;
        }
    }

    //turns off the ability to find the cursors position on the JPanel
    @Override
    public void removeNotify() {
        if (watchingMouse) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(mouseWatcher);
            watchingMouse = false;
        }
        super.removeNotify();
    }


    @Override
    public void paint(Graphics g) {
        super.paint(g);
        paintGhost(g);
    }


    //this is a overriden method that runs every time revalidate() is called. It redraws and repositions every component in GamePanel
    @Override
    public void doLayout() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        //widths for each object on the window
        //visual example(Everything is in terms of the size of one cell)
        /*
         * | Server 3.4 |gap| Ships board 10.9 |gap| Attacks board 10.9 |gap| Chat 3.4 |
0        *  3.4  3.75              14.65 15.0               25.9 26.25      29.65
         *   shipsX                  attacksX                 chatX          designW
         */
        double boardW = BoardPanel.widthInCells();
        double shipsX = GameRules.SIDE_CELLS + GameRules.GAP_CELLS;
        double attacksX = shipsX + boardW + GameRules.GAP_CELLS;
        double chatX = attacksX + boardW + GameRules.GAP_CELLS;
        double designW = chatX + GameRules.SIDE_CELLS;

        //height for each object on the window
        //visual example(Everything is in terms of the size of one cell)
        /*
         * 0.00  disconnect button
         * 0.70  gap
         * 1.15  titles: Server, Ships, Attacks, Chat     <- headerY
         * 2.00  small gap
         * 2.20  column numbers on the boards             <- columnY
         * 2.95  first row of board cells                 <- cellY
         *       ... 10 rows ...
         * 12.95 bottom of the boards                     <- designH
         */

        double headerY = GameRules.BUTTON_H_CELLS +  GameRules.AFTER_BUTTON_CELLS;
        double columnY = headerY + GameRules.HEADER_CELLS + GameRules.AFTER_HEADER_CELLS;
        double cellY = columnY + GameRules.COLUMN_LABEL_CELLS;
        double boardsBottom = columnY + BoardPanel.heightInCells();

        double designH = boardsBottom;

        if (showSetupButtons) {
            designH += GameRules.AFTER_BOARD_CELLS + GameRules.SETUP_BUTTON_H_CELLS;
        }


        //size of cell in pixels
        double cellPx = Math.min(getWidth() / designW, getHeight() / designH);

        //offset of where the GUI starts from the edge of the window
        double offsetX = (getWidth() - designW * cellPx) / 2.0;
        double offsetY = (getHeight() - designH * cellPx) / 2.0;

        //placing objects based on the nubers calculated previously
        place(disconnect, 0, 0, GameRules.BUTTON_W_CELLS, GameRules.BUTTON_H_CELLS, cellPx, offsetX, offsetY);
        place(shipsPlaced, designW - GameRules.SHIPS_PLACED_W_CELLS, 0,
                GameRules.SHIPS_PLACED_W_CELLS, GameRules.BUTTON_H_CELLS, cellPx, offsetX, offsetY);
        place(serverLabel, 0, headerY, GameRules.SIDE_CELLS, GameRules.HEADER_CELLS, cellPx, offsetX, offsetY);
        place(shipsLabel, shipsX + GameRules.ROW_LABEL_CELLS, headerY,
                GameRules.BOARD_SIZE, GameRules.HEADER_CELLS, cellPx, offsetX, offsetY);
        place(attacksLabel, attacksX + GameRules.ROW_LABEL_CELLS, headerY,
                GameRules.BOARD_SIZE, GameRules.HEADER_CELLS, cellPx, offsetX, offsetY);
        place(chatLabel, chatX, headerY, GameRules.SIDE_CELLS, GameRules.HEADER_CELLS, cellPx, offsetX, offsetY);
        place(serverScroll, 0, cellY, GameRules.SIDE_CELLS, GameRules.BOARD_SIZE, cellPx, offsetX, offsetY);
        place(ships, shipsX, columnY, boardW, BoardPanel.heightInCells(), cellPx, offsetX, offsetY);
        place(attacks, attacksX, columnY, boardW, BoardPanel.heightInCells(), cellPx, offsetX, offsetY);
        place(chatScroll, chatX, cellY, GameRules.SIDE_CELLS, GameRules.BOARD_SIZE, cellPx, offsetX, offsetY);

        //button location and setting up
        double buttonsY =  boardsBottom + GameRules.AFTER_BOARD_CELLS;
        double gap = GameRules.SETUP_BUTTON_GAP_CELLS;
        double buttonW = (designW - gap * 5) / 6.0;
        //places each button with a gap
        for (int i = 0; i < shipButtons.length; i++) {
            place(shipButtons[i], i * (buttonW + gap), buttonsY, buttonW,
                    GameRules.SETUP_BUTTON_H_CELLS, cellPx, offsetX, offsetY);
        }
        place(clearButton, 5 * (buttonW + gap), buttonsY, buttonW,
                GameRules.SETUP_BUTTON_H_CELLS, cellPx, offsetX, offsetY);



        //set the size of title, button, and message text size and Fonts
        float titleSize = (float) (GameRules.HEADER_CELLS * cellPx * 0.55);
        float messageSize = (float) (cellPx * 0.32);
        Font titles = new Font(GameRules.FONT, Font.PLAIN, Math.max(1, Math.round(titleSize)));
        serverLabel.setFont(titles);
        shipsLabel.setFont(titles);
        attacksLabel.setFont(titles);
        chatLabel.setFont(titles);
        fit(disconnect, GameRules.BUTTON_H_CELLS, cellPx);
        fit(shipsPlaced, GameRules.BUTTON_H_CELLS, cellPx);
        for (ColorButton button : shipButtons) {
            fit(button, GameRules.SETUP_BUTTON_H_CELLS, cellPx);
        }
        fit(clearButton, GameRules.SETUP_BUTTON_H_CELLS, cellPx);
        Font messages = new Font(GameRules.FONT, Font.PLAIN, Math.max(1, Math.round(messageSize)));
        serverText.setFont(messages);
        chatText.setFont(messages);
    }

    //set each components location(x,y), size(w = width, h = height), size of an individual cell(cellPx), and the offset from the sides of the windows(offsetX and offsetY)
    private void place(Component component, double x, double y, double w, double h,
                              double cellPx, double offsetX, double offsetY) {
        int left = (int) Math.round(offsetX + x * cellPx);
        int top = (int) Math.round(offsetY + y * cellPx);
        int right = (int) Math.round(offsetX + (x + w) * cellPx);
        int bottom = (int) Math.round(offsetY + (y + h) * cellPx);
        component.setBounds(left, top, Math.max(1, right - left), Math.max(1, bottom - top));
    }

    private void fit(JButton button, double heightCells, double cellPx) {
        int size = Math.max(1, (int) Math.round(heightCells * cellPx * 0.55));
        Font font = new Font(GameRules.FONT, Font.PLAIN, size);

        //This reduces the size of the text until it can fit onto the button
        while(size > 1) {
            FontMetrics metrics = button.getFontMetrics(font);
            if(metrics.stringWidth(button.getText()) <= button.getWidth() - 10) {
                break;
            }

            size--;
            font = new Font(GameRules.FONT, Font.PLAIN, size);
        }
        button.setFont(font);
    }

    //this give the server information into a funciton that reads the server commands
    private void onServerLine(String line) {
        SwingUtilities.invokeLater(() -> handleServerLine(line));
    }

    //reads the server commands
    private void handleServerLine(String line) {
        if(line.equals("BYE")) {
            //server hangs up
            appendServer("Game Over");
            return;
        }
        if (line.equals("SETUP")) {
            //set up
            showSetup();
            return;
        }
        if (line.equals("SETUP_DONE")) {
            //after set up
            showSetupButtons = false;
            ghost = null;
            setSetupButtonsVisible(false);
            attacks.setCoverMessage(null);
            revalidate();
            repaint();
            return;
        }
        if(line.equals("WAITING")) {
            //watiting for other player
            showSetupButtons = false;
            ghost = null;
            setSetupButtonsVisible(false);
            revalidate();
            repaint();
            return;
        }
        if(line.equals("CLEARED")) {
            //pressed cleared
            resetBoard();
            return;

        }
        if(line.equals("DONE_REJECT")) {
            //
            submitted = false;
            return;
        }
        if(line.startsWith("REJECT ")) {
            //
            waitingForPlaceReply = false;
            repaint();
            return;
        }
        if(line.startsWith("REMOVED ")) {
            //removed ship
            return;
        }
        if((line.startsWith("PLACED "))) {
            applyPlaced(line);
            return;
        }
        if(line.startsWith("SEND ")){
            appendServer(line.substring(5));
            return;
        }
        if(line.startsWith("ASK ")) {
            appendServer(line.substring(4));
            return;
        }
        appendServer(line);
    }

    private void showSetup() {
        showSetupButtons = true;
        submitted = false;
        setSetupButtonsVisible(true);
        attacks.setCoverMessage("Not used when\npicking ships");
        refreshShipsPlacedButton();
        revalidate();
        repaint();
    }

    //sets buttons visible or not visible
    private void setSetupButtonsVisible(boolean visible) {
        for (ColorButton button : shipButtons) {
            button.setVisible(visible);
        }
        clearButton.setVisible(visible);
        shipsPlaced.setVisible(visible);
    }

    //every time ship is placed
    private void onShipButton(String name) {
        //checks for server connection, is ship placed, the set up buttons are greyed out, or the client isnt waiting for a reply
        if (connection == null || submitted || waitingForPlaceReply || !showSetupButtons)
            return;

        //stores the current position of the mouse
        rememberMouse();

        //checks if the ship is already placed
        if(placed.containsKey(name)) {
            pickUp(name);
            return;
        }

        //otherwise ship hasent been placed so ghost is made
        ghost = new Ghost(name, GameRules.shipSize(name));
        repaint();
    }

    //clearing the board
    private void onClear() {
        //add comments from here on out
        if (connection == null || submitted || !showSetupButtons)
            return;

        version++;
        resetBoard();
        connection.send("CLEAR");
    }

    private void onGameClick(Point panelPoint) {
        //checks to make sure there is a connection, ships are already submited, and set up buttons are hidden
        if (connection == null || submitted || !showSetupButtons) {
            return;
        }


        Point local = SwingUtilities.convertPoint(this, panelPoint, ships);
        //checks if the click happend on the grid
        boolean inside = local.x >= 0 && local.y >= 0  && local.x < ships.getWidth() && local.y < ships.getHeight();
        //sets a cell if the click happened in the grid
        Cord cell = inside ? ships.cellAt(local.x, local.y) : null;
        if (cell != null) {
            //checks if theres a ship there
            String name = ships.shipAt(cell.row, cell.col);
            if (name != null) {
                pickUp(name);
                return;
            }
        }

        //if there's no ghost and waiting for reply, no ship is placed
        if (ghost == null || waitingForPlaceReply) {
            return;
        }

        //if cell is null, that means that this isnt a good place to put this ship
        if (cell == null) {
            appendServer("This isn't a possible place to put this ship.");
            return;
        }

        //asks server if it can place a ship in those spots
        waitingForPlaceReply = true;
        currentVersion =version;
        String direction = ghost.horizontal ? "H" : "V";
        connection.send("PLACE " + ghost.name + " " + cell.row + " " + cell.col + " " + direction);
    }

    //this function picks up ships that have already been placed
    private void pickUp(String name) {
        if (connection == null || submitted || waitingForPlaceReply) {
            return;
        }

        Placement placement = placed.get(name);
        if (placement == null) {
            return;
        }
        erase(placement);
        placed.remove(name);
        ghost = new Ghost(name, placement.size);
        ghost.horizontal = placement.horizontal;
        refreshShipButton(name);
        refreshShipsPlacedButton();
        connection.send("REMOVE " + name);
        repaint();
    }

    //this actually puts a ship onto the board when the server says it can be placed there
    private void applyPlaced(String line) {
        waitingForPlaceReply = false;
        String[] parts = line.split(" ");
        if (parts.length != 5 || currentVersion !=version) {
            repaint();
            return;
        }
        try {
            String name = parts[1];
            int row = Integer.parseInt(parts[2]);
            int col = Integer.parseInt(parts[3]);
            boolean horizontal = parts[4].equals("H");
            int size = GameRules.shipSize(name);
            Placement placement = new Placement(name, row, col, horizontal, size);
            placed.put(name, placement);
            for (int i = 0; i < size; i++) {
                int r = row + (horizontal ? 0 : i);
                int c = col + (horizontal ? i : 0);
                ships.setOccupant(r, c, name);
            }
            ghost = null;
            ships.repaint();
            refreshShipButton(name);
            refreshShipsPlacedButton();
            repaint();
        } catch (NumberFormatException e) {
            repaint();
        }
    }

    //this resets the board clearing what the user has
    private void resetBoard() {
        ghost = null;
        waitingForPlaceReply = false;
        placed.clear();
        ships.clearOccupants();
        for (ColorButton button : shipButtons) {
            button.setFill(GameRules.BUTTON_COLOR);
        }
        refreshShipsPlacedButton();
        repaint();
    }
    private void erase(Placement placement) {
        for (int i = 0; i < placement.size; i++) {
            int row = placement.row + (placement.horizontal ? 0 : i);
            int col = placement.col + (placement.horizontal ? i : 0);
            ships.setOccupant(row, col, null);
        }
        ships.repaint();
    }

    //recolors a ship button
    private void refreshShipButton(String name) {
        ColorButton button = buttonFor(name);
        if (button == null) {
            return;
        }
        if (placed.containsKey(name)) {
            button.setFill(GameRules.PLACED_BUTTON_COLOR);
        } else {
            button.setFill(GameRules.BUTTON_COLOR);
        }
    }

    //this is for the shipsPlaced button. Checks when all ships have been placed
    private void refreshShipsPlacedButton() {
        if (placed.size() == 5) {
            shipsPlaced.setFill(GameRules.BUTTON_COLOR);
        } else {
            shipsPlaced.setFill(GameRules.PLACED_BUTTON_COLOR);
        }
    }

    //Take in a name of a button and returns it
    private ColorButton buttonFor(String name) {
        for (int i = 0; i < SHIP_NAMES.length; i++) {
            if (SHIP_NAMES[i].equals(name)) {
                return shipButtons[i];
            }
        }
        return null;
    }

    //paints the ghost ship on the cursor on the grid
    private void paintGhost(Graphics g) {
        if (ghost == null || mousePoint == null || !showSetupButtons) {
            return;
        }
        int cell = ships.cellPx();
        if (cell <= 0) {
            return;
        }
        Point local = SwingUtilities.convertPoint(this, mousePoint, ships);
        boolean inside = local.x >= 0 && local.y >= 0
                && local.x < ships.getWidth() && local.y < ships.getHeight();
        Cord snapped = inside ? ships.cellAt(local.x, local.y) : null;
        int x;
        int y;
        if (snapped != null) {
            Point origin = ships.cellOrigin(snapped.row, snapped.col);
            Point onPanel = SwingUtilities.convertPoint(ships, origin, this);
            x = onPanel.x;
            y = onPanel.y;
        } else if (ghost.horizontal) {
            x = mousePoint.x;
            y = mousePoint.y - cell / 2;
        } else {
            x = mousePoint.x - cell / 2;
            y = mousePoint.y;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        for (int i = 0; i < ghost.size; i++) {
            int sx = x + (ghost.horizontal ? i * cell : 0);
            int sy = y + (ghost.horizontal ? 0 : i * cell);
            g2.setColor(GameRules.SHIP_COLOR);
            g2.fillRect(sx, sy, cell, cell);
            g2.setColor(GameRules.FONT_COLOR);
            g2.drawRect(sx, sy, cell - 1, cell - 1);
        }
        g2.dispose();
    }

    //gets mouse positon
    private void rememberMouse() {
        PointerInfo info = MouseInfo.getPointerInfo();
        if (info == null) {
            return;
        }
        Point screen = info.getLocation();
        SwingUtilities.convertPointFromScreen(screen, this);
        mousePoint = screen;
    }

    //checks if a button has been clicked
    private boolean isOnButton(Point panelPoint) {
        for (Component component : getComponents()) {
            if (component instanceof JButton && component.isVisible()
                    && component.getBounds().contains(panelPoint)) {
                return true;
            }
        }
        return false;
    }

    //adds one line to the server box so you can see what the server has said
    private void appendServer(String text) {
        serverText.append(text + "\n");
        serverText.setCaretPosition(serverText.getDocument().getLength());
    }

    //code for after ship has been placed
    private void onShipsPlaced() {
        if (connection == null || submitted || placed.size() != 5) {
            return;
        }
        submitted = true;
        ghost = null;
        connection.send("DONE");
        repaint();
    }


    //set up diffrent components for the Panel
    private JLabel title(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(Color.BLACK);
        return label;
    }

    private JTextArea messageArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(GameRules.MESSAGE_COLOR);
        area.setForeground(Color.BLACK);
        area.setMargin(new Insets(8, 8, 8, 8));
        return area;
    }
    private JScrollPane scroll(JTextArea area) {
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(GameRules.MESSAGE_COLOR);
        return scroll;
    }

    //this just creates every button with a certain color
    private class ColorButton extends JButton {
        private Color fill;
        ColorButton(String text, Color fill) {
            super(text);
            this.fill = fill;
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setMargin(new Insets(0, 2, 0, 2));
            setUI(new BasicButtonUI());
        }
        void setFill(Color fill) {
            this.fill = fill;
            repaint();
        }
        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(fill);
            g.fillRect(0, 0, getWidth(), getHeight());
            super.paintComponent(g);
        }
    }
    private final class Ghost {
        final String name;
        final int size;
        boolean horizontal = true;
        Ghost(String name, int size) {
            this.name = name;
            this.size = size;
        }
    }
    private final class Placement {
        final String name;
        final int row;
        final int col;
        final boolean horizontal;
        final int size;
        Placement(String name, int row, int col, boolean horizontal, int size) {
            this.name = name;
            this.row = row;
            this.col = col;
            this.horizontal = horizontal;
            this.size = size;
        }
    }
}

