import javax.swing.*;
import java.io.File;
import java.awt.*;
import java.util.ArrayList;

//change the shuffle method to shuffle only until a match is available
//random shuffling (vs switching in order)
//cache matches found before to prevent having to check multiple times to find the same matches
//will have to check whether those matches are still valid tho

class Logic {
    //40 type of tiles , 14 x 10 board
    private int level;
    private Tile board [] [];
    private GameState state;
    private long startTime, pauseTime;
    private int pairsLeft;
    Point [] hint;
    int hintsLeft;

    public Logic () {
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

        shuffle (70);
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

    public int getTileType (Point tile) {
        return getTile (tile).type;
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

    //hints?

    private Tile getTile (Point point) {
        return board [point.y] [point.x];
    }

    public boolean hasMatches () {
        for (int i = 0; i < 140; i++) {
            for (int j = i + 1; j < 140; j++) {
                Point tile1 = new Point (i / 14, i % 14);
                Point tile2 = new Point (j / 14, j % 14);
                if (getTile (tile1).equals (getTile (tile2)) && match (tile1, tile2) != null) {
                    this.hint = new Point [] {tile1, tile2};
                    return true;
                }
            }
        }

        return false;
    }

    public void shuffle () {
        while (!hasMatches ()) switchRandom ();
    }

    public void switchRandom () {
        switchTiles (nthTile (random (0, pairsLeft * 2 - 1)), nthTile (random (0, pairsLeft * 2 - 1)));
    }

    public void shuffle (int minRepetitions) {
        for (int i = 0; i < minRepetitions; i++) switchRandom ();

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
                if (getTile (new Point (j, i)) != null) {
                    count++;
                    if (count == index) return new Point (j, i);
                }
            }
        }

        return new Point (-1, -1);
    }

    private void switchTiles (Point tile1, Point tile2) {
        Tile temp = getTile (tile1);
        board [tile1.y] [tile1.x] = getTile (tile2);
        board [tile2.y] [tile2.x] = temp;
    }

    //NOTE: alignVertical and alignHorizontal are very similar
    public void alignTiles (int level) {
        switch (level) {
            case 3:
                alignUp (true);
                alignDown (true);
                break;
            case 4:
                alignRight (true);
                alignLeft (true);
                break;
            case 5:
                alignLeft (false);
                break;
            case 6:
                alignDown (false);
                break;
            case 7:
                alignUp (false);
                break;
            case 8:
                alignRight (false);
                break;
            case 9:
                alignMiddle ();
        }
    }

    private void alignLeft (boolean half) {
        alignHorizontal (0, half ? 7 : 14);
    }

    private void alignRight (boolean half) {
        alignHorizontal (13, half ? 6 : -1);
    }

    //not the method's responsibility to check that the start and end values are valid
    private void alignHorizontal (int start, int end) {
        int direction = (int) Math.signum (end - start);

        for (int i = 0; i < 10; i++) {
            for (int j = start; j - end != 0; j += direction) {
                if (getTile (new Point (j, i)) == null) {
                    boolean hasMoreTiles = false;

                    for (int k = j + direction; k - end != 0; k += direction) {
                        if (getTile (new Point (k, i)) != null) {
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
        alignVertical (0, half ? 5 : 10);
    }

    private void alignDown (boolean half) {
        alignVertical (9, half ? 4 : -1);
    }

    //not the method's responsibility to check that the start and end values are valid
    private void alignVertical (int start, int end) {
        int direction = (int) Math.signum (end - start);

        for (int i = 0; i < 14; i++) {
            for (int j = start; j - end != 0; j += direction) {
                if (getTile (new Point (i, j)) == null) {
                    boolean hasMoreTiles = false;

                    for (int k = j + direction; k - end != 0; k += direction) {
                        if (getTile (new Point (i, k)) != null) {
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

    private void alignMiddle () {
        ArrayList <Point> holes = findHoles ();

        while (holes.size () != 0) {
            for (Point point : holes) {
                //will pick horizontal align over vertical align if both are the same
                if (Math.abs (4.5 - point.y) / 5 < Math.abs (6.5 - point.x) / 7) {
                    if (point.y < 5) alignVertical (point.y, 0);
                    else alignVertical (point.y, 10);
                } else {
                    if (point.x < 7) alignHorizontal (point.x, 0);
                    else alignHorizontal (point.x, 14);
                }
            }

            holes = findHoles ();
        }
    }

    //returns outermost gap
        private ArrayList <Point> findHoles () {
        ArrayList <Point> holes = new ArrayList <Point> ();

        for (int i = 0; i < 10; i++) {
            ArrayList <Point> outerRing = nthOuterRing (i);

            for (Point point : outerRing) {
                if (hole (point)) holes.add (point);
            }
        }

        return holes;
    }

    private boolean hole (Point point) {
        if (getTile (point) != null) return false;

        int count = 0;

        for (Direction direction: Direction.values ()) {
            Point step = new Point (point.x + direction.x, point.y + direction.y);
            if (invalidPos (step) || getTile (step) == null) count++;
            if (count >= 3) return true;
        }

        return false;
    }

    private ArrayList <Point> nthOuterRing (int n) {
        ArrayList <Point> ring = new ArrayList <Point> ();

        for (int j = n; j < 10 - n; j += 9 - 2 * n) {
            for (int i = n; i < 14 - n; i++) {
                ring.add (new Point (i, j));
            }
        }

        for (int j = n; j < 14 - n; j += 13 - 2 * n) {
            for (int i = n + 1; i < 10 - n - 1; i++) {
                ring.add (new Point (j, i));
            }
        }

        return ring;
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
        if (getTile (tile1).equals (getTile (tile2))) return null;
        return match (tile1, tile2, new ArrayList <Step> ());
    }

    private ArrayList <Step> match (Point curPos, Point tile2, ArrayList <Step> history) {
        if (curPos.equals (tile2)) return history;
        if (invalidPos (curPos) || history.size () >= 3) return null;

        ArrayList <Step> path = null;

        for (Direction direction : Direction.values()) {
            ArrayList <Step> newPath = match (newPos (curPos, direction), tile2, addStep (history, direction));

            if (newPath != null && (path == null || path.size () > newPath.size () || (path.size () == newPath.size () && length (newPath) < length (path)))) {
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

        if (!(xOnBorder || yOnBorder) && getTile (pos) == null) return true;

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