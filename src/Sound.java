import javax.sound.sampled.*;
import java.io.IOException;

public class Sound {
    private static final String fileName = "Resources/russian-land-loop-88761.wav";
    private AudioInputStream audioInputStream = null;
    private Clip line = null;

    public Sound () {
        try {
            line = AudioSystem.getClip ();
            audioInputStream = AudioSystem.getAudioInputStream (this.getClass ().getResource (fileName));
            line.open (audioInputStream);
            line.setLoopPoints (0, -1);
        } catch (Exception e) {
            e.printStackTrace ();
        }
    }

    public void play () {
        line.loop (Clip.LOOP_CONTINUOUSLY);
        line.start ();
    }

    public void pause () {
        line.stop ();
    }

    public void dispose () {
        line.drain();
        line.close();

        try {
            audioInputStream.close ();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}