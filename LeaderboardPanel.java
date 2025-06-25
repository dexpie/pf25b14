import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;

public class LeaderboardPanel extends JPanel {
    private JTable table;
    private LeaderboardTableModel tableModel;

    public LeaderboardPanel() {
        setLayout(new BorderLayout());
        tableModel = new LeaderboardTableModel();
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);
        refreshLeaderboard();
    }

    public void refreshLeaderboard() {
        List<String[]> data = LeaderboardUtil.getTopPlayers(10);
        tableModel.setData(data);
    }

    // Table model for leaderboard
    private static class LeaderboardTableModel extends AbstractTableModel {
        private String[] columns = {"Nickname", "Win", "Draw", "Lose"};
        private List<String[]> data;

        public void setData(List<String[]> data) {
            this.data = data;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int col) {
            return columns[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            return data.get(row)[col];
        }
    }
}
