import java.awt.*;
public class Cell {
    public static final int SIZE = 120;
    public static final int PADDING = SIZE / 5;
    public static final int SEED_SIZE = SIZE - PADDING * 2;

    Seed content;
    int row, col;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        content = Seed.NO_SEED;
    }

    public void newGame() {
        content = Seed.NO_SEED;
    }

    public void paint(Graphics g, boolean highlight) {
        int x1 = col * SIZE + PADDING;
        int y1 = row * SIZE + PADDING;
        if (content == Seed.CROSS || content == Seed.NO_SEED) {
            g.drawImage(content.getImage(), x1, y1, SEED_SIZE, SEED_SIZE, null);
            if (highlight) {
                Graphics2D g2 = (Graphics2D) g;
                Stroke oldStroke = g2.getStroke();
                g2.setColor(Color.YELLOW);
                g2.setStroke(new BasicStroke(6));
                g2.drawOval(x1, y1, SEED_SIZE, SEED_SIZE);
                g2.setStroke(oldStroke);
            }
        }
    }

    public void paint(Graphics g) {
        paint(g, false);
    }
}