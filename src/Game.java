import javax.swing.*;
import java.io.File;
import java.awt.*;
import java.util.ArrayList;

class Game {
    //40 type of tiles , 14 x 10 board
    private int level;
    private Tile board [] [];
    private GameState state;
    private long startTime, pauseTime;
    private int pairsLeft;
    Point [] hint;
    int hintsLeft;

    public Game () {
        level = 1;
        board = new Tile [10] [14];
        state = GameState.PLAYING;
        startTime = pauseTime = System.currentTimeMillis ();
        pairsLeft = 70;

        //initialize board to have an equal amount of each tile (as much as possible)
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 14; j += 2) {
                board [i] [j] = new Tile ((i * 14 + j) / 2 % 40);
                board [i] [j + 1] = new Tile ((i * 14 + j) / 2 % 40);
            }
        }

        shuffle ();
    }

    static class Tile {
        static String images [] = {"Black Cracks.jpg", "Blue Leaves.jpg", "Blue Swirl Painting.jpg", "Bricks.jpg", "Cabbage.jpg", "Cracked Ice.jpg", "Cracked Wall.jpg", "Dewdrops on Purple Leaf.jpg", "Dewdrops on Orange Flower.jpg", "Ferns.jpg", "Fire.jpg", "Golden Maple Leaves.jpg", "Green Cut Glass.png", "Grey Abstract.jpg", "Leaves on a Tree.jpg", "Lemon Bubbles.jpg", "Lemon Wedge.jpg", "Maple Leaves.jpg", "Mossy Rock Face.jpg", "Night Sky.jpg", "Orange Maple Leaves.jpg", "Orange Sunset.jpg", "Orange Swirl Painting.jpg", "Pink and Purple Smoke.jpg", "Pink Clouds.jpg", "Pink Flowers.jpg", "Purple Feathers.jpg", "Purple Flowers.jpg", "Purple Oil Painting.jpg", "Red Abstract Painting.jpg", "Red Cut Glass.png", "Red Leaf.jpg", "Rock Wall.jpg", "Sea Foam.jpg", "Smoke.jpg", "Sunset with Trees.jpg", "Tree Bark.jpg", "Virus.jpg", "White Silk.jpg", "White Stones.jpg"};

        private ImageIcon icon;
        private int type; //int to make it easy to initialize and also to not have to compare images to compare tiles

        public Tile (int type) {
            this.type = type;
            this.icon = new ImageIcon (new File ("./Tiles/" + images [type]).getPath ());
        }

        public boolean equals (Tile other) {
            return type == other.getType ();
        }

        public ImageIcon getIcon () {
            return icon;
        }

        public int getType () {
            return type;
        }
    }

    static enum GameState {
        PLAYING,
        WON_GAME,
        OVER,
        PAUSED,
        LOST_LEVEL,
        WON_LEVEL
    }

    public int getLevel () {
        return level;
    }

    public int getTile (Point tile) {
        return board [tile.y] [tile.x].type;
    }

    public GameState getState () {
        if (state == GameState.PAUSED) return state;

        if (getTime () > 600000) {
            state = lostLevel();
        }

        if (pairsLeft == 0) {
            state = wonLevel();
        }

        return state;
    }

    public long getTime () {
        return System.currentTimeMillis () - startTime;
    }

    public void removeTile (Point tile) {
        board [tile.y] [tile.x] = null;
    }

    //checking won or lost is up to other class
    private GameState lostLevel () {
        level--;
        if (level < 1) return GameState.OVER;
        return GameState.LOST_LEVEL;
    }

    private GameState wonLevel () {
        level++;
        if (level > 9) return GameState.WON_GAME;
        return GameState.WON_LEVEL;
    }

    //hints, board shifting

    public boolean hasMatches () {
        for (int i = 0; i < 140; i++) {
            for (int j = i + 1; j < 140; j++) {
                Point tile1 = new Point (i / 14, i % 14);
                Point tile2 = new Point (j / 14, j % 14);
                if (board [tile1.y] [tile1.x].equals (board [tile2.y] [tile2.x]) && match (tile1, tile2) != null) {
                    this.hint = new Point [] {tile1, tile2};
                    return true;
                }
            }
        }

        return false;
    }

    public void shuffle () {
        for (int i = 0; i < pairsLeft; i++) {
            switchTiles (nthTile (random (0, pairsLeft - 1)), nthTile (random (pairsLeft, pairsLeft * 2 - 1)))
        }

        if (hasMatches ()) return;
        shuffle ();
    }

    private static int random (int min, int max) {
        return (int) (Math.random () * (max - min + 1) + min);
    }

    //returns nth non empty tile
    private Point nthTile (int index) {
        int count = -1;

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 14; j++) {
                if (board [i] [j] != null) {
                    count++;
                    if (count == index) return new Point (j, i);
                }
            }
        }

        return new Point (-1, -1);
    }

    private void switchTiles (Point tile1, Point tile2) {
        Tile temp = board [tile1.y] [tile1.x];
        board [tile1.y] [tile1.x] = board [tile2.y] [tile2.x];
        board [tile2.y] [tile2.x] = temp;
    }

    public void alignTiles (int level) {
        switch (level) {
            case 3:
                alignUpAndDown();
                break;
            case 4:
                alignRightAndLeft();
                break;
            case 5:
                alignLeft(false);
                break;
            case 6:
                alignDown(false);
                break;
            case 7:
                alignUp(false);
                break;
            case 8:
                alignRight(false);
                break;
            case 9:
                alignMiddle ();
        }
    }

    private void alignRightAndLeft () {
        alignRight (true);
        alignLeft (true);
    }

    private void alignUpAndDown () {
        alignDown (true);
        alignUp (true);
    }

    public void align (Direction direction, boolean half) {
        int horizontalBound = 14, verticalBound = 10;
        if (half) {
            
        }
    }

    private void alignRight (boolean half) {
        int bound = half ? 7 : 14;

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < bound; j++) {
                if (board [i] [j] == null) {
                    boolean hasMoreTiles = false;

                    for (int k = j + 1; k < bound; k++) {
                        if (board [i] [k] != null) {
                            switchTiles (new Point (j, i), new Point (k, i));
                            hasMoreTiles = true;
                            break;
                        }
                    }

                    if (!hasMoreTiles) break;
                }
            }
        }
    }

    private void alignLeft (boolean half) {
        int bound = half ? 6 : -1;

        for (int i = 0; i < 10; i++) {
            for (int j = 13; j > bound; j--) {
                if (board [i] [j] == null) {
                    boolean hasMoreTiles = false;

                    for (int k = j - 1; k > bound; k--) {
                        if (board [i] [k] != null) {
                            switchTiles (new Point (j, i), new Point (k, i));
                            hasMoreTiles = true;
                            break;
                        }
                    }

                    if (!hasMoreTiles) break;
                }
            }
        }
    }

    private void alignUp (boolean half) {

    }

    private void alignDown (boolean half) {

    }

    private void alignMiddle () {

    }

    public void pause () {
        pauseTime = System.currentTimeMillis ();
        state = GameState.PAUSED;
    }

    public void resume () {
        if (state == GameState.PAUSED) {
            state = GameState.PLAYING;
            startTime += System.currentTimeMillis () - pauseTime;
        }
    }

    //keep track of matches made and give time bonus
    public ArrayList <Step> match (Point tile1, Point tile2) {
        return match (tile1, tile2, new ArrayList <Step> ());
    }

    private ArrayList <Step> match (Point curPos, Point tile2, ArrayList <Step> history) {
        if (curPos.equals (tile2)) return history;
        if (invalidPos (curPos) || history.size () >= 3) return null;

        ArrayList <Step> path = null;

        for (Direction direction : Direction.values()) {
            ArrayList <Step> newPath = match (newPos (curPos, direction), tile2, addStep (history, direction));

            if (newPath != null && (path == null || length (newPath) > length (path))) {
                path = newPath;
            }
        }

        return path;
    }

    private boolean invalidPos (Point pos) {
        if (pos.y < -1 || pos.x < -1 || pos.y >= 10 || pos.x >= 14) return false;

        boolean xOnBorder = false, yOnBorder = false;
        if (pos.x == -1 || pos.x == 14) xOnBorder = true;
        if (pos.y == -1 || pos.y == 10) yOnBorder = true;

        if (xOnBorder ^ yOnBorder) return true;

        if (!(xOnBorder || yOnBorder) && board [pos.y] [pos.x] == null) return true;

        return false;
    }

    static private int length (ArrayList <Step> path) {
        int length = 0;
        for (Step step : path) {
            length += step.stepCount;
        }
        return length;
    }

    static private ArrayList <Step> addStep (ArrayList <Step> history, Direction direction) {
        if (history.size () != 0 && history.get(history.size () - 1).direction.equals (direction)) {
            history.add (new Step (direction, history.get (history.size () - 1).stepCount + 1));
            history.remove (history.size () - 2);
        } else {
            history.add (new Step (direction, 1));
        }

        return history;
    }

    static private Point newPos (Point point, Direction direction) {
        point.x += direction.x;
        point.y += direction.y;
        return point;
    }

    enum Direction {
        UP (0, -1),
        DOWN (0, 1),
        RIGHT (1, 0),
        LEFT (-1, 0);

        private int x, y;

        private Direction (int x, int y) {
            //Maybe signum here if I wanted to make this more widely useable
            this.x = x;
            this.y = y;
        }

        public int getX () {
            return x;
        }

        public int getY () {
            return y;
        }
        
        public boolean equals (Direction other) {
            return x == other.getX () && y == other.getY ();
        }
    }

    static class Step {
        private Direction direction;
        private int stepCount;

        public Step (Direction direction, int stepCount) {
            this.direction = direction;
            this.stepCount = stepCount;
        }

        public Direction getDirection () {
            return direction;
        }

        public int getSteps () {
            return stepCount;
        }
    }
}