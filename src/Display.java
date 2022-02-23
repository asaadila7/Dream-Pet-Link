import javax.swing.*;
import java.io.File;


public class Display {
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

        public int getType () {
            return type;
        }

        public ImageIcon getIcon () {
            return this.icon;
        }
    }
}
