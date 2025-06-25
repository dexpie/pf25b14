import javax.swing.*;


@SuppressWarnings("serial")
public class AppletMain extends JApplet {

    @Override
    public void init() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    setContentPane(new GameMain());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}