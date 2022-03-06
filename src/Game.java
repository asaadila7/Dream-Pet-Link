import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

//dialogs for won, lost, lost game, won game
//hints: need button

public class Game extends Container {
    private final int HEIGHT = 10, WIDTH = 14;
    private Logic logic;
    private Sound sound;
    private Background pauseScreen, shuffleScreen;
    private Board playScreen;
    private Point lastClick;
    private JPanel buttonPane;
    private JProgressBar timeBar;
    private JButton pauseButton;
    private JToggleButton soundButton;
    private ImageIcon soundOn = new ImageIcon ("./soundOn.png");
    private ImageIcon soundOff = new ImageIcon ("./soundOff.png");

    public Game (JFrame owner) {
        logic = new Logic (Tile.TYPES, HEIGHT, WIDTH);
        logic.setUpLevel ();

        playScreen = new Board (this, HEIGHT, WIDTH);
        pauseScreen = new Background ("./pauseScreen.png");
        shuffleScreen = new Background ("./shuffleScreen.png");

        timeBar = new JProgressBar (0, (int) Logic.MAX_TIME);

        pauseButton = new JButton (new ImageIcon ("./pause.png"));
        soundButton = new JToggleButton (soundOn);
        soundButton.setSelectedIcon (new ImageIcon ("./soundOff.png"));
        buttonPane.add (pauseButton);
        buttonPane.add (soundButton);

        pauseButton.addActionListener (
            new ActionListener () {
                @Override
                public void actionPerformed (ActionEvent event) {
                    pause ();
                    new Pause (owner);
                }
            }
        );

        soundButton.addItemListener (
            new ItemListener () {
                @Override
                public void itemStateChanged(ItemEvent eve) {
                    if (soundButton.isSelected()) {
                        soundButton.setIcon (soundOn);
                        playSound ();
                    } else {
                        soundButton.setIcon (soundOff);
                        stopSound ();
                    }
                }
            }
        );

        setLayout (new FlowLayout ());
        add (buttonPane);
        add (timeBar);
        add (pauseScreen);
    }

    public void startLevel (boolean soundOn) {
        switchPanel (playScreen);
        if (soundOn) playSound ();
        logic.startLevel ();
    }

    public void pause () {
        logic.pause ();
        stopSound ();
        switchPanel (pauseScreen);
    }

    public void resume (boolean soundOn) {
        switchPanel (playScreen);
        if (soundOn) playSound();
        logic.resume ();
    }

    private void playSound () {
        sound = new Sound ();
        sound.play ();
    }

    private void stopSound () {
        sound.shouldQuit = true;
    }

    public void update () {
        timeBar.setValue ((int) logic.getTimeLeft());
        revalidate ();
        repaint ();
    }

    public void clickTile (int x, int y) {
        if (lastClick == null) {
            lastClick = new Point (x, y);
            return;
        }

        if (logic.removeMatch (lastClick, new Point (x, y))) {
            lastClick = null;
            if (!logic.hasMatches ()) {
                logic.pause ();
                switchPanel (shuffleScreen);
                try { //potential problem: screen will be unresponsive for a whole minute********************
                    Thread.sleep (1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                switchPanel (playScreen);
                logic.resume ();
                logic.shuffle ();
            }

            playScreen.update ();
        } else {
            lastClick = new Point (x, y);
        }

        update ();
    }

    private void switchPanel (JPanel panel) { //will this make a copy of the panel or pass the existing one in?
        remove (2);
        add (panel);
        update ();
    }

    class Tile extends JRadioButton {
        static final int TYPES = 40;
        private static final String IMAGES [] = {"Black Cracks", "Blue Leaves", "Blue Swirl Painting", "Bricks", "Cabbage", "Cracked Ice", "Cracked Wall", "Dewdrops on Orange Flower", "Dewdrops on Purple Leaf", "Ferns", "Fire", "Golden Maple Leaves", "Green Cut Glass", "Grey Abstract", "Leaves on a Tree", "Lemon Bubbles", "Lemon Wedge", "Maple Leaves", "Mossy Rock Face", "Night Sky", "Orange Maple Leaves", "Orange Sunset", "Orange Swirl Painting", "Pink and Purple Smoke", "Pink Clouds", "Pink Flowers", "Purple Feathers", "Purple Flowers", "Purple Oil Painting", "Red Abstract Painting", "Red Cut Glass", "Red Leaf", "Rock Wall", "Sea Foam", "Smoke", "Sunset with Trees", "Tree Bark", "Virus", "White Silk", "White Stones"};
        private int type;
        
        //will assume type is within 0 and 39 and filetypes are jpg
        public Tile (int type) {
            super (new ImageIcon ("./Tiles/" + IMAGES [type] + ".jpg"));
            this.type = type;
        }

        public void setType (int type) {
            this.type = type;
            super.setIcon (getIconForType());
        }

        private ImageIcon getIconForType () {
            return new ImageIcon ("./Tiles/" + IMAGES [type] + ".jpg");
        }

        public int getType () {
            return type;
        }
    }

    class Board extends JPanel {
        class Handler implements ActionListener { //should I use an item listener instead?**************************
            @Override
            public void actionPerformed (ActionEvent event) {
                String actionCommand = event.getActionCommand ();
                int x = Integer.parseInt (actionCommand.substring (0, actionCommand.indexOf (' ')));
                int y = Integer.parseInt (actionCommand.substring (actionCommand.indexOf (',') + 1));
                game.clickTile (x, y);
            }
        }

        private Tile [] [] tiles;
        private Game game;

        public Board (Game game, int height, int width) {
            this.game = game;
            setLayout (new GridLayout (height, width, 2, 2));

            tiles = new Tile [HEIGHT] [WIDTH];
            Handler handler = new Handler ();
            ButtonGroup group = new ButtonGroup ();
            for (int i = 0; i < HEIGHT; i++) {
                for (int j = 0; j < 14; j++) {
                    tiles [i] [j] = new Tile (logic.getTile (new Point (j, i)));
                    tiles [i] [j].setActionCommand (j + ", " + i);
                    tiles [i] [j].addActionListener (handler);
                    group.add (tiles [i] [j]);
                    add (tiles [i] [j]);
                }
            }
        }

        public void update () {
            for (int i = 0; i < HEIGHT; i++) {
                for (int j = 0; j < WIDTH; j++) {
                    int type = logic.getTile (new Point (j, i));
                    if (tiles [i] [j].getType () != type) {
                        tiles [i] [j] .setType (type);
                    }
                }
            }
        }
    }

    public class Background extends JPanel {
        private Image background;
    
        public Background (String imageFile) {
            super ();
            setDoubleBuffered (true);
    
            try {
                background = ImageIO.read (new File (imageFile));
            } catch (IOException ignored) {
                System.out.println ("File not found");
            }
        }

        @Override
        protected void paintComponent (Graphics g)  {
            super.paintComponent (g);
            g.drawImage (background, 0, 0, getWidth (), getHeight (), background.getWidth (this) - getWidth (), background.getHeight (this) - getHeight (), background.getWidth (this), background.getHeight (this), this);
        }
    }
}
