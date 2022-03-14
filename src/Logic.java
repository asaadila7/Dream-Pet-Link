import java.awt.Point;
import java.util.ArrayList;

//Changes: no longer handles time or returns a game state: has only a boolean to keep track of whether all the pairs have been matched
//got rid of assertPlaying () as well. the calling class can handle that

public class Logic {
    public static final int height = 10, width = 14; //even numbers are nice
    private final int level;
    private int pairsLeft;
    private int board [] [];
    private boolean boardCleared;
    private Point [] hint;

    //will assume tileTypes is > 0  and level is from 1 to 9
    public Logic (int tileTypes, int level) {
        this.level = level;
        board = new int [height] [width];
        pairsLeft = height * width / 2;
        boardCleared = false;

        int count = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                board [i] [j] = count % tileTypes;
                count++;
            }
        }

        for (int i = 0; i < pairsLeft; i++) switchRandom ();
        shuffle (); //just in case switching got rid of all the matches
    }

    public boolean boardCleared () {
        return boardCleared;
    }

    //will occasionally return null;
    public Point [] getHint () {
        if (hint != null) return hint;
        hasMatches (); //will set hint for me (or it will just stay null if there are no matches left)
        return hint;
    }

    public int getTile (Point tile) {
        return board [tile.y] [tile.x];
    }

    //should i make this return a boolean?
    public void removeMatch (Point tile1, Point tile2) { //should this return a boolean?
        if (null == match (tile1, tile2)) return;
        board [tile1.y] [tile1.x] = -1;
        board [tile2.y] [tile2.x] = -1;
        pairsLeft--;
        if (pairsLeft == 0) boardCleared = true;
        alignTiles ();
    }

    public void shuffle () {
        while (!hasMatches ()) switchRandom ();
    }

    private void switchRandom () {
        switchTiles (nthTile (random (0, pairsLeft * 2 - 1)), nthTile (random (0, pairsLeft * 2 - 1)));
    }

    private static int random (int min, int max) {
        return (int) (Math.random () * (max - min + 1)) + min;
    }

    //returns point corresponding to nth non-empty tile
    //will assume the board has at least n non-empty tiles
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

        return null;
    }

    private void switchTiles (Point tile1, Point tile2) {
        int temp = getTile (tile1);
        board [tile1.y] [tile1.x] = getTile (tile2);
        board [tile2.y] [tile2.x] = temp;
    }

    public boolean hasMatches () {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Point curPos = new Point (j, i);
                if (getTile (curPos) == -1) continue;
                if (canMatch (curPos, new Path (), curPos)) return true;
            }
        }

        return false;
    }

    private boolean canMatch (Point tile, Path history, Point curPos) {
        if (!validPos (tile)) return false;

        int size = history.size ();
        int tileType = getTile (tile);

        if (size > 3) return false;
        if (validPos (curPos) && getTile (curPos) == tileType && size > 0) { //no way to return to the same tile in <=3 moves
            hint = new Point [] {tile, curPos};
            return true;
        }

        for (Step.Direction direction : Step.Direction.values ()) {
            Point newPos = newPos (curPos, direction);
            if (invalidPosForPath (newPos) && (!validPos (newPos) || getTile (newPos) != tileType)) continue;
            if (canMatch (tile, history.addStep (direction), newPos)) return true;
        }

        return false;
    }

    //will return null if tiles cannot be matched, will otherwise return a list of steps to get from one tile to the other
    public Path match (Point tile1, Point tile2) {
        if (getTile (tile1) == -1 || tile1.equals (tile2) || getTile (tile1) != getTile (tile2)) return null;
        return match (tile1, tile2, new Path ());
    }

    private Path match (Point curPos, Point destinationPos, Path history) {
        System.out.println ("CurPos: " + curPos);
        //keep this order!
        if (history.size () > 3) {
            System.out.println ("More than three moves");
            return null;
        }
        if (curPos.equals (destinationPos)) return history;
        if (invalidPosForPath (curPos) && history.size () != 0) {
            System.out.println ("Invalid pos for path: (" + curPos.x + ", " + curPos.y + ")");
            return null;
        }

        Path path = null;

        for (Step.Direction direction: Step.Direction.values()) {
            //preventing searching in the exact opposite direction
            if (curPos.x == destinationPos.x && Math.signum (curPos.y - destinationPos.y) == direction.getY ()) continue;
            if (curPos.y == destinationPos.y && Math.signum (curPos.x - destinationPos.x) == direction.getX ()) continue;

            Path newPath = match (newPos (curPos, direction), destinationPos, history.addStep (direction));

            //want the path that is shortest in terms of lines (most important, must be <= 3) and in terms of length
            if (newPath != null && newPath.size() <= 3 && (null == path || path.size () > newPath.size () || (path.size () == newPath.size () && path.length () > newPath.length ()))) {
                System.out.println ("Path has been overwritten");
                path = newPath;
            }
        }

        return path;
    }

    //no guarantee that newPos is valid
    private Point newPos (Point curPos, Step.Direction direction) {
        Point newPos = (Point) curPos.clone ();
        newPos.x += direction.getX ();
        newPos.y += direction.getY ();
        return newPos;
    }

    //checks that there is empty space for a line to pass through
    private boolean invalidPosForPath (Point pos) {
        if (validPos (pos)) {
            if (getTile (pos) == -1) return false;
            return true;
        }

        //return false if more than one tile off the edge
        if (pos.x < -1 || pos.x > width) return true;
        if (pos.y < -1 || pos.y > height) return true;

        if ((pos.x == -1 || pos.x == width) && (pos.y == -1 || pos.y == height)) return true;
        return false;
    }

    private boolean validPos (Point pos) {
        if (pos.x >= width || pos.x < 0) return false;
        if (pos.y >= height || pos.y < 0) return false;
        return true;
    }

    //NOTE: alignVertical and alignHorizontal are very similar
    public void alignTiles () {
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
    //will align up to end - 1
    private void alignHorizontal (int start, int end) {
        int direction = (int) Math.signum (end - start);

        for (int i = 0; i < height; i++) {
            for (int j = start; j != end; j += direction) {
                if (getTile (new Point (j, i)) == -1) {
                    int k = j + direction;
                    while (k != end && getTile (new Point (k, i)) == -1) k += direction;
                    if (k == end) break;
                    else switchTiles (new Point (k, i), new Point (j, i));
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
            for (int j = start; j != end; j += direction) {
                if (getTile (new Point (i, j)) == -1) {
                    int k = j + direction;
                    while (k != end && getTile (new Point (i, k)) == -1) k += direction;
                    if (k == end) break;
                    else switchTiles (new Point (i, k), new Point (i, j));
                }
            }
        }
    }

    /* alternate alignMiddle that fixes all the holes in order before it goes back around to fix the holes caused by fixing the old holes
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
    */

    private void alignMiddle () {
        Point hole = findOuterMostHole ();

        while (hole != null) {
            if (Math.abs ((float) (height - 1) / 2 - hole.y) / height < Math.abs ((float) (width - 1) / 2 - hole.x) / width) {
                if (hole.y < height / 2) alignVertical (hole.y, -1);
                else alignVertical (hole.y, height);
            } else {
                if (hole.x < width / 2) alignHorizontal (hole.x, -1);
                else alignHorizontal (hole.x, width);
            }
        }
    }

    private Point findOuterMostHole () {
        for (int i = 0; i < height; i++) {
            ArrayList <Point> outerRing = nthOuterRing (i);
            for (Point point: outerRing) {
                if (hole (point)) return point;
            }
        }

        return null;
    }

    private boolean hole (Point point) {
        if (getTile (point) != -1) return false;

        int count = 0;

        for (Step.Direction direction: Step.Direction.values ()) {
            Point step = newPos (point, direction);
            if (!validPos (step) || getTile (step) == -1) count++;
            if (count >= 3) return true;
        }

        return false;
    }

    private ArrayList <Point> nthOuterRing (int n) {
        ArrayList <Point> ring = new ArrayList <Point> ();

        //check this
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
}
