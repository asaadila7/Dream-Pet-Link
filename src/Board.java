import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

//time bonus when match found
public class Board extends JPanel {
    private static final Border hintBorder = BorderFactory.createLineBorder(Color.orange);
    private static final Border selectBorder = BorderFactory.createLineBorder(Color.yellow); //optional int param to specify thickness of border in pixels
    private static final Color lineColor = Color.yellow;
    private static final int tileSize = 30; //in pixels
    private static final int tileGap = 2;

    private Tile [] [] tiles;
    private Point oldHint [];
    private Point lastClicked;
    private Logic logic;
    private ArrayList <Line> lines;

    public Board (int level) {
        logic = new Logic (Tile.TYPES, level);
        lines = new ArrayList <Line> ();

        tiles = new Tile [Logic.height] [Logic.width];
        Handler handler = new Handler ();
        ButtonGroup group = new ButtonGroup ();
        setLayout (new GridLayout (Logic.height, Logic.width, tileGap, tileGap));

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
        return !logic.hasMatches ();
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

        //revalidate ();***********************************
        repaint ();
    }

    //do i need to update the board when i change borders?
    public void showHint () {
        clearHintBorder ();
        if (logic.getHint () == null) {
            logic.shuffle ();
            updateBoard ();
        }
        oldHint = logic.getHint ();
        tiles [oldHint [0].y] [oldHint [0].x].setBorder (hintBorder);
        tiles [oldHint [1].y] [oldHint [1].x].setBorder (hintBorder);
    }

    private void clearHintBorder () {
        if (oldHint != null) {
            for (int i = 0; i < 2; i++) {
                if (tiles [oldHint [i].y] [oldHint [i].x].getBorder () == hintBorder) tiles [oldHint [i].y] [oldHint [i].x].setBorder (null); //may not work: maybe try overriding paintBorder or set and empty border
            }
            oldHint = null;
        }
    }

    public void selectMatch (Point tile, Path path) {
        System.out.println ("Match: (" + tile.x + ", " + tile.y + ")");
        clearHintBorder ();
        tiles [tile.y] [tile.x].setBorder (selectBorder);

        //drawing line to connect the match
        //assuming no padding around the edges
        int startX = (int) ((lastClicked.x + 0.5) * tileSize) + (lastClicked.x * tileGap);
        int startY = (int) ((lastClicked.y + 0.5) * tileSize) + (lastClicked.y * tileGap);
        for (Step step : path) {
            int endX = startX + (step.getDirection ().getX () * step.getSteps ());
            int endY = startY + (step.getDirection ().getY () * step.getSteps ());
            lines.add (new Line (startX, startY, endX, endY));
            startX = endX;
            startY = endY;
        }

        repaint ();
        try {
            Thread.sleep (500);
        } catch (Exception e) {
            e.printStackTrace();
        }

        lines.clear ();
        repaint ();

        lastClicked = null;
    }

    public void selectNonMatch (Point tile) {
        System.out.println ("Non match: (" + tile.x + ", " + tile.y + ")");
        clearHintBorder ();
        if (lastClicked != null) tiles [lastClicked.y] [lastClicked.x].setBorder (null);
        tiles [tile.y] [tile.x].setBorder (selectBorder);
        lastClicked = tile;
    }

    private void clickTile (int x, int y) {
        Point thisClick = new Point (x, y);

        if (lastClicked == null) {
            System.out.println ("Last clicked is null");
            selectNonMatch (thisClick);
        } else {
            Path path = logic.match (lastClicked, thisClick);
            if (path == null) {
                System.out.println ("Path is null");
                selectNonMatch (thisClick);
            } else {
                selectMatch (thisClick, path);
                if (!logic.hasMatches ()) logic.shuffle (); //should time be paused when shuffling?
                updateBoard (); //should i move this out of if block?
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent (g);
        g.setColor (lineColor);
        for (Line line: lines) g.drawLine (line.x1, line.y1, line.x2, line.y2);
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

    private static class Line{
        final int x1;
        final int y1;
        final int x2;
        final int y2;

        public Line(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
    }

    class Tile extends JRadioButton {
        static final int TYPES = 40;
        private static final String IMAGES [] = {"Black Cracks.jpg", "Blue Leaves.jpg", "Blue Swirl Painting.jpg", "Bricks.jpg", "Cabbage.jpg", "Cracked Ice.jpg", "Cracked Wall.jpg", "Dewdrops on Orange Flower.jpg", "Dewdrops on Purple Leaf.jpg", "Ferns.jpg", "Fire.jpg", "Golden Maple Leaves.jpg", "Green Cut Glass.png", "Grey Abstract.jpg", "Leaves on a Tree.jpg", "Lemon Bubbles.jpg", "Lemon Wedge.jpg", "Maple Leaves.jpg", "Mossy Rock Face.jpg", "Night Sky.jpg", "Orange Maple Leaves.jpg", "Orange Sunset.jpg", "Orange Swirl Painting.jpg", "Pink and Purple Smoke.jpg", "Pink Clouds.jpg", "Pink Flowers.jpg", "Purple Feathers.jpg", "Purple Flowers.jpg", "Purple Oil Painting.jpg", "Red Abstract Painting.jpg", "Red Cut Glass.png", "Red Leaf.jpg", "Rock Wall.jpg", "Sea Foam.jpg", "Smoke.jpg", "Sunset with Trees.jpg", "Tree Bark.jpg", "Virus.jpg", "White Silk.jpg", "White Stones.jpg"};
        private int type;

        //will assume type is within 0 and 39 and filetypes are jpg
        public Tile (int type) {
            super (getIconForType (type));
            this.type = type;
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
            if (type == -1) return new ImageIcon (Tile.class.getResource ("Resources/empty.png"));
            return new ImageIcon (Tile.class.getResource ("Resources/Tiles/" + IMAGES [type]));
        }

        public int getType () {
            return type;
        }
    }
}
