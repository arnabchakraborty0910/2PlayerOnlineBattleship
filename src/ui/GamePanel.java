package ui;


import game.GameComponents.GameRules;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;


public class GamePanel extends JPanel{
    private final JButton disconnect = new DisconnectButton();
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


    public GamePanel() {
        setLayout(null);
        setBackground(GameRules.BACKGROUND_COLOR);
        add(disconnect);
        add(serverLabel);
        add(shipsLabel);
        add(attacksLabel);
        add(chatLabel);
        add(serverScroll);
        add(chatScroll);
        add(ships);
        add(attacks);
    }

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
        double designH = cellY + GameRules.BOARD_SIZE;

        //size of cell in pixels
        double cellPx = Math.min(getWidth() / designW, getHeight() / designH);

        //offset of where the GUI starts from the edge of the window
        double offsetX = (getWidth() - designW * cellPx) / 2.0;
        double offsetY = (getHeight() - designH * cellPx) / 2.0;

        //placing objects based on the nubers calculated previously
        place(disconnect, 0, 0, GameRules.BUTTON_W_CELLS, GameRules.BUTTON_H_CELLS, cellPx, offsetX, offsetY);
        place(serverLabel, 0, headerY, GameRules.SIDE_CELLS, GameRules.HEADER_CELLS, cellPx, offsetX, offsetY);
        place(shipsLabel, shipsX + GameRules.ROW_LABEL_CELLS, headerY, GameRules.BOARD_SIZE, GameRules.HEADER_CELLS, cellPx, offsetX, offsetY);
        place(attacksLabel, attacksX + GameRules.ROW_LABEL_CELLS, headerY, GameRules.BOARD_SIZE, GameRules.HEADER_CELLS, cellPx, offsetX, offsetY);
        place(chatLabel, chatX, headerY, GameRules.SIDE_CELLS, GameRules.HEADER_CELLS, cellPx, offsetX, offsetY);
        place(serverScroll, 0, cellY, GameRules.SIDE_CELLS, GameRules.BOARD_SIZE, cellPx, offsetX, offsetY);
        place(ships, shipsX, columnY, boardW, BoardPanel.heightInCells(), cellPx, offsetX, offsetY);
        place(attacks, attacksX, columnY, boardW, BoardPanel.heightInCells(), cellPx, offsetX, offsetY);
        place(chatScroll, chatX, cellY, GameRules.SIDE_CELLS, GameRules.BOARD_SIZE, cellPx, offsetX, offsetY);

        //set the size of title, button, and message text size and Fonts
        float titleSize = (float) (GameRules.HEADER_CELLS * cellPx * 0.55);
        float buttonSize = (float) (GameRules.BUTTON_H_CELLS * cellPx * 0.55);
        float messageSize = (float) (cellPx * 0.32);
        Font titles = new Font(GameRules.FONT, Font.PLAIN, Math.max(1, Math.round(titleSize)));
        serverLabel.setFont(titles);
        shipsLabel.setFont(titles);
        attacksLabel.setFont(titles);
        chatLabel.setFont(titles);
        disconnect.setFont(new Font(GameRules.FONT, Font.PLAIN, Math.max(1, Math.round(buttonSize))));
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

    //set up diffrent components for the Panel
    private JLabel title(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(Color.BLACK);
        return label;
    }

    private static JTextArea messageArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(GameRules.MESSAGE_COLOR);
        area.setForeground(Color.BLACK);
        area.setMargin(new Insets(8, 8, 8, 8));
        return area;
    }
    private static JScrollPane scroll(JTextArea area) {
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(GameRules.MESSAGE_COLOR);
        return scroll;
    }

    //set disconnect button up
    private static class DisconnectButton extends JButton {
        DisconnectButton() {
            super("Disconnect");
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setUI(new BasicButtonUI());
            addActionListener(event -> System.exit(0));
        }
        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(Color.RED);
            g.fillRect(0, 0, getWidth(), getHeight());
            super.paintComponent(g);
        }
    }
}
