import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Point;

//bugs b/c split startLevel () into start and setup

public class LogicTest {
    Logic logic;
    int height = 10, width = 14;

    void getToLevel (int level) {
        while (level > logic.getLevel ()) {
            incrementLevel ();
        }
    }

    void incrementLevel () {
        if (logic.getState () == Logic.GameState.PAUSED) logic.resume ();
        else if (logic.getState () == Logic.GameState.LOST_LEVEL || logic.getState () == Logic.GameState.WON_LEVEL) logic.startLevel ();
        else if (logic.getState() != Logic.GameState.PLAYING) throw new Error ("Game level cannot be incremented b/c game has been won/lost");

        while (logic.getState () == Logic.GameState.PLAYING) {
            if (!logic.hasMatches ()) logic.shuffle ();
            logic.removeHint ();
            logic.updateState();
        }
    }

    int [] [] fillBoard () {
        int [] [] board = new int [height] [width];
        logic.pause ();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                board [i] [j] = logic.getTile (new Point (j, i));
            }
        }
        logic.resume ();
        return board;
    }

    @BeforeEach 
    void setUp () {
        logic = new Logic (40);
        logic.startLevel ();
    }

    void assertBoardEquals (int [] [] board1, int [] [] board2) {
        for (int i = 0; i < height; i++) {
            assertArrayEquals (board1 [i], board2 [i]);
        }
    }

    @Test
    void testAlignTiles() {
        //level 1
        int [] [] board1 = fillBoard ();
        Point [] hint = logic.getHint ();
        
        for (int i = 0; i < 20; i++) {
            logic.removeHint ();
            board1 [hint [0].y] [hint [0].x] = -1;
            board1 [hint [1].y] [hint [1].x] = -1;
            hint = logic.getHint ();
        }

        int [] [] board2 = fillBoard ();
        logic.pause ();
        assertBoardEquals (board1, board2);

        //level 3
        getToLevel (3);

        for (int i = 0; i < 20; i++) logic.removeHint ();
        board1 = fillBoard ();
        logic.pause ();
        for (int j = 0; j < 14; j++) {
            int i = 0;
            while (board1 [i] [j] != -1 && i < 5) i++;
            for (; i < 5; i++) assert (board1 [i] [j] == -1);
            i = 9;
            while (board1 [i] [j] != -1 && i > 4) i--;
            for (; i > 4; i--) assert (board1 [i] [j] == -1);
        }

        //level 4
        incrementLevel ();
        for (int i = 0; i < 20; i++) logic.removeHint ();
        board1 = fillBoard ();
        logic.pause ();
        for (int i = 0; i < 10; i++) {
            int j = 0;
            while (board1 [i] [j] != -1 && j < 7) j++;
            for (; j < 7; j++) assert (board1 [i] [j] == -1);
            j = 13;
            while (board1 [i] [j] != -1 && j > 6) j--;
            for (; j > 6; j--) assert (board1 [i] [j] == -1);
        }

        //level 5
        incrementLevel ();
        for (int i = 0; i < 20; i++) logic.removeHint ();
        board1 = fillBoard ();
        logic.pause ();
        for (int i = 0; i < 10; i++) {
            int j = 0;
            while (board1 [i] [j] != -1 && j < 14) j++;
            for (; j < 14; j++) assert (board1 [i] [j] == -1);
        }

        //level 6
        for (int i = 0; i < 20; i++) logic.removeHint ();
        board1 = fillBoard ();
        logic.pause();
        for (int j = 0; j < 14; j++) {
            int i = 9;
            while (board1 [i] [j] != -1 && i >= 0) i--;
            for (; i >= 0; i--) assert (board1 [i] [j] == -1);
        }

        //level 7
        for (int i = 0; i < 20; i++) logic.removeHint ();
        board1 = fillBoard ();
        logic.pause();
        for (int j = 0; j < 14; j++) {
            int i = 0;
            while (board1 [i] [j] != -1 && i < 10) i++;
            for (; i < 10; i++) assert (board1 [i] [j] == -1);
        }

        //level 8
        incrementLevel ();
        for (int i = 0; i < 20; i++) logic.removeHint ();
        board1 = fillBoard ();
        logic.pause();
        for (int i = 0; i < 10; i++) {
            int j = 13;
            while (board1 [i] [j] != -1 && j >= 0) j--;
            for (; j >= 0; j--) assert (board1 [i] [j] == -1);
        }

        //level 9
        incrementLevel ();
        for (int i = 0; i < 20; i++) logic.removeHint ();
        logic.pause();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 14; j++) {
                Point tile = new Point (j, i);
                assert (!hole (tile));
                System.out.print (pad (logic.getTile (tile)));
            }
            System.out.println ();
        }
    }

    String pad (int num) {
        return num + ((num < 10) ? "  " : " ");
    }

    boolean hole (Point point) {
        if (logic.getTile (point) != -1) return false;

        int count = 0;

        for (Step.Direction direction: Step.Direction.values ()) {
            Point step = new Point (point.x + direction.getX (), point.y + direction.getY ());
            if (invalidPos (step) || logic.getTile (step) == -1) count++;
            if (count >= 3) return true;
        }

        return false;
    }

    boolean invalidPos (Point point) {
        return point.x >= 0 && point.x < width && point.y >= 0 && point.y < height;
    }

    @Test
    void testGetState() {
    }

    @Test
    void testGetTime() {

    }

    //test whether time pauses when a game is between levels
    @Test
    void testHasMatches() {

    }

    @Test
    void testMatch() {

    }

    @Test
    void testPauseAndResume() {
        long time = logic.getTime ();
        logic.pause();
        try {
            Thread.sleep (1000);
        } catch (InterruptedException e) {
            e.printStackTrace ();
        }
        logic.resume ();
        assert (logic.getTime() - time < 100);
    }

    @Test
    void testRemoveTile() {
        Point [] hint = logic.getHint();
        logic.removeMatch (hint [0], hint [1]);
        assert (logic.getTile (hint [0]) == -1);
        assert (logic.getTile (hint [1]) == -1);
    }

    //make sure the tiles are not moved around on the board
    @Test
    void testShuffle() {
        int [] [] board1 = fillBoard ();
        logic.shuffle ();
        int [] [] board2 = fillBoard ();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (board1 [i] [j] != board2 [i] [j]) return;
            }
        }

        fail ("Old board matches new board");
    }
}
