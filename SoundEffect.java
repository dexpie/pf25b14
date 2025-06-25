import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.*;

public enum SoundEffect {
    EAT_FOOD("audio/eatfood.wav"),
    EXPLODE("audio/explode.wav"),
    DIE("audio/die.wav"),
    BACKSOUND("audio/backsound.wav");

    public static enum Volume {
        MUTE, LOW, MEDIUM, HIGH
    }

    public static Volume volume = Volume.LOW;

    private Clip clip;
    private boolean isLooping = false;

    private SoundEffect(String soundFileName) {
        try {
            URL url = this.getClass().getClassLoader().getResource(soundFileName);
            if (url == null) {
                System.err.println("File suara tidak ditemukan: " + soundFileName);
                return;
            }
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(url);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (volume != Volume.MUTE && clip != null) {
            if (clip.isRunning())
                clip.stop();
            clip.setFramePosition(0);
            clip.start();
        }
    }

    public void playLoop() {
        if (volume != Volume.MUTE && clip != null) {
            if (clip.isRunning())
                return;
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            isLooping = true;
        }
    }

    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            isLooping = false;
        }
    }

    public static void setVolume(Volume level) {
        volume = level;
        if (volume == Volume.MUTE) {
            BACKSOUND.stop();
        } else {
            if (!BACKSOUND.isLooping()) {
                BACKSOUND.playLoop();
            }
        }
    }

    public boolean isLooping() {
        return isLooping;
    }

    public static void init() {
        values();
    }
}
