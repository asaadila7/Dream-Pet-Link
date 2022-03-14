import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

//multiple levels, quit, pause, sound, hint, instructions

public class Game extends Container {
    private static final ImageIcon instructions = new ImageIcon (Game.class.getResource ("Resources/instructions.png"));
    private static final ImageIcon close = new ImageIcon (Game.class.getResource ("Resources/close.png"));
    private static final ImageIcon pause = new ImageIcon (Game.class.getResource ("Resources/pause.png"));
    private static final ImageIcon resume = new ImageIcon (Game.class.getResource ("Resources/resume.png"));
    private static final ImageIcon hint = new ImageIcon (Game.class.getResource ("Resources/hint.png"));
    private static final ImageIcon soundOn = new ImageIcon (Game.class.getResource ("Resources/soundOn.png"));
    private static final ImageIcon soundOff = new ImageIcon (Game.class.getResource ("Resources/soundOff.png"));

    private final CardLayout cardLayout = new CardLayout ();
    private final JPanel screen = new JPanel (cardLayout);
    private final Background shuffleScreen = new Background ("Resources/background.jpg");
    private final Background pauseScreen = new Background ("Resources/background.jpg");
    private final JScrollPane instructionsScreen = new Instructions ();
    private final Background levelScreen = new Background ("Resources/background.jpg");
    private static final String instructionsString = "Instructions";
    private static final String pauseString = "Pause";
    private static final String shuffleString = "Shuffle";
    private static final String playString = "Play";
    private static final String levelString = "Lost";

    private final JProgressBar timeBar;
    private Timer timer;

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
    private Sound sound;

    private boolean hasQuit;
    private boolean volumeOn;
    private int hintsLeft;
    private int level;
    private int [] score;

    public Game () {
        hasQuit = false;
        hintsLeft = 6;
        level = 1;
        score = new int [9];
        levelLabel = new JLabel (Integer.toString (level));
        playScreen = new Board (level);
        timeBar = new JProgressBar (0, (int) Timer.MAX_TIME);
        timer = new Timer ();
        hintButton.setText (Integer.toString (hintsLeft));

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
        pauseScreen.add (quitButton);

        screen.add (instructionsString, instructionsScreen);
        screen.add (playString, playScreen);
        screen.add (pauseString, pauseScreen);
        screen.add (shuffleString, shuffleScreen);
        screen.add (levelString, levelScreen);
        add (screen);
        cardLayout.show (screen, playString);

        hintButton.addActionListener (
            new ActionListener () {
                @Override
                public void actionPerformed (ActionEvent event) {
                    if (hintsLeft > 0) {
                        playScreen.showHint ();
                        hintsLeft--;
                        hintButton.setText (Integer.toString (hintsLeft));
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
                        if (!pauseButton.isSelected ()) pause ();
                        setButtonsEnabled (false, false, false, true);
                        cardLayout.show (screen, instructionsString);
                    } else {
                        cardLayout.show (screen, playString);
                        setButtonsEnabled (true, true, true, true);
                        if (!pauseButton.isSelected ()) resume ();
                    }
                }
            }
        );

        final Runnable runnable = new Runnable() {
            public void run () {
                while (!hasQuit) runGame ();
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
        update ();

        timer.start ();
    }

    public void runGame () {
        timeBar.setValue (timer.getTimeLeft ());
        if (timer.getTimeLeft () <= 0) {
            if (level == 1) newLevel (false, true);
            else newLevel (false, false);
        } else if (playScreen.boardCleared ()) {
            if (level == 9) newLevel (true, true);
            else newLevel (true, false);
        } else if (playScreen.needsShuffling ()) {
            timer.pause ();
            cardLayout.show (screen, shuffleString);
            update (); //do i need this?
            try {
                Thread.sleep (1000);
            } catch (Exception e) {
                e.printStackTrace ();
            }
            cardLayout.show (screen, playString);
            timer.resume ();
            update (); //do i need this?
        }

    }

    private void newLevel (boolean won, boolean finished) {
        String labelText = "You " + (won ? "won" : "lost") + " the " + (finished ? "game" : "level");

        if (won) {
            timer.pause ();
            score [level - 1] = timer.getTimeLeft ();

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
            }
        }

        stopSound ();
        setButtonsEnabled (false, false, false, false);
        newLevelLabel.setText (labelText);
        cardLayout.show (screen, levelString);
    }

    public void startGame (boolean volumeOn) {
        this.volumeOn = volumeOn;
        startLevel ();
    }

    private void startLevel () {
        setButtonsEnabled (true, true, true, true);
        if (volumeOn) playSound ();
        playScreen.updateBoard ();
        cardLayout.show (screen, playString);
        update ();
        timer = new Timer ();
        timer.start ();
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

    private void toggleSoundOn () {
        volumeOn = true;
        playSound ();
    }

    private void toggleSoundOff () {
        volumeOn = false;
        stopSound ();
    }

    public void pause () {
        timer.pause ();
        stopSound ();
    }

    public void resume () {
        if (volumeOn) playSound();
        timer.resume ();
    }

    private void playSound () {
        sound = new Sound ();
        sound.play ();
    }

    private void stopSound () {
        if (sound != null) sound.shouldQuit = true;
    }

    private void update () {
        revalidate ();
        repaint ();
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

    public class Timer {
        public static final int MAX_TIME = 600000;
        private long startTime;
        private long pauseTime;
        private boolean paused, started;

        public Timer () {
            paused = false;
            started = false;
        }

        public void start () {
            started = true;
            startTime = System.currentTimeMillis ();
        }

        public void pause () {
            if (!paused) {
                pauseTime = System.currentTimeMillis ();
                paused = true;
            }
        }

        public int getTimeLeft () { //will throw error if startTime not initialized
            if (!started) return (int) MAX_TIME;
            if (paused) return (int) (MAX_TIME - pauseTime + startTime);
            else return (int) (MAX_TIME - System.currentTimeMillis() + startTime);
        }

        public void resume () {
            if (paused) {
                startTime += System.currentTimeMillis () - pauseTime;
                paused = false;
            }
        }
    }
}
