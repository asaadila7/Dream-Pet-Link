import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.event.*;
import java.awt.*;

public class App {
    static class AppFrame extends JFrame {
        public Game game;
        public Menu menu;
        private boolean volumeOn;
        private boolean playing;

        public AppFrame () {
            super ("Dream Pet Link");
            playing = false;
            volumeOn = false;
            menu = new Menu (this);
            setContentPane (menu);
            pack ();
            setVisible (true);
    
            this.addWindowListener (new WindowAdapter () { //window listener to pause game on minimize
                @Override
                public void windowDeiconified (WindowEvent e) {
                    if (playing) game.resume ();
                }
    
                @Override
                public void windowIconified (WindowEvent e) {
                    if (playing) game.pause ();
                }
            });
        
            //runnable to run game
            final Runnable r = new Runnable() {
                public void run () {
                    while (true) {
                        if (playing && game.gameOver ()) {
                            playing = false;
                            volumeOn = game.hasSound();
                            game = null;
                            setContainer (menu);
                        }
                    }
                }
            };
            new Thread (r).start();
    
            setResizable (false);
            setDefaultCloseOperation (WindowConstants.EXIT_ON_CLOSE);
        }

        public void setSound (boolean isOn) {
            volumeOn = isOn;
        }

        private void setContainer (Container container) { //will this make a copy of the container or send it the original object?
            setContentPane (container);
            pack ();
            revalidate ();
        }

        public void startGame () {
            playing = true;
            game = new Game ();
            setContainer (game);
            game.startGame (volumeOn);
        }
    }

    public static Logic logic;
    public static boolean soundOn;
    public static AppFrame frame;

    public static void main (String[] args) {
        frame = new AppFrame ();
    }
}
