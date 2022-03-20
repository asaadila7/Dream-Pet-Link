import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.event.*;
import java.awt.*;

//ask bout pass by value vs. pass by reference in java

public class App {
    static class AppFrame extends JFrame {
        public Game game;
        public Menu menu;
        private boolean volumeOn;

        public AppFrame () {
            super ("Dream Pet Link");
            volumeOn = false;
            menu = new Menu (this, volumeOn);
            setContainer (menu);
            setVisible (true);
            //setResizable (false);
            this.setDefaultCloseOperation (WindowConstants.EXIT_ON_CLOSE);

            this.addWindowListener (new WindowAdapter () { //window listener to pause game on minimize
                @Override
                public void windowDeiconified (WindowEvent e) {
                    if (game != null && !game.isPaused ()) game.resume ();
                }

                @Override
                public void windowIconified (WindowEvent e) {
                    if (game != null) game.pause ();
                }

                @Override
                public void windowActivated (WindowEvent e) {
                    if (game != null && !game.isPaused ()) game.resume ();
                }

                @Override
                public void windowDeactivated (WindowEvent e) {
                    if (game != null) game.pause ();
                }
            });

            final Runnable runnable = new Runnable() {
                public void run () {
                    while (true) runFrame ();
                }
            };
            new Thread (runnable).start ();
        }

        public void runFrame () {
            if (game != null && game.gameOver ()) {
                volumeOn = game.hasSound();
                game = null;
                menu.setSoundOn (volumeOn);
                setContainer (menu);
            }
        }

        public void setSound (boolean isOn) {
            volumeOn = isOn;
        }

        private void setContainer (Container container) { //will this make a copy of the container or send it the original object?
            setContentPane (container);
            pack ();
            revalidate ();
            setLocationRelativeTo (null);
        }

        public void startGame () {
            game = new Game ();
            setContainer (game);
            pack ();
            game.startGame (volumeOn);
        }
    }

    public static AppFrame frame;

    public static void main (String[] args) {
        frame = new AppFrame ();
    }
}
