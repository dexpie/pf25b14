import javax.swing.*;

/** Tic-tac-toe Applet */
@SuppressWarnings("serial")
public class AppletMain extends JApplet {

    /** init() to setup the GUI components */
    @Override
    public void init() {
        // Run GUI codes in the Event-Dispatching thread for thread safety
        try {
            // Use invokeAndWait() to ensure that init() exits after GUI construction
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