package ui;


import game.GameComponents.GameRules;
import net.ClientConnection;
import ui.ClientStateManager.ClientStateMachine;

import javax.swing.*;

import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class GamePanel extends JPanel{
    private static final String[] SHIP_NAMES = {
            "Carrier", "BattleShip", "Destroyer", "Submarine", "PatrolBoat"
    };
    private static final String[] SHIP_LABELS = {
            "Carrier", "BattleShip", "Destroyer", "Submarine", "Patrol Boat"
    };

    //all the components for the UI
    private final JButton disconnect = new ColorButton("Disconnect", GameRules.BUTTON_COLOR);
    private final JButton gameOverButton = new ColorButton("Disconnect", GameRules.BUTTON_COLOR);
    private final JLabel serverLabel = title("Server");
    private final JLabel shipsLabel = title("Ships");
    private final JLabel attacksLabel = title("Attacks");
    private final JLabel chatLabel = title("Chat");
    private final JTextArea serverText = messageArea();
    private final JTextArea chatText = messageArea();
    private final JScrollPane serverScroll = scroll(serverText);
    private final JScrollPane chatScroll = scroll(chatText);
    public final BoardPanel ships = new BoardPanel();
    public final BoardPanel attacks = new BoardPanel();
    private final ColorButton[] shipButtons = new ColorButton[SHIP_NAMES.length];
    private final ColorButton clearButton = new ColorButton("Clear", GameRules.BUTTON_COLOR);
    private final ColorButton shipsPlaced = new ColorButton("Ships Placed", GameRules.PLACED_BUTTON_COLOR);

    public ClientConnection connection;//used for communicating with server
    private final ClientStateMachine states = new ClientStateMachine();
    public Point mousePoint;//mouse position
    public boolean showSetupButtons;
    private boolean watchingMouse;

    //legend for when the players start shooting
    private final LegendPanel legend = new LegendPanel();
    public boolean showPlayLegend;

    //Game over boolean
    private boolean showGameOver;

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
            states.onClick(mousePoint);
        }
        repaint();
    };

    public GamePanel() {
        states.panel = this;
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

        //actionlistner for game over button
        gameOverButton.setVisible(false);
        gameOverButton.addActionListener(event -> {
            if (connection != null) {
                connection.close();
            }
            System.exit(0);
        });

        add(gameOverButton);
        add(disconnect);
        add(serverLabel);
        add(shipsLabel);
        add(attacksLabel);
        add(chatLabel);
        add(serverScroll);
        add(chatScroll);
        add(ships);
        add(attacks);

        //adds legend
        legend.setVisible(false);
        add(legend);

        //adds all the ship buttons
        for (int i = 0; i < SHIP_NAMES.length; i++) {
            String name = SHIP_NAMES[i];
            ColorButton button = new ColorButton(SHIP_LABELS[i], GameRules.BUTTON_COLOR);
            button.setVisible(false);
            button.addActionListener(event -> states.placeShipClientState.onShipButton(name));
            shipButtons[i] = button;
            add(button);
        }

        //these buttons start off a false because they are activated after certain conditions
        clearButton.setVisible(false);
        clearButton.addActionListener(event -> states.placeShipClientState.onClear());
        add(clearButton);
        shipsPlaced.setVisible(false);
        shipsPlaced.addActionListener(event -> states.placeShipClientState.onShipsPlaced());        add(shipsPlaced);

        //sets the keyboard inputs(R for rotate0
        InputMapBindings();

        //sets up a line to the server so that the player can talk to the server
        try {
            connection = new ClientConnection(this::onServerLine);
        } catch (IOException e) {
            appendServer("Could not connect to the server.");
        }
        states.changeState(states.waitingForOtherPlayerClientState);
    }

    private void InputMapBindings() {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "rotate");//adds r as a button

        //makes pressing r do something. In this case, rotate the ship
        getActionMap().put("rotate", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                states.placeShipClientState.rotate();
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
        states.paint(g);
    }


    //this is a overriden method that runs every time revalidate() is called. It redraws and repositions every component in GamePanel
    @Override
    public void doLayout() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        //set game over screen and nothing else
        if (showGameOver) {
            Rectangle button = gameOverButtonBounds();
            gameOverButton.setBounds(button);
            int size = Math.max(1, (int) (button.height * 0.55));
            Font font = new Font(GameRules.FONT, Font.PLAIN, size);
            while (size > 1) {
                FontMetrics metrics = gameOverButton.getFontMetrics(font);
                if (metrics.stringWidth(gameOverButton.getText()) <= button.width - 10) {
                    break;
                }
                size--;
                font = new Font(GameRules.FONT, Font.PLAIN, size);
            }
            gameOverButton.setFont(font);
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
        if (showPlayLegend) {
            designH += GameRules.AFTER_BOARD_CELLS + GameRules.LEGEND_H_CELLS;
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

        //places legend
        if (showPlayLegend) {
            double legendY = boardsBottom + GameRules.AFTER_BOARD_CELLS;
            double legendW = boardW + GameRules.GAP_CELLS + boardW;
            place(legend, shipsX, legendY, legendW, GameRules.LEGEND_H_CELLS, cellPx, offsetX, offsetY);
        }

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
        if (states.handleLine(line)) {
            return;
        }
        if (line.equals("BYE")) {
            appendServer("Game Over");
            return;
        }
        if (line.startsWith("SEND ")) {
            appendServer(line.substring(5));
            return;
        }
        if (line.startsWith("ASK ")) {
            appendServer(line.substring(4));
            return;
        }
        appendServer(line);
    }


    //sets buttons visible or not visible
    public void setSetupButtonsVisible(boolean visible) {
        for (ColorButton button : shipButtons) {
            button.setVisible(visible);
        }
        clearButton.setVisible(visible);
        shipsPlaced.setVisible(visible);
    }

    public void setSetupMode(boolean showing) {
        showSetupButtons = showing;
        setSetupButtonsVisible(showing);
        revalidate();
        repaint();
    }

    //set up mode
    public void setPlayLegend(boolean showing) {
        showPlayLegend = showing;
        legend.setVisible(showing);
        revalidate();
        repaint();
    }

    public void setShipButtonFill(String name, Color fill) {
        for (int i = 0; i < SHIP_NAMES.length; i++) {
            if (SHIP_NAMES[i].equals(name)) {
                shipButtons[i].setFill(fill);
                return;
            }
        }
    }

    public void resetShipButtonFills() {
        for (ColorButton button : shipButtons) {
            button.setFill(GameRules.BUTTON_COLOR);
        }
    }

    public void setShipsPlacedFill(Color fill) {
        shipsPlaced.setFill(fill);
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
    public void appendServer(String text) {
        serverText.append(text + "\n");
        serverText.setCaretPosition(serverText.getDocument().getLength());
    }

    //set all components to false for game over screen
    public void setGameOverMode(boolean showing) {
        showGameOver = showing;
        disconnect.setVisible(!showing);
        gameOverButton.setVisible(showing);
        serverLabel.setVisible(!showing);
        shipsLabel.setVisible(!showing);
        attacksLabel.setVisible(!showing);
        chatLabel.setVisible(!showing);
        serverScroll.setVisible(!showing);
        chatScroll.setVisible(!showing);
        ships.setVisible(!showing);
        attacks.setVisible(!showing);
        if (showing) {
            setSetupMode(false);
            setPlayLegend(false);
        }
        revalidate();
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

    //legend class
    private class LegendPanel extends JPanel {
        LegendPanel() {
            setOpaque(true);
            setBackground(GameRules.BACKGROUND_COLOR);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getWidth() <= 0 || getHeight() <= 0) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int fontSize = Math.max(1, (int) (getHeight() * 0.42));
            int box = Math.max(4, (int) (getHeight() * 0.55));
            int gap = Math.max(4, box / 2);
            int textGap = Math.max(2, box / 5);
            g2.setFont(new Font(GameRules.FONT, Font.PLAIN, fontSize));
            FontMetrics metrics = g2.getFontMetrics();

            String shipText = "-> ship";
            String hitText = "-> hit";
            String missText = "-> Miss";

            //math to set up legend
            int shipW = box + textGap + metrics.stringWidth(shipText);
            int hitW = box + textGap + metrics.stringWidth(hitText);
            int missW = box + textGap + metrics.stringWidth(missText);
            int total = shipW + gap + hitW + gap + missW;

            while (fontSize > 1 && total > getWidth() - 4) {
                fontSize--;
                box = Math.max(4, (int) (getHeight() * 0.55));
                gap = Math.max(4, box / 2);
                textGap = Math.max(2, box / 5);
                g2.setFont(new Font(GameRules.FONT, Font.PLAIN, fontSize));
                metrics = g2.getFontMetrics();
                shipW = box + textGap + metrics.stringWidth(shipText);
                hitW = box + textGap + metrics.stringWidth(hitText);
                missW = box + textGap + metrics.stringWidth(missText);
                total = shipW + gap + hitW + gap + missW;
            }

            int x = (getWidth() - total) / 2;
            int y = (getHeight() - box) / 2;
            int textY = y + (box + metrics.getAscent() - metrics.getDescent()) / 2;
            g2.setColor(GameRules.FONT_COLOR);

            g2.setColor(GameRules.SHIP_COLOR);
            g2.fillRect(x, y, box, box);
            g2.setColor(GameRules.FONT_COLOR);
            g2.drawString(shipText, x + box + textGap, textY);
            x += shipW + gap;

            g2.setColor(GameRules.HIT_COLOR);
            g2.fillRect(x, y, box, box);
            g2.setColor(GameRules.FONT_COLOR);
            g2.drawString(hitText, x + box + textGap, textY);
            x += hitW + gap;

            g2.drawRect(x, y, box - 1, box - 1);
            int pad = Math.max(2, box / 6);
            g2.drawOval(x + pad, y + pad, box - pad * 2, box - pad * 2);
            g2.drawString(missText, x + box + textGap, textY);
            g2.dispose();
        }
    }

    //game over button
    public Rectangle gameOverButtonBounds() {
        int buttonW = Math.max(1, (int) (getWidth() * 0.16));
        int buttonH = Math.max(1, (int) (getHeight() * 0.07));
        int buttonX = (getWidth() - buttonW) / 2;
        int buttonY = (int) (getHeight() * 0.68);
        return new Rectangle(buttonX, buttonY, buttonW, buttonH);
    }

}

