import javax.swing.*;
import java.io.File;
import java.awt.*;
import java.util.ArrayList;

class Game {
    //40 animals, 14 x 10 board
    private int level;
    private Tile board [] [];
    private GameState state;

    public Game () {
        level = 1;
        board = new Tile [10] [14];
        state = GameState.PLAYING;

        //initialize board to have an equal amount of each tile (as much as possible)
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 14; j += 2) {
                board [i] [j] = new Tile ((i * 14 + j) / 2 % 40);
                board [i] [j + 1] = new Tile ((i * 14 + j) / 2 % 40);
            }
        }

        //shuffle tiles
        for (int i = 0; i < 40; i++) {
            Point tile1 = new Point ((int) (Math.random () * 14), (int) (Math.random () * 10));
            Point tile2 = new Point ((int) (Math.random () * 14), (int) (Math.random () * 10));
            Tile temp = board [tile1.y] [tile1.x];
            board [tile1.y] [tile1.x] = board [tile2.y] [tile2.x];
            board [tile2.y] [tile2.x] = temp;
        }
    }

    static class Tile {
        static String images [] = {"Black Cracks.jpg", "Blue Leaves.jpg", "Blue Swirl Painting.jpg", "Bricks.jpg", "Cabbage.jpg", "Cracked Ice.jpg", "Cracked Wall.jpg", "Dewdrops on Purple Leaf.jpg", "Dewsrops on Orange Flower.jpg", "Ferns.jpg", "Fire.jpg", "Golden Maple Leaves.jpg", "Green Cut Glass.png", "Grey Abstract.jpg", "Leaves on a Tree.jpg", "Lemon Bubbles.jpg", "Lemon Wedge.jpg", "Maple Leaves.jpg", "Mossy Rock Face.jpg", "Night Sky.jpg", "Orange Maple Leaves.jpg", "Orange Sunset.jpg", "Orange Swirl Painting.jpg", "Pink and Purple Smoke.jpg", "Pink Clouds.jpg", "Pink Flowers.jpg", "Purple Feathers.jpg", "Purple Flowers.jpg", "Purple Oil Painting.jpg", "Red Abstract Painting.jpg", "Red Cut Glass.png", "Red Leaf.jpg", "Rock Wall.jpg", "Sea Foam.jpg", "Smoke.jpg", "Sunset with Trees.jpg", "Tree Bark.jpg", "Virus.jpg", "White Silk.jpg", "White Stones.jpg"};

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
        PAUSED,
        WON_LEVEL,
        LOST_LEVEL,
        WON_GAME,
        OVER
    }

    public int getLevel () {
        return level;
    }

    public int getTile (Point tile) {
        return board [tile.y] [tile.x].type;
    }

    public GameState getState () {
        return state;
    }

    public ArrayList <Step> match (Point tile1, Point tile2) {
        
    }

    static class Step {
        static class Direction {
            private int x, y;

            public Direction (int x, int y) {
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

        final static Direction UP = new Direction (0, -1);
        final static Direction DOWN = new Direction (0, 1);
        final static Direction RIGHT = new Direction (1, 0);
        final static Direction LEFT = new Direction (-1, 0);

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