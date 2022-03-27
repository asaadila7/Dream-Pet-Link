import java.awt.Point;

//Changes: no longer handles time or returns a game state: has only a boolean to keep track of whether all the pairs have been matched
//got rid of assertPlaying () as well. the calling class can handle that
//maybe combine board and logic classes
//before shuffling clear all borders
//point can be translated instead of making new points all the time. maybe come back and clean this up later

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

        for (int i = 0; i < pairsLeft; i++) {
            board [i / width] [i % width] = count;
            board [(i + pairsLeft) / width] [(i + pairsLeft) % width] = count;
            count++;
            count %= tileTypes;
        }

        for (int i = 0; i < pairsLeft; i++) switchRandom ();
        shuffle (); //just in case switching got rid of all the matches
    }

    public boolean boardCleared () {
        return boardCleared;
    }

    //will occasionally return null;
    public Point [] getHint () {
        if (hint == null || getTile (hint [0]) == -1 || !canMatch (hint [0], new Path (), hint [0])) hasMatches (); //will set hint for me (or it will just stay null if there are no matches left)
        return hint;
    }

    public int getTile (Point tile) {
        return board [tile.y] [tile.x];
    }

    //should i make this return a boolean?
    public Path removeMatch (Point tile1, Point tile2) { //should this return a boolean?
        Path path = match (tile1, tile2);
        if (null == path) return null;
        board [tile1.y] [tile1.x] = -1;
        board [tile2.y] [tile2.x] = -1;
        pairsLeft--;
        if (pairsLeft == 0) boardCleared = true;
        alignTiles ();
        return path;
    }

    public void shuffle () {
        for (int i = 0; i < 10; i++) switchRandom ();
        while (!hasMatches ()) switchRandom ();
    }

    private void switchRandom () {
        switchTiles (randomTile (), randomTile ());
    }

    //returns random non-empty tile
    private Point randomTile () {
        int random = random (0, height * width - 1);
        Point randomPoint = new Point (random % width, random / width);

        while (getTile (randomPoint) == -1) {
            random = random (0, height * width - 1);
            randomPoint = new Point (random % width, random / width);
        }

        return randomPoint;
    }

    private static int random (int min, int max) {
        return (int) (Math.random () * (max - min + 1)) + min;
    }

    private void switchTiles (Point tile1, Point tile2) {
        int temp = getTile (tile1);
        board [tile1.y] [tile1.x] = getTile (tile2);
        board [tile2.y] [tile2.x] = temp;
    }

    public boolean hasMatches () {
        if (hint != null && getTile (hint [0]) != -1 && canMatch (hint [0], new Path (), hint [0])) return true;
        else hint = null;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Point curPos = new Point (j, i);
                if (getTile (curPos) == -1) continue;
                if (canMatch (curPos, new Path (), curPos)) return true;
            }
        }

        return false;
    }

    //do NOT pass in empty tile or it will try to match another empty tile
    private boolean canMatch (Point tile, Path history, Point curPos) {
        int tileType = getTile (tile);
        if (history.size () > 3) return false;
        if (curPos.equals (tile) && history.size () > 0) return false; //loop back to same tile
        if (validPos (curPos) && getTile (curPos) == tileType && !curPos.equals (tile)) {
            hint = new Point [] {curPos, tile};
            return true;
        }

        for (Step.Direction direction : Step.Direction.values ()) {
            Point newPos = newPos (curPos, direction);
            if (invalidPosForPath (newPos) && (!validPos (newPos) || getTile (newPos) != tileType)) continue;
            if (canMatch (tile, history.addStep (direction), newPos)) return true;
        }

        return false;
    }

    //if combining board anf logic classes, careful about calling the removeMatch function straight away
    //will return null if tiles cannot be matched, will otherwise return a list of steps to get from one tile to the other
    private Path match (Point tile1, Point tile2) {
        if (getTile (tile1) == -1 || tile1.equals (tile2) || getTile (tile1) != getTile (tile2)) return null;
        return match (tile1, tile2, new Path ());
    }

    private Path match (Point curPos, Point destinationPos, Path history) {
        //keep this order!
        if (history.size () > 3) return null;
        if (curPos.equals (destinationPos)) return history;
        if (invalidPosForPath (curPos) && history.size () != 0) return null;

        Path path = null;

        for (Step.Direction direction: Step.Direction.values()) {
            //preventing searching in the exact opposite direction
            if (curPos.x == destinationPos.x && Math.signum (curPos.y - destinationPos.y) == direction.getY ()) continue;
            if (curPos.y == destinationPos.y && Math.signum (curPos.x - destinationPos.x) == direction.getX ()) continue;

            Path newPath = match (newPos (curPos, direction), destinationPos, history.addStep (direction));

            //want the path that is shortest in terms of lines (most important, must be <= 3) and in terms of length
            if (newPath != null && newPath.size() <= 3 && (null == path || path.size () > newPath.size () || (path.size () == newPath.size () && path.length () > newPath.length ()))) {
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

    private boolean alignHorizontalLine (Point start, int direction) {
        int k = start.x + direction;
        Point point = new Point (k, start.y);
        while (validPos (point) && getTile (point) == -1) {
            k += direction;
            point.translate (direction, 0);
        }
        if (!validPos (point)) return false;
        switchTiles (start, point);
        return true;
    }

    //not the method's responsibility to check that the start and end values are valid
    //will align up to end - 1
    private void alignHorizontal (int start, int end) {
        int direction = (int) Math.signum (end - start);
        for (int i = 0; i < height; i++) {
            for (int j = start; j != end; j += direction) {
                Point point = new Point (j, i);
                if (getTile (point) == -1) alignHorizontalLine (point, direction);
            }
        }
    }

    private void alignUp (boolean half) {
        alignVertical (0, half ? height / 2 : height);
    }

    private void alignDown (boolean half) {
        alignVertical (height - 1, half ? (height - 1) / 2: -1);
    }

    private boolean alignVerticalLine (Point start, int direction) {
        int k = start.y + direction;
        Point point = new Point (start.x, k);
        while (validPos (point) && getTile (point) == -1) {
            k += direction;
            point.translate (0, direction);
        }
        if (!validPos (point)) return false;
        switchTiles (start, point);
        return true;
    }

    //not the method's responsibility to check that the start and end values are valid
    private void alignVertical (int start, int end) {
        int direction = (int) Math.signum (end - start);

        for (int i = 0; i < width; i++) {
            for (int j = start; j != end; j += direction) {
                Point point = new Point (i, j);
                if (getTile (point) == -1) alignVerticalLine (point, direction);
            }
        }
    }

    private void printBoard () {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < 14; j++) {
                System.out.print (board [i] [j] + (board [i] [j] < 10  && board [i] [j] == -1 ? "  " : " "));
            }
            System.out.println ();
        }
    }

    private boolean alignLine (boolean vertical, Point start, int direction) {
        if (vertical) return alignVerticalLine (start, direction);
        return alignHorizontalLine (start, direction);
    }

    private void alignMiddle () {
        Point hole = findHole ();
        boolean madeNoChanges = false, vertical = true;
        System.out.println ("Hole is null: " + (hole == null));

        while (hole != null) {
            if (madeNoChanges) vertical = !vertical; //try aligning vertical if horizontal didn't work last time, and vice versa
            else {
                if (Math.abs ((float) (height - 1) / 2 - hole.y) / height < Math.abs ((float) (width - 1) / 2 - hole.x) / width) vertical = true;
                else vertical = false;
            }

            int direction;
            if ((vertical && hole.y < height / 2) || (!vertical && hole.x < width / 2)) direction = -1;
            else direction = 1;
            madeNoChanges = !alignLine (vertical, hole, direction);

            printBoard ();
            hole = findHole ();
            System.out.println ("Hole is null: " + (hole == null));
        }
    }

    private Point findHole () {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (getTile (new Point (j, i)) == -1) {
                    int vDir = i < height / 2 ? -1 : 1, hDir = j < width / 2 ? -1 : 1;
                    for (Point point = new Point (j, i + vDir); validPos (point); point.translate (0, vDir)) if (getTile (point) != -1) return new Point (j, i);
                    for (Point point = new Point (j + hDir, i); validPos (point); point.translate (hDir, 0)) if (getTile (point) != -1) return new Point (j, i);
                }
            }
        }

        return null;
    }
}
