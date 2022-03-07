import java.util.ArrayList;
import java.awt.Point;

//cache matches found before to prevent having to check multiple times to find the same matches
//will have to check whether those matches are still valid tho
//get rid of assert playing in some of the methods

class Logic {
    //40 type of tiles , 14 x 10 board
    public final static long MAX_TIME = 600000L;
    private int level;
    private int board [] [];
    private GameState state;
    private long startTime, pauseTime;
    private int pairsLeft;
    private int height, width;
    private int tileTypes;
    private Point [] hint;

    public Logic (int tileTypes, int height, int width) {
        level = 1;
        board = new int [height] [width];
        this.tileTypes = tileTypes;
    }

    private void assertPlaying () {
        if (state != GameState.PLAYING) throw new Error ("Invalid Game State: should be playing"); //generic error?
    }

    public void setUpLevel () {
        if (state == GameState.PAUSED || state == GameState.PLAYING || state == GameState.OVER || state == GameState.WON_GAME) {
            throw new Error ("Cannot start level: invalid game state");
        }

        pairsLeft = height * width / 2;

        //initialize board to have an equal amount of each tile (as much as possible)
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j += 2) {
                board [i] [j] = (i * width + j) / 2 % tileTypes;
                board [i] [j + 1] = (i * width + j) / 2 % tileTypes;
            }
        }

        shuffle (pairsLeft);
    }

    public void startLevel () {
        state = GameState.PLAYING;
        startTime = pauseTime = System.currentTimeMillis ();
    }

    public static enum GameState {
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
        return board [tile.y] [tile.x];
    }

    public void updateState () {
        if (state == GameState.PLAYING) {
            if (getTimeLeft () <= 0) { //hardcoded time limit
                state = lostLevel();
            }
    
            if (pairsLeft == 0) {
                state = wonLevel();
            }
        }
    }

    public GameState getState () {
        if (state == null) return state;
        updateState ();
        return state;
    }

    public int getTimeLeft () {
        if (getState() != GameState.PLAYING) {
            return (int) (MAX_TIME - (pauseTime - startTime));
        }
        return (int) (MAX_TIME - (System.currentTimeMillis () - startTime));
    }

    public Point [] getHint () {
        assertPlaying();
        return hint;
    }

    public void removeHint () {
        removeMatch (hint [0], hint [1]);
    }

    public boolean removeMatch (Point tile1, Point tile2) {
        assertPlaying();
        if (match (tile1, tile2) == null) return false;
        board [tile1.y] [tile1.x] = -1;
        board [tile2.y] [tile2.x] = -1;
        pairsLeft--;
        alignTiles ();
        return true;
    }

    //checking won or lost is up to class that uses this one
    private GameState lostLevel () {
        pause ();
        level--;
        if (level < 1) return GameState.OVER;
        return GameState.LOST_LEVEL;
    }

    private GameState wonLevel () {
        pause ();
        level++;
        if (level > 9) return GameState.WON_GAME;
        return GameState.WON_LEVEL;
    }

    public boolean hasMatches () {
        assertPlaying();
        for (int i = 0; i < pairsLeft * 2; i++) {
            for (int j = i + 1; j < pairsLeft * 2; j++) {
                Point tile1 = nthTile (i);
                Point tile2 = nthTile (j);
                if (getTile (tile1) != -1 && getTile (tile1) == (getTile (tile2)) && match (tile1, tile2) != null) {
                    this.hint = new Point [] {tile1, tile2};
                    return true;
                }
            }
        }

        return false;
    }

    private void switchRandom () {
        switchTiles (nthTile (random (0, pairsLeft * 2 - 1)), nthTile (random (0, pairsLeft * 2 - 1)));
    }

    public void shuffle () {
        assertPlaying();
        while (!hasMatches ()) switchRandom ();
    }
    
    public void shuffle (int minRepetitions) {
        assertPlaying();
        for (int i = 0; i < minRepetitions; i++) switchRandom ();
        shuffle ();
    }

    private static int random (int min, int max) {
        return (int) (Math.random () * (max - min + 1) + min);
    }

    //returns nth non empty tile
    private Point nthTile (int index) {
        int count = -1;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (getTile (new Point (j, i)) != -1) {
                    count++;
                    if (count == index) return new Point (j, i);
                }
            }
        }

        return new Point (-1, -1);
    }

    private void switchTiles (Point tile1, Point tile2) {
        int temp = getTile (tile1);
        board [tile1.y] [tile1.x] = getTile (tile2);
        board [tile2.y] [tile2.x] = temp;
    }

    //NOTE: alignVertical and alignHorizontal are very similar
    public void alignTiles () {
        assertPlaying();
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
        alignHorizontal (0, half ? width / 2 : width);
    }

    private void alignRight (boolean half) {
        alignHorizontal (width - 1, half ? (width - 1) / 2 : -1);
    }

    //not the method's responsibility to check that the start and end values are valid
    private void alignHorizontal (int start, int end) {
        int direction = (int) Math.signum (end - start);

        for (int i = 0; i < height; i++) {
            for (int j = start; j - end != 0; j += direction) {
                if (getTile (new Point (j, i)) == -1) {
                    boolean hasMoreTiles = false;

                    for (int k = j + direction; k - end != 0; k += direction) {
                        if (getTile (new Point (k, i)) != -1) {
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
        alignVertical (0, half ? height / 2 : height);
    }

    private void alignDown (boolean half) {
        alignVertical (height - 1, half ? (height - 1) / 2: -1);
    }

    //not the method's responsibility to check that the start and end values are valid
    private void alignVertical (int start, int end) {
        int direction = (int) Math.signum (end - start);

        for (int i = 0; i < width; i++) {
            for (int j = start; j - end != 0; j += direction) {
                if (getTile (new Point (i, j)) == -1) {
                    boolean hasMoreTiles = false;

                    for (int k = j + direction; k - end != 0; k += direction) {
                        if (getTile (new Point (i, k)) != -1) {
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
                if (Math.abs ((float) (height - 1) / 2 - point.y) / (height / 2) < Math.abs ((float) (width - 1) / 2 - point.x) / (width / 2)) {
                    if (point.y < height / 2) alignVertical (point.y, 0);
                    else alignVertical (point.y, height);
                } else {
                    if (point.x < width / 2) alignHorizontal (point.x, 0);
                    else alignHorizontal (point.x, width);
                }
            }

            holes = findHoles ();
        }
    }

    //returns outermost gap
    private ArrayList <Point> findHoles () {
        ArrayList <Point> holes = new ArrayList <Point> ();

        for (int i = 0; i < height; i++) {
            ArrayList <Point> outerRing = nthOuterRing (i);

            for (Point point : outerRing) {
                if (hole (point)) holes.add (point);
            }
        }

        return holes;
    }

    private boolean invalidPos (Point point) {
        return point.x >= 0 && point.x < width && point.y >= 0 && point.y < height;
    }

    private boolean hole (Point point) {
        if (getTile (point) != -1) return false;

        int count = 0;

        for (Step.Direction direction: Step.Direction.values ()) {
            Point step = new Point (point.x + direction.getX (), point.y + direction.getY ());
            if (invalidPos (step) || getTile (step) == -1) count++;
            if (count >= 3) return true;
        }

        return false;
    }

    private ArrayList <Point> nthOuterRing (int n) {
        ArrayList <Point> ring = new ArrayList <Point> ();

        for (int j = n; j < height - n; j += height - 1 - (2 * n)) {
            for (int i = n; i < width - n; i++) {
                ring.add (new Point (i, j));
            }
        }

        for (int j = n; j < width - n; j += width - 1 - (2 * n)) {
            for (int i = n + 1; i < height - n - 1; i++) {
                ring.add (new Point (j, i));
            }
        }

        return ring;
    }

    public void pause () {
        if (getState () == GameState.PLAYING) {
            pauseTime = System.currentTimeMillis ();
            state = GameState.PAUSED;
        }
    }

    public void resume () {
        if (state == GameState.PAUSED) {
            state = GameState.PLAYING;
            startTime += System.currentTimeMillis () - pauseTime;
        }
    }

    public void giveTimeBonus () {
        assertPlaying();
        startTime += 250;
    }

    public ArrayList <Step> match (Point tile1, Point tile2) {
        assertPlaying();
        if (getTile (tile1) == (getTile (tile2))) return null;
        return match (tile1, tile2, new ArrayList <Step> ());
    }

    private ArrayList <Step> match (Point curPos, Point tile2, ArrayList <Step> history) {
        if (curPos.equals (tile2)) return history;
        if (invalidPosForPath (curPos) || history.size () >= 3) return null;

        ArrayList <Step> path = null;

        for (Step.Direction direction : Step.Direction.values()) {
            ArrayList <Step> newPath = match (newPos (curPos, direction), tile2, addStep (history, direction));

            if (newPath != null && (path == null || path.size () > newPath.size () || (path.size () == newPath.size () && length (newPath) < length (path)))) {
                path = newPath;
            }
        }

        return path;
    }

    private boolean invalidPosForPath (Point pos) {
        if (pos.y < -1 || pos.x < -1 || pos.y > height || pos.x > width) return false;

        boolean xOnBorder = false, yOnBorder = false;
        if (pos.x == -1 || pos.x == width) xOnBorder = true;
        if (pos.y == -1 || pos.y == height) yOnBorder = true;

        if (xOnBorder ^ yOnBorder) return true;

        if (!(xOnBorder || yOnBorder) && getTile (pos) == -1) return true;

        return false;
    }

    static private int length (ArrayList <Step> path) {
        int length = 0;
        for (Step step : path) {
            length += step.getSteps ();
        }
        return length;
    }

    static private ArrayList <Step> addStep (ArrayList <Step> history, Step.Direction direction) {
        if (history.size () != 0 && history.get(history.size () - 1).getDirection ().equals (direction)) {
            history.add (new Step (direction, history.get (history.size () - 1).getSteps () + 1));
            history.remove (history.size () - 2);
        } else {
            history.add (new Step (direction, 1));
        }

        return history;
    }

    static private Point newPos (Point point, Step.Direction direction) {
        point.x += direction.getX ();
        point.y += direction.getY ();
        return point;
    }
}