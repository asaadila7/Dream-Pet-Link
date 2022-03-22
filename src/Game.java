import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

//if i click on an empty tile, it should get rid of my last selection for me

public class Game extends Container {
    private static final ImageIcon instructions = new ImageIcon (Game.class.getResource ("Resources/about.png"));
    private static final ImageIcon close = new ImageIcon (Game.class.getResource ("Resources/close.png"));
    private static final ImageIcon pause = new ImageIcon (Game.class.getResource ("Resources/pause.png"));
    private static final ImageIcon resume = new ImageIcon (Game.class.getResource ("Resources/resume.png"));
    private static final ImageIcon hint = new ImageIcon (Game.class.getResource ("Resources/hint.png"));
    private static final ImageIcon soundOn = new ImageIcon (Game.class.getResource ("Resources/soundOn.png"));
    private static final ImageIcon soundOff = new ImageIcon (Game.class.getResource ("Resources/soundOff.png"));

    private final JPanel shuffleScreen = new JPanel ();
    private final Background pauseScreen = new Background ("Resources/pause_background.png");
    private final static About instructionsScreen = new About ();
    private static final String instructionsString = "Instructions";
    private static final String pauseString = "Pause";
    private static final String shuffleString = "Shuffle";
    private static final String playString = "Play";
    private static final String levelString = "Lost";
    private final JPanel levelScreen = new JPanel ();
    private final CardLayout cardLayout = new CardLayout ();
    private final JPanel screen = new JPanel (cardLayout);

    private final JProgressBar timeBar;

    private final QuitHandler quitHandler = new QuitHandler ();
    private final StartLevelHandler startLevelHandler = new StartLevelHandler ();

    private final JButton quitButton = new JButton ("Return to menu");
    private final JButton startButton = new JButton ();
    private final JButton hintButton = new JButton (hint);
    private final JToggleButton instructionsButton = new JToggleButton (instructions);
    private final JToggleButton pauseButton = new JToggleButton (pause);
    private final JToggleButton soundButton = new JToggleButton (soundOn);

    private final JLabel levelLabel;
    private final JLabel newLevelLabel = new JLabel ();

    private Board playScreen;
    private final Sound sound = new Sound ();

    private boolean hasQuit;
    private int hintsLeft;
    private int level;
    private int [] score;
    private boolean inBetweenLevels;

    public Game () {
        inBetweenLevels = false;
        hasQuit = false;
        hintsLeft = 100; //*************************
        level = 1;
        score = new int [9];
        levelLabel = new JLabel ("Level " + level + "/9");
        playScreen = new Board (level);
        timeBar = new JProgressBar (0, (int) Board.Timer.MAX_TIME);
        hintButton.setText (hintsLeft + " hints");

        JLabel shuffleLabel = new JLabel ("No More Matches");
        shuffleLabel.setAlignmentY (JLabel.CENTER_ALIGNMENT); //Not working
        shuffleScreen.add (shuffleLabel);

        JPanel buttonPane = new JPanel ();
        pauseButton.setSelectedIcon (resume);
        soundButton.setSelectedIcon (soundOff);
        instructionsButton.setSelectedIcon (close);
        buttonPane.add (pauseButton);
        buttonPane.add (soundButton);
        buttonPane.add (hintButton);
        buttonPane.add (instructionsButton);

        levelScreen.add (newLevelLabel);
        levelScreen.add (startButton);
        levelScreen.add (quitButton);

        startButton.addActionListener (startLevelHandler);
        quitButton.addActionListener (quitHandler);
        quitButton.setAlignmentY (JButton.CENTER_ALIGNMENT);//not working
        pauseScreen.add (quitButton);

        screen.add (instructionsString, instructionsScreen);
        screen.add (pauseString, pauseScreen);
        screen.add (shuffleString, shuffleScreen);
        screen.add (levelString, levelScreen);
        add (screen);

        hintButton.addActionListener (
            new ActionListener () {
                @Override
                public void actionPerformed (ActionEvent event) {
                    if (hintsLeft > 0) {
                        playScreen.showHint ();
                        hintsLeft--;
                        hintButton.setText (hintsLeft + " hints");
                    } //else?
                }
            }
        );

        pauseButton.addItemListener (
            new ItemListener () {
                @Override
                public void itemStateChanged (ItemEvent event) {
                    if (pauseButton.isSelected()) {
                        pause ();
                        cardLayout.show (screen, pauseString);
                        setButtonsEnabled (true, false, false, true);
                    } else {
                        setButtonsEnabled (true, true, true, true);
                        cardLayout.show (screen, playString);
                        resume ();
                    }
                }
            }
        );

        soundButton.addItemListener (
            new ItemListener () {
                @Override
                public void itemStateChanged(ItemEvent event) {
                    if (soundButton.isSelected()) sound.pause ();
                    else sound.play ();
                }
            }
        );

        instructionsButton.addItemListener (
            new ItemListener () {
                @Override
                public void itemStateChanged (ItemEvent event) {
                    if (instructionsButton.isSelected ()) {
                        if (!pauseButton.isSelected ()) pause ();
                        setButtonsEnabled (false, false, false, true);
                        cardLayout.show (screen, instructionsString);
                    } else {
                        cardLayout.show (screen, pauseString);
                        setButtonsEnabled (true, true, true, true);
                        if (!pauseButton.isSelected ()) {
                            cardLayout.show (screen, playString);
                            resume ();
                        }
                    }
                }
            }
        );

        final Runnable runnable = new Runnable() {
            public void run () {
                while (!hasQuit) runGame ();
                sound.dispose ();
            }
        };
        new Thread (runnable).start ();

        setLayout (new BoxLayout (this, BoxLayout.Y_AXIS));
        JPanel topPane = new JPanel ();
        topPane.setLayout (new BoxLayout (topPane, BoxLayout.LINE_AXIS));
        topPane.add (levelLabel);
        topPane.add (buttonPane);
        topPane.add (timeBar);
        topPane.setBorder (BorderFactory.createEmptyBorder (20, 20, 20, 20));
        add (topPane);
        screen.setBorder (BorderFactory.createEmptyBorder (20, 20, 20, 20));
        add (screen);

        playScreen.timer.start ();
    }

    public void runGame () {
        timeBar.setValue (playScreen.timer.getTimeLeft ());
        if (!inBetweenLevels && playScreen.timer.getTimeLeft () <= 0) {
            inBetweenLevels = true;
            if (level == 1) newLevel (false, true);
            else newLevel (false, false);
        } else if (!inBetweenLevels && playScreen.boardCleared ()) {
            inBetweenLevels = true;
            if (level == 9) newLevel (true, true);
            else newLevel (true, false);
        } else if (playScreen.needsShuffling ()) {
            System.out.println ("Shuffling");
            playScreen.timer.pause ();
            cardLayout.show (screen, shuffleString);
            try {
                Thread.sleep (1000);
            } catch (Exception e) {
                e.printStackTrace ();
            }
            cardLayout.show (screen, playString);
            playScreen.timer.resume ();
        }

    }

    private void newLevel (boolean won, boolean finished) {
        String labelText = "You " + (won ? "won" : "lost") + " the " + (finished ? "game" : "level");

        if (won) {
            playScreen.timer.pause ();
            score [level - 1] = playScreen.timer.getTimeLeft ();

            labelText += ". Your score is ";
            if (!finished) labelText += Integer.toString (score [level - 1]);
            else {
                int totalScore = 0;
                for (int i = 0; i < 9; i++) totalScore += score [i];
                labelText += Integer.toString (totalScore);
            }
        }

        if (finished) {
            level = 1;
            score = new int [9];
            hintsLeft = 6;
            startButton.setText ("Play again");
        } else {
            if (won) {
                level++;
                hintsLeft += 3;
                startButton.setText ("Start Next Level");
            } else {
                startButton.setText ("Play Level Again");
                //do i give them back the hints they lost in the level?
            }
        }

        sound.pause ();
        setButtonsEnabled (false, false, false, false);
        newLevelLabel.setText (labelText);
        cardLayout.show (screen, levelString);
        System.out.println ("New Level Screen");
        System.out.println ("Won: " + won + "\nfinished: " + finished);
    }

    public void startGame (boolean volumeOn) {
        soundButton.setSelected (!volumeOn);
        startLevel ();
    }

    private void startLevel () {
        playScreen = new Board (level);
        screen.add (playString, playScreen);
        inBetweenLevels = false;
        setButtonsEnabled (true, true, true, true);
        levelLabel.setText ("Level " + level + "/9");
        if (!soundButton.isSelected ()) sound.play ();
        cardLayout.show (screen, playString);
        playScreen.timer.start ();
    }

    private void setButtonsEnabled (boolean pause, boolean sound, boolean hint, boolean instructions) {
        pauseButton.setEnabled (pause);
        soundButton.setEnabled (sound);
        hintButton.setEnabled (hint);
        instructionsButton.setEnabled (instructions);
    }

    public boolean gameOver () {
        return hasQuit;
    }

    public void pause () {
        playScreen.timer.pause ();
        sound.pause ();
    }

    public void resume () {
        if (!soundButton.isSelected ()) sound.play ();
        playScreen.timer.resume ();
    }

    public boolean isPaused () {
        return pauseButton.isSelected ();
    }

    public boolean hasSound () {
        return !soundButton.isSelected ();
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

    public class Background extends JPanel {
        private Image background;

        public Background (String imageFile) {
            super ();
            setDoubleBuffered (true);

            try {
                background = ImageIO.read (this.getClass ().getResource (imageFile));
            } catch (IOException ignored) {
                System.out.println ("File not found: " + imageFile);
            }
        }

        @Override
        protected void paintComponent (Graphics g)  {
            super.paintComponent (g);
            g.drawImage (background, 0, 0, getWidth (), getHeight (), background.getWidth (this) - getWidth (), background.getHeight (this) - getHeight (), background.getWidth (this), background.getHeight (this), this);
        }
    }
}
