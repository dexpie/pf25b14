import java.io.File;
import javax.sound.sampled.*;

public class BackgroundMusic {
    private static Clip clip;

    public static void play(String filepath) {
        try {
            if (clip != null && clip.isRunning()) {
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File(filepath));
            clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
    }
}
