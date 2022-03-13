import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

//multiple levels, quit, pause, sound, hint, instructions

public class Game extends Container {
    //make some of these local if possible
    //use cardLayout

    private static final ImageIcon instructions = new ImageIcon ("./resources/instructions.png");
    private static final ImageIcon close = new ImageIcon ("./resources/close.png");
    private static final ImageIcon pause = new ImageIcon ("./resources/pause.png");
    private static final ImageIcon resume = new ImageIcon ("./resources/resume.png");
    private static final ImageIcon hint = new ImageIcon ("./resources/hint.png");
    private static final ImageIcon soundOn = new ImageIcon ("./resources/soundOn.png");
    private static final ImageIcon soundOff = new ImageIcon ("./resources/soundOff.png");

    private final CardLayout cardLayout = new CardLayout ();
    private final JPanel screen = new JPanel (cardLayout);
    private final Background shuffleScreen = new Background ("./Resources/background.jpg");
    private final Background pauseScreen = new Background ("./Resources/background.jpg");
    private final JScrollPane instructionsScreen = new Instructions ();
    private static final String instructionsString = "Instructions";
    private static final String pauseString = "Pause";
    private static final String shuffleString = "Shuffle";
    private static final String playString = "Play";

    private static final long MAX_TIME = 60000;
    private long startTime;

    private final QuitHandler quitHandler = new QuitHandler ();
    private final StartLevelHandler startLevelHandler = new StartLevelHandler ();

    private final JButton quitButton = new JButton ("Quit");
    private final JButton startButton = new JButton ("Start Next Level");
    private final JButton hintButton = new JButton (hint);
    private final JToggleButton instructionsButton = new JToggleButton (instructions);
    private final JToggleButton pauseButton = new JToggleButton (pause);
    private final JToggleButton soundButton = new JToggleButton (soundOn);

    private final JLabel levelLabel;
    private final JProgressBar timeBar;

    private Board playScreen;
    private Sound sound;

    private boolean hasQuit;
    private boolean volumeOn;
    private int hintsLeft;
    private int level;
    private int [] score = new int [9];

    public Game () {
        hintsLeft = 6;
        level = 1;
        levelLabel = new JLabel (Integer.toString (level));
        playScreen = new Board (level);

        JPanel buttonPane = new JPanel ();
        pauseButton.setSelectedIcon (resume);
        soundButton.setSelectedIcon (soundOff);
        instructionsButton.setSelectedIcon (close);
        buttonPane.add (pauseButton);
        buttonPane.add (soundButton);
        buttonPane.add (hintButton);
        buttonPane.add (instructionsButton);

        screen.add (instructionsString, instructionsScreen);
        screen.add (playString, playScreen);
        screen.add (pauseString, pauseScreen);
        screen.add (shuffleString, shuffleScreen);
        add (screen);
        cardLayout.show (screen, playString);

        quitButton.addActionListener (quitHandler);
        pauseScreen.add (quitButton);

        timeBar = new JProgressBar (0, (int) MAX_TIME);

        hintButton.addActionListener (
            new ActionListener () {
                @Override
                public void actionPerformed (ActionEvent event) {
                    if (hintsLeft > 0) {
                        playScreen.showHint ();
                        hintsLeft--;
                        hintButton.setText (Integer.toString (hintsLeft));
                    }
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
                    } else {
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
                        if (!pauseButton.isSelected ()) pause (); //CHANGE PAUSE METHOD TO NOT CHANGE TO PAUSE SCREEN: MUST TURN OFF SOUND
                        setButtonsEnabled (false);
                        cardLayout.show (screen, instructionsString);
                    } else {
                        cardLayout.show (screen, playString);
                        setButtonsEnabled (true);
                        if (!pauseButton.isSelected ()) resume (); //REWRITE TO JUST CHANGE TIME AND TOGGLE SOUND
                    }
                }
            }
        );

        final Runnable runnable = new Runnable() {
            public void run () {
                while (!hasQuit) {
                    logic.updateState ();
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
        cardLayout.show (screen, playString);
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
    }

    public void resume () {
        setButtonsEnabledForPause (true);
        if (volumeOn) playSound();
        logic.resume ();
    }

    private void playSound () {
        sound = new Sound ();
        sound.play ();
    }

    private void stopSound () {
        if (sound != null) sound.shouldQuit = true;
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
                cardLayout.show (screen, shuffleString);
                try { //potential problem: screen will be unresponsive for a whole 2 seconds ********************
                    Thread.sleep (2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                cardLayout.show (screen, playString);
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
