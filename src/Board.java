import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

//sound played and lines drawn when match is found

public class Board extends JPanel {
    private static final int tileSize = 40; //in pixels

    private Tile [] [] tiles;
    private Point oldHint [];
    private Point lastClicked;
    private Logic logic;
    private ArrayList <Line> lines;

    public Timer timer;

    public Board (int level) {
        timer = new Timer ();
        logic = new Logic (Tile.TYPES, level);
        lines = new ArrayList <Line> ();

        tiles = new Tile [Logic.height] [Logic.width];
        Handler handler = new Handler ();
        ButtonGroup group = new ButtonGroup ();
        setLayout (new GridLayout (Logic.height, Logic.width));

        for (int i = 0; i < Logic.height; i++) {
            for (int j = 0; j < Logic.width; j++) {
                tiles [i] [j] = new Tile (logic.getTile (new Point (j, i)));
                tiles [i] [j].setActionCommand (j + ", " + i);
                tiles [i] [j].addActionListener (handler);
                group.add (tiles [i] [j]);
                add (tiles [i] [j]);
            }
        }
    }

    public boolean boardCleared () {
        return logic.boardCleared ();
    }

    public boolean needsShuffling () {
        return !logic.hasMatches () && !boardCleared ();
    }

    public void updateBoard () {
        for (int i = 0; i < Logic.height; i++) {
            for (int j = 0; j < Logic.width; j++) {
                int type = logic.getTile (new Point (j, i));
                if (tiles [i] [j].getType () != type) {
                    tiles [i] [j].setType (type);
                }
            }
        }
        repaint ();
    }

    //do i need to update the board when i change borders?
    public void showHint () { //should i clear the selection when i show the hint?
        clearHintBorder ();
        oldHint = logic.getHint ();
        if (oldHint == null) {
            logic.shuffle ();
            updateBoard ();
            oldHint = logic.getHint ();
        }
        System.out.println ("hint 1: (" + oldHint [0].x + ", " + oldHint [0].y + ")");
        System.out.println ("hint 2: (" + oldHint [1].x + ", " + oldHint [1].y + ")");
        tiles [oldHint [0].y] [oldHint [0].x].setHintBorder ();
        tiles [oldHint [1].y] [oldHint [1].x].setHintBorder ();
    }

    private void clearHintBorder () {
        if (oldHint != null) {
            for (int i = 0; i < 2; i++) tiles [oldHint [i].y] [oldHint [i].x].clearHintBorder ();
            oldHint = null;
        }
    }

    public void selectMatch (Point tile, Path path) {
        //System.out.println ("Match: (" + tile.x + ", " + tile.y + ")");
        clearHintBorder ();
        //tiles [tile.y] [tile.x].setSelectBorder ();;

        //drawing line to connect the match
        //assuming no padding around the edges
        /*int startX = (int) ((lastClicked.x + 0.5) * tileSize) + (lastClicked.x * tileGap);
        int startY = (int) ((lastClicked.y + 0.5) * tileSize) + (lastClicked.y * tileGap);
        for (Step step : path) {
            int xSteps = step.getDirection ().getX () * step.getSteps ();
            int ySteps = step.getDirection ().getY () * step.getSteps ();
            int endX = startX + xSteps * (tileSize + tileGap);
            int endY = startY + ySteps * (tileSize + tileGap);
            System.out.println ("Adding a line: startX = " + startX + ", startY = " + startY + ", endX = " + endX + ", endY = " + endY);
            lines.add (new Line (startX, startY, endX, endY));
            startX = endX;
            startY = endY;
        }

        repaint ();
        try {
            Thread.sleep (250);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        //lines.clear ();
        //tiles [tile.y] [tile.x].clearBorder ();
        tiles [lastClicked.y] [lastClicked.x].clearBorder ();
        timer.giveTimeBonus();
        lastClicked = null;
        if (needsShuffling()) logic.shuffle ();
        updateBoard ();
    }

    public void selectNonMatch (Point tile) {
        //System.out.println ("Non match: (" + tile.x + ", " + tile.y + ")");
        clearHintBorder ();
        if (lastClicked != null) tiles [lastClicked.y] [lastClicked.x].clearBorder ();
        tiles [tile.y] [tile.x].setSelectBorder ();;
        lastClicked = tile;
    }

    private void clickTile (int x, int y) {
        Point thisClick = new Point (x, y);

        if (lastClicked == null) selectNonMatch (thisClick);
        else {
            Path path = logic.removeMatch (thisClick, lastClicked);
            if (path == null) selectNonMatch (thisClick);
            else selectMatch (thisClick, path);
        }
    }

    @Override
    public void paint (Graphics g) {
        super.paint (g);
        Graphics2D graphics = (Graphics2D) g;
        graphics.setColor (Line.lineColor);
        graphics.setStroke (new BasicStroke (3));
        for (Line line: lines) graphics.drawLine (line.x1, line.y1, line.x2, line.y2);
    }

    class Handler implements ActionListener { //should I use an item listener instead?**************************
        @Override
        public void actionPerformed (ActionEvent event) {
            String actionCommand = event.getActionCommand ();
            int x = Integer.parseInt (actionCommand.substring (0, actionCommand.indexOf (',')));
            int y = Integer.parseInt (actionCommand.substring (actionCommand.indexOf (' ') + 1));
            clickTile (x, y);
        }
    }

    private static class Line {
        final int x1;
        final int y1;
        final int x2;
        final int y2;
        public static final Color lineColor = Color.black;

        public Line (int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
    }

    class Tile extends JRadioButton {
        public static final int padding = 5;
        public final static int borderWidth = 3;
        private static final Border hintBorder;
        private static final Border selectBorder;
        private static final Border emptyBorder = BorderFactory.createEmptyBorder (padding, padding, padding, padding);
        private static final String IMAGES [] = {"bat.png", "bee.png", "beetle.png", "buffalo.png", "bullfinch.png", "butterfly.png", "camel.png", "cat.png", "chameleon.png", "chicken.png", "clown-fish.png", "cow.png", "crab.png", "crocodile.png", "deer.png", "elephant.png", "flamingo.png", "fox.png", "frog.png", "giraffe.png", "hedgehog.png", "ladybug.png", "lion.png", "mouse.png", "owl.png", "panda.png", "parrot.png", "penguin.png", "pig.png", "platypus.png", "rabbit.png", "sheep.png", "sloth.png", "snake.png", "spider.png", "squid.png", "stingray.png", "turtle.png", "whale.png", "zebra.png"};
        private static final ImageIcon icons [];
        private static ImageIcon empty = new ImageIcon (Tile.class.getResource ("Resources/empty.png"));
        static final int TYPES = 40;
        private int type;

        static {
            icons = new ImageIcon [IMAGES.length];
            for (int i = 0; i < IMAGES.length; i++) {
                icons [i] = new ImageIcon (Tile.class.getResource ("Resources/Tiles/" + IMAGES [i]));
            }

            Border paddingBorder = BorderFactory.createEmptyBorder (padding - borderWidth, padding - borderWidth, padding - borderWidth, padding - borderWidth);
            Border hint = BorderFactory.createLineBorder (Color.red, borderWidth);
            Border select = BorderFactory.createLineBorder (Color.orange, borderWidth);
            hintBorder = BorderFactory.createCompoundBorder (paddingBorder, hint);
            selectBorder = BorderFactory.createCompoundBorder (paddingBorder, select);
        }

        //will assume type is within 0 and 39 and filetypes are jpg
        public Tile (int type) {
            super (getIconForType (type));
            this.type = type;
            setBorder (emptyBorder);
            setBorderPainted (true);
        }

        public void setType (int type) {
            setType (type, this.type); //supplies old type
        }

        public void setType (int newType, int oldType) {
            this.type = newType;
            if (oldType == -1 && newType != -1) setEnabled (true);
            if (newType == -1 && oldType != -1) setEnabled (false);
            super.setIcon (getIconForType (newType));
        }

        private static ImageIcon getIconForType (int type) {
            if (type == -1) return empty;
            return icons [type];
        }

        public int getType () {
            return type;
        }

        public void setHintBorder () {
            setBorder (hintBorder);
            repaint ();
        }

        public void clearHintBorder () {
            if (getBorder () == hintBorder) clearBorder ();
        }

        public void clearSelectBorder () {
            if (getBorder () == selectBorder) clearBorder ();
        }

        public void clearBorder () {
            setBorder (emptyBorder);
            repaint ();
        }

        public void setSelectBorder () {
            setBorder (selectBorder);
            repaint ();
        }
    }

    public class Timer {
        public static final int MAX_TIME = 600000;
        private long startTime;
        private long pauseTime;
        private boolean paused, started;

        public Timer () {
            paused = false;
            started = false;
        }

        public void start () {
            started = true;
            startTime = System.currentTimeMillis ();
        }

        public void pause () {
            if (!paused) {
                pauseTime = System.currentTimeMillis ();
                paused = true;
            }
        }

        public int getTimeLeft () { //will throw error if startTime not initialized
            if (!started) return (int) MAX_TIME;
            if (paused) return (int) (MAX_TIME - pauseTime + startTime);
            else return (int) (MAX_TIME - System.currentTimeMillis() + startTime);
        }

        public void resume () {
            if (paused) {
                startTime += System.currentTimeMillis () - pauseTime;
                paused = false;
            }
        }

        public void giveTimeBonus () {
            if (started) startTime += 500;
        }
    }
}
