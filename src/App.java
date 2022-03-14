import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.event.*;
import java.awt.*;

//volume and mute: <a href="https://www.flaticon.com/free-icons/speaker" title="speaker icons">Speaker icons created by Pixel perfect - Flaticon</a>
//question mark: <a href="https://www.flaticon.com/free-icons/doubt" title="doubt icons">Doubt icons created by Freepik - Flaticon</a>
//x: <a href="https://www.flaticon.com/free-icons/close" title="close icons">Close icons created by inkubators - Flaticon</a>
//pause: <a href="https://www.flaticon.com/free-icons/pause" title="pause icons">Pause icons created by Kiranshastry - Flaticon</a>
//resume: <a href="https://www.flaticon.com/free-icons/pause" title="pause icons">Pause icons created by IYAHICON - Flaticon</a>
//background image for pause screen is Pawel Czerwinski on unsplash
//empty square : <a href="https://www.flaticon.com/free-icons/rounded-square" title="rounded square icons">Rounded square icons created by Freepik - Flaticon</a>
//hint: <a href="https://www.flaticon.com/free-icons/magnifying-glass" title="magnifying glass icons">Magnifying glass icons created by Ayub Irawan - Flaticon</a>

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
            setResizable (false);
            this.setDefaultCloseOperation (WindowConstants.EXIT_ON_CLOSE);

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

    public static AppFrame frame;

    public static void main (String[] args) {
        frame = new AppFrame ();
    }
}
