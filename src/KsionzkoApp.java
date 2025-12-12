import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;

public class KsionzkoApp extends JFrame {
    private JTable table;
    private JTextField tytulField;
    private JTextField autorField;
    private JTextField rokField;
    private JButton addBtn;
    private JButton delBtn;
    private JButton updBtn;
    private Tabela model;

    public KsionzkoApp() {
        super("Biblioteka ale fancy");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout());

        model = new Tabela();
        table = new JTable(model);

        JPanel inputPanel = new JPanel(new GridLayout(2, 3));
        tytulField = new JTextField();
        autorField = new JTextField();
        rokField = new JTextField();
        inputPanel.add(new JLabel("Tytuł"));
        inputPanel.add(new JLabel("Autor"));
        inputPanel.add(new JLabel("Rok wydania"));
        inputPanel.add(tytulField);
        inputPanel.add(autorField);
        inputPanel.add(rokField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        addBtn = new JButton("Dodaj");
        delBtn = new JButton("Usuń");
        updBtn = new JButton("Aktualizuj");
        buttonPanel.add(addBtn);
        buttonPanel.add(delBtn);
        buttonPanel.add(updBtn);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> dodajKsiazke());
        delBtn.addActionListener(e -> usunKsiazke());
        updBtn.addActionListener(e -> aktualizujKsiazke());

        odswiezTabele();
        setVisible(true);
    }

    private Connection polacz() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/4tp_ksiegozbior", "root", "");
    }

    private void dodajKsiazke() {
        try (Connection conn = polacz()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO ksiazki (tytul, autor, rok_wydania) VALUES (?, ?, ?)");
            ps.setString(1, tytulField.getText());
            ps.setString(2, autorField.getText());
            ps.setInt(3, Integer.parseInt(rokField.getText()));
            ps.executeUpdate();
            odswiezTabele();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void usunKsiazke() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int id = (int) model.getValueAt(row, 0);
            try (Connection conn = polacz()) {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM ksiazki WHERE id=?");
                ps.setInt(1, id);
                ps.executeUpdate();
                odswiezTabele();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void aktualizujKsiazke() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int id = (int) model.getValueAt(row, 0);
            try (Connection conn = polacz()) {
                PreparedStatement ps = conn.prepareStatement("UPDATE ksiazki SET tytul=?, autor=?, rok_wydania=? WHERE id=?");
                ps.setString(1, tytulField.getText());
                ps.setString(2, autorField.getText());
                ps.setInt(3, Integer.parseInt(rokField.getText()));
                ps.setInt(4, id);
                ps.executeUpdate();
                odswiezTabele();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void odswiezTabele() {
        try (Connection conn = polacz()) {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM ksiazki");
            List<Object[]> dane = new ArrayList<>();
            while (rs.next()) {
                dane.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("tytul"),
                        rs.getString("autor"),
                        rs.getInt("rok_wydania")
                });
            }
            model.setData(dane);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(KsionzkoApp::new);
    }
}

class Tabela extends AbstractTableModel {
    private String[] nazwyKolumn = {"ID", "Tytuł", "Autor", "Rok wydania"};
    private List<Object[]> dane = new ArrayList<>();

    public void setData(List<Object[]> dane) {
        this.dane = dane;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return dane.size();
    }

    @Override
    public int getColumnCount() {
        return nazwyKolumn.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return dane.get(rowIndex)[columnIndex];
    }

    @Override
    public String getColumnName(int column) {
        return nazwyKolumn[column];
    }
}