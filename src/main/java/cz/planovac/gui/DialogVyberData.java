package cz.planovac.gui;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class DialogVyberData extends JDialog {
    private LocalDate vybraneDatum = null;
    private YearMonth aktualniMesic;
    
    private final JLabel mesicRokLabel;
    private final JTable kalendarTable;
    private final DefaultTableModel kalendarModel;

    public DialogVyberData(JDialog parent, LocalDate vychoziDatum) {
        super(parent, "Vyberte datum", true);
        inicializuj(vychoziDatum);
        // Tyto řádky musíme dát až po inicializaci, aby překladač neřval
        mesicRokLabel = new JLabel("", SwingConstants.CENTER);
        kalendarModel = new DefaultTableModel(null, new String[]{"Po", "Út", "St", "Čt", "Pá", "So", "Ne"}) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        kalendarTable = new JTable(kalendarModel);
        sestavGUI();
    }

    public DialogVyberData(JFrame parent, LocalDate vychoziDatum) {
        super(parent, "Vyberte datum", true);
        inicializuj(vychoziDatum);
        mesicRokLabel = new JLabel("", SwingConstants.CENTER);
        kalendarModel = new DefaultTableModel(null, new String[]{"Po", "Út", "St", "Čt", "Pá", "So", "Ne"}) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        kalendarTable = new JTable(kalendarModel);
        sestavGUI();
    }
    
    private void inicializuj(LocalDate vychoziDatum) {
        setSize(300, 250);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        
        if (vychoziDatum != null) {
            this.aktualniMesic = YearMonth.from(vychoziDatum);
        } else {
            this.aktualniMesic = YearMonth.now();
        }
    }

    private void sestavGUI() {
        // Horní panel pro přepínání měsíců
        JPanel topPanel = new JPanel(new BorderLayout());
        JButton predchoziButton = new JButton("<");
        JButton dalsiButton = new JButton(">");
        
        mesicRokLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        predchoziButton.addActionListener(e -> zmenMesic(-1));
        dalsiButton.addActionListener(e -> zmenMesic(1));
        
        topPanel.add(predchoziButton, BorderLayout.WEST);
        topPanel.add(mesicRokLabel, BorderLayout.CENTER);
        topPanel.add(dalsiButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Tabulka kalendáře
        kalendarTable.setRowHeight(25);
        kalendarTable.setCellSelectionEnabled(true);
        kalendarTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Vykreslovač pro lepší vzhled
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (value == null || value.toString().isEmpty()) {
                    c.setBackground(Color.WHITE);
                } else if (column >= 5) { // Víkendy
                    c.setBackground(new Color(230, 255, 230));
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                if (isSelected && value != null && !value.toString().isEmpty()) {
                    c.setBackground(new Color(173, 216, 230)); // Vybraný den modře
                }
                return c;
            }
        };
        for (int i = 0; i < 7; i++) kalendarTable.getColumnModel().getColumn(i).setCellRenderer(renderer);

        // Reakce na dvojklik nebo stisk Enter
        kalendarTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) potvrdVyber();
            }
        });
        
        add(new JScrollPane(kalendarTable), BorderLayout.CENTER);

        // Spodní tlačítka
        JPanel bottomPanel = new JPanel();
        JButton okButton = new JButton("Vybrat");
        JButton stornoButton = new JButton("Zrušit");
        
        okButton.addActionListener(e -> potvrdVyber());
        stornoButton.addActionListener(e -> dispose());
        
        bottomPanel.add(okButton);
        bottomPanel.add(stornoButton);
        add(bottomPanel, BorderLayout.SOUTH);

        vykresliMesic();
    }

    private void zmenMesic(int posun) {
        aktualniMesic = aktualniMesic.plusMonths(posun);
        vykresliMesic();
    }

    private void vykresliMesic() {
        mesicRokLabel.setText(aktualniMesic.getMonthValue() + " / " + aktualniMesic.getYear());
        kalendarModel.setRowCount(0); // Vyčistit

        LocalDate prvniDen = aktualniMesic.atDay(1);
        int posunDnu = prvniDen.getDayOfWeek().getValue() - 1; // Po je 1, Ne je 7
        int pocetDni = aktualniMesic.lengthOfMonth();

        String[] tyden = new String[7];
        int denVMesici = 1;

        // První týden
        for (int i = 0; i < 7; i++) {
            if (i >= posunDnu) {
                tyden[i] = String.valueOf(denVMesici++);
            } else {
                tyden[i] = "";
            }
        }
        kalendarModel.addRow(tyden);

        // Zbytek měsíce
        while (denVMesici <= pocetDni) {
            tyden = new String[7];
            for (int i = 0; i < 7 && denVMesici <= pocetDni; i++) {
                tyden[i] = String.valueOf(denVMesici++);
            }
            kalendarModel.addRow(tyden);
        }
    }

    private void potvrdVyber() {
        int row = kalendarTable.getSelectedRow();
        int col = kalendarTable.getSelectedColumn();
        if (row != -1 && col != -1) {
            Object hodnota = kalendarModel.getValueAt(row, col);
            if (hodnota != null && !hodnota.toString().isEmpty()) {
                int den = Integer.parseInt(hodnota.toString());
                vybraneDatum = aktualniMesic.atDay(den);
                dispose();
            }
        }
    }

    public LocalDate getVybraneDatum() {
        return vybraneDatum;
    }
}