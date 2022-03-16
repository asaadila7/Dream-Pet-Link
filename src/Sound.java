import javax.sound.sampled.*;
import java.io.IOException;

//TODO: Sound is stopping after playing only once
//can i make sound not have to start from beginning when paused and run again?

public class Sound implements Runnable {
    //private String fileLocation = new File ("./Soundtrack.wav").getPath ();
    private String fileName = "Resources/bensound-tenderness.wav";
    private AudioInputStream audioInputStream = null;
    private SourceDataLine line = null;
    public volatile boolean shouldQuit; //true when music should stop playing

    public Sound () {
        try {
            audioInputStream = AudioSystem.getAudioInputStream (this.getClass ().getResource (fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }

        AudioFormat audioFormat = audioInputStream.getFormat(); //get file format

        try {
            line = (SourceDataLine) AudioSystem.getLine (new DataLine.Info (SourceDataLine.class, audioFormat));
            line.open (audioFormat);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run () {
        line.start ();

        while (!shouldQuit) {
            playSound ();
        }

        System.out.println ("Should quit");

        line.drain();
        line.close();

        try {
            audioInputStream.close ();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void play () {
        shouldQuit = false;
        Thread t = new Thread (this); //new thread to play audio synchronously
        t.start();
    }

    private void playSound () {
        int nBytesRead = 0;
        byte [] abData = new byte [500];

        while (nBytesRead != -1) {
            if (shouldQuit) {
                return;
            }

            try {
                nBytesRead = audioInputStream.read (abData, 0, abData.length);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (nBytesRead >= 0) {
                line.write (abData, 0, nBytesRead);
            }
        }
    }
}