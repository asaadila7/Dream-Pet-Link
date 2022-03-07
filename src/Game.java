import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Game extends Container {
    //make some of these local if possible
    private final static int HEIGHT = 10, WIDTH = 14;
    private Logic logic;
    private Sound sound;
    private JPanel pauseScreen, shuffleScreen;
    private JScrollPane instructionsScreen;
    private Board playScreen;
    private JProgressBar timeBar;

    private boolean hasQuit;
    private boolean volumeOn;
    private int hintsLeft;
    private int [] score;
    private QuitHandler quitHandler;
    private StartLevelHandler startLevelHandler;
    private JLabel levelLabel;

    private JPanel buttonPane;
    private AbstractButton soundButton, pauseButton, instructionsButton, hintButton;
    private ImageIcon soundOn, soundOff, pause, resume, hint, instructions, closeInstructions;

    public Game () {
        levelLabel = new JLabel (Integer.toString (logic.getLevel ()));

        quitHandler = new QuitHandler ();
        startLevelHandler = new StartLevelHandler ();

        hintsLeft = 6;
        score = new int [9];

        logic = new Logic (Tile.TYPES, HEIGHT, WIDTH);
        logic.setUpLevel ();

        playScreen = new Board (this, HEIGHT, WIDTH);
        shuffleScreen = new Background ("./shuffleScreen.png");
        instructionsScreen = new Instructions ();
        pauseScreen = new Background ("./pauseScreen.png");

        JButton quitButton = new JButton ("QUIT");
        quitButton.addActionListener (quitHandler);
        pauseScreen.add (quitButton);

        timeBar = new JProgressBar (0, (int) Logic.MAX_TIME);

        soundOn = new ImageIcon ("./soundOn.png");
        soundOff = new ImageIcon ("./soundOff.png");
        pause = new ImageIcon ("./pause.png");
        resume = new ImageIcon ("./resume.png");
        hint = new ImageIcon ("./hint.png");
        instructions = new ImageIcon ("./instructions.png");
        closeInstructions = new ImageIcon ("./close.png");

        pauseButton = new JToggleButton (pause);
        pauseButton.setSelectedIcon (resume);
        soundButton = new JToggleButton (soundOn);
        soundButton.setSelectedIcon (soundOff);
        hintButton = new JButton (Integer.toString (hintsLeft), hint);
        instructionsButton = new JToggleButton (instructions);
        instructionsButton.setSelectedIcon (closeInstructions);

        buttonPane.add (pauseButton);
        buttonPane.add (soundButton);
        buttonPane.add (hintButton);
        buttonPane.add (instructionsButton);

        hintButton.addActionListener (
            new ActionListener () {
                @Override
                public void actionPerformed (ActionEvent event) {
                    if (hintsLeft > 0) {
                        Point [] hint = logic.getHint ();
                        playScreen.setHintBorder (hint [0], hint [1]);
                        hintsLeft--;
                        hintButton.setText (Integer.toString (hintsLeft));
                    }
                }
            }
        );

        pauseButton.addItemListener (
            new ItemListener () {
                @Override
                public void itemStateChanged (ItemEvent eve) {
                    if (pauseButton.isSelected()) pause ();
                    else resume ();
                }
            }
        );

        soundButton.addItemListener (
            new ItemListener () {
                @Override
                public void itemStateChanged(ItemEvent event) {
                    if (soundButton.isSelected()) toggleSoundOn();
                    else toggleSoundOff ();
                }
            }
        );

        instructionsButton.addItemListener (
            new ItemListener () {
                @Override
                public void itemStateChanged (ItemEvent event) {
                    if (instructionsButton.isSelected ()) {
                        if (logic.getState () != Logic.GameState.PAUSED) {
                            logic.pause ();
                            stopSound ();
                        }
                        setButtonsEnabled (false);
                        switchComponent (instructionsScreen);
                    } else {
                        switchComponent (playScreen);
                        setButtonsEnabled (true);
                        if (logic.getState () != Logic.GameState.PAUSED) {
                            playSound ();
                            logic.resume ();
                        }
                    }
                }
            }
        );

        final Runnable runnable = new Runnable() {
            public void run () {
                while (!hasQuit) {
                    if (logic.getState () == Logic.GameState.LOST_LEVEL || logic.getState () == Logic.GameState.OVER) {
                        JPanel panel = new JPanel ();
                        if (logic.getState () == Logic.GameState.OVER) {
                            JLabel label = new JLabel ("You lost the game");
                            JButton button = new JButton ("Return to menu");
                            button.addActionListener (quitHandler);
                            panel.add (label);
                            panel.add (button);
                        }  else {
                            score [logic.getLevel ()] = 0;
                            JLabel label = new JLabel ("You lost the level");
                            JButton button = new JButton ("Play from level " + logic.getLevel ());
                            button.addActionListener (startLevelHandler);
                            panel.add (label);
                            panel.add (button);
                        }
                        stopSound ();
                        setButtonsEnabledBetweenLevels (false);
                        switchComponent (panel);
                    }
                }
            }
        };
        new Thread (runnable).start ();

        setLayout (new FlowLayout ());
        add (levelLabel);
        add (buttonPane);
        add (timeBar);
        add (playScreen);
        update ();
    }

    private void setButtonsEnabledBetweenLevels (boolean enabled) {
        setButtonsEnabled (enabled);
        instructionsButton.setEnabled (enabled);
    }

    private void setButtonsEnabled (boolean enabled) {
        setButtonsEnabledForPause (enabled);
        pauseButton.setEnabled (enabled);
    }

    private void setButtonsEnabledForPause (boolean enabled) {
        soundButton.setEnabled (enabled);
        hintButton.setEnabled (enabled);
    }

    public void startGame (boolean volumeOn) {
        this.volumeOn = volumeOn;
        if (volumeOn) playSound ();
        logic.startLevel();
    }

    public boolean gameOver () {
        return hasQuit;
    }

    private void toggleSoundOn () {
        volumeOn = true;
        playSound ();
    }

    private void toggleSoundOff () {
        volumeOn = false;
        stopSound ();
    }

    private void startLevel () {
        setButtonsEnabledBetweenLevels (true);
        if (volumeOn) {
            playSound ();
            volumeOn = true;
        }
        logic.setUpLevel ();
        playScreen.updateBoard ();
        switchComponent (playScreen);
        update ();
        logic.startLevel ();
    }

    private JPanel wonGame () {
        int totalScore = 0;
        for (int s : score) totalScore += s;
        JPanel panel = new JPanel ();
        JLabel label = new JLabel ("You Won! Your score is " + totalScore);
        JButton button = new JButton ("Play next level");
        button.addActionListener (startLevelHandler);
        panel.add (label);
        panel.add (button);
        return panel;
    }

    private JPanel wonLevel () {
        JPanel panel = new JPanel ();
        JLabel label = new JLabel ("+" + Integer.toString ( logic.getTimeLeft ()));
        JButton button = new JButton ("Return to menu");
        button.addActionListener (quitHandler);
        panel.add (label);
        panel.add (button);
        return panel;
    }

    public void pause () {
        logic.pause ();
        stopSound ();
        setButtonsEnabledForPause (false);
        switchComponent (pauseScreen);
    }

    public void resume () {
        switchComponent (playScreen);
        setButtonsEnabledForPause (true);
        if (volumeOn) playSound();
        logic.resume ();
    }

    private void playSound () {
        sound = new Sound ();
        sound.play ();
    }

    private void stopSound () {
        sound.shouldQuit = true;
    }

    private void update () {
        timeBar.setValue (logic.getTimeLeft());
        revalidate ();
        repaint ();
    }

    private void clickTile (int x, int y) {
        Point lastClicked = playScreen.getLastClicked ();
        Point thisClick = new Point (x, y);

        if (lastClicked == null || !logic.removeMatch (lastClicked, thisClick)) {
            playScreen.selectNonMatch (thisClick);
        } else {
            playScreen.selectMatch (thisClick);
            if (logic.getState () == Logic.GameState.WON_LEVEL || logic.getState () == Logic.GameState.WON_GAME) {
                score [logic.getLevel () - 2] = logic.getTimeLeft ();
                stopSound ();
                setButtonsEnabledBetweenLevels (false);
                if (logic.getState () == Logic.GameState.WON_GAME) switchComponent (wonGame());
                else {
                    hintsLeft += 3;
                    switchComponent (wonLevel ());
                }
            } else if (!logic.hasMatches ()) {
                logic.pause ();
                switchComponent (shuffleScreen);
                try { //potential problem: screen will be unresponsive for a whole minute********************
                    Thread.sleep (1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                switchComponent (playScreen);
                logic.resume ();
                logic.shuffle ();
            }

            playScreen.updateBoard ();
        }

        update ();
    }

    private void switchComponent (JComponent component) { //will this make a copy of the component or pass the existing one in?
        remove (2);
        add (component);
        update ();
    }

    public boolean hasSound () {
        return volumeOn;
    }

    class StartLevelHandler implements ActionListener {
        @Override
        public void actionPerformed (ActionEvent event) {
            startLevel ();
        }
    }

    class QuitHandler implements ActionListener {
        @Override
        public void actionPerformed (ActionEvent event) {
            hasQuit = true;
        }
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
        private Border hintBorder, selectBorder;
        private Point hint [];
        private Point lastClicked;
        private static final int tileSize = 30; //in pixels
        private static final int tileGap = 2;

        public Board (Game game, int height, int width) {
            hintBorder = BorderFactory.createLineBorder(Color.orange);
            selectBorder = BorderFactory.createLineBorder(Color.yellow); //optional int param to specify thickness of border in pixels
            this.game = game;
            setLayout (new GridLayout (height, width, tileGap, tileGap));

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

        public void updateBoard () {
            for (int i = 0; i < HEIGHT; i++) {
                for (int j = 0; j < WIDTH; j++) {
                    int type = logic.getTile (new Point (j, i));
                    if (tiles [i] [j].getType () != type) {
                        tiles [i] [j] .setType (type);
                    }
                }
            }
        }

        public void setHintBorder (Point tile1, Point tile2) {
            clearHintBorder ();
            tiles [tile1.y] [tile1.x].setBorder (hintBorder);
            tiles [tile2.y] [tile2.x].setBorder (hintBorder);
            hint = new Point [] {tile1, tile2};
        }

        private void clearHintBorder () {
            if (hint != null) {
                for (int i = 0; i < 2; i++) {
                    tiles [hint [i].y] [hint [i].x].setBorder (null); //may not work: maybe try overriding paintBorder or set and empty border
                }
                hint = null;
            }
        }

        //draw line between the tiles and make them disappear and then make lastClicked = null
        public void selectMatch (Point tile) {
            ArrayList <Step> path = logic.match (lastClicked, tile);
            
            clearHintBorder ();
            tiles [tile.y] [tile.x].setBorder (selectBorder);
        }

        public void selectNonMatch (Point tile) {
            clearHintBorder ();
            if (lastClicked != null) tiles [lastClicked.y] [lastClicked.x].setBorder (null);
            tiles [tile.y] [tile.x].setBorder (selectBorder);
            lastClicked = tile;
        }

        public Point getLastClicked () {
            return lastClicked;
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
