import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class SymbolChooserPanel extends JPanel {
    public interface SymbolChoiceListener {
        void symbolChosen(Seed chosen);
    }

    private final SymbolChoiceListener listener;

    public SymbolChooserPanel(SymbolChoiceListener listener) {
        this.listener = listener;
        setLayout(new FlowLayout(FlowLayout.CENTER, 40, 40));
        setBackground(GameMain.COLOR_BG);

        ImageIcon iconX = new ImageIcon(getClass().getResource("/images/cross.png"));
        ImageIcon iconO = new ImageIcon(getClass().getResource("/images/nought.png"));

        JButton btnX = new JButton(iconX);
        btnX.setPreferredSize(new Dimension(Cell.SIZE, Cell.SIZE));
        btnX.setToolTipText("Play as X");
        btnX.addActionListener(e -> listener.symbolChosen(Seed.CROSS));

        JButton btnO = new JButton(iconO);
        btnO.setPreferredSize(new Dimension(Cell.SIZE, Cell.SIZE));
        btnO.setToolTipText("Play as O");
        btnO.addActionListener(e -> listener.symbolChosen(Seed.NOUGHT));

        add(btnX);
        add(btnO);
    }
}
