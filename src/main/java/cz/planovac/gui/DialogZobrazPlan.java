package cz.planovac.gui;

import cz.planovac.model.Pracovnik;
import cz.planovac.model.TypSmeny;
import cz.planovac.utils.I18n;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class DialogZobrazPlan extends JDialog {
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final List<LocalDate> dataRozsah;
    private final boolean pouzivatZkratky; 

    // --- CACHE PRO RYCHLÝ TISK ---
    private final Map<LocalDate, Boolean> cacheSvatku = new java.util.HashMap<>();
    private final Map<String, Color> cacheBarev = new java.util.HashMap<>();
    private static final Color COLOR_SVATEK_BG = new Color(255, 230, 230);
    private static final Color COLOR_VIKEND_BG = new Color(230, 255, 230);
    private static final Color COLOR_HISTORIE_BG = new Color(245, 245, 245);
    private static final Color COLOR_HEADER_SVATEK = new Color(230, 80, 80);
    private static final Color COLOR_HEADER_VIKEND = new Color(80, 180, 80);

    public DialogZobrazPlan(Frame owner, Map<LocalDate, Map<Integer, TypSmeny>> plan, List<Pracovnik> pracovnici, LocalDate odDatum, LocalDate doDatum, boolean pouzivatZkratky) {
        super(owner, I18n.t("📅 Prohlížet historii (Schválené plány)"), true);
        this.pouzivatZkratky = pouzivatZkratky;
        setSize(1350, 750);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        List<Pracovnik> setrideniPracovnici = new ArrayList<>(pracovnici);
        setrideniPracovnici.sort(Comparator.comparing(Pracovnik::getPrijmeni).thenComparing(Pracovnik::getJmeno));

        tableModel = new DefaultTableModel() {
            @Override public boolean isCellEditable(int row, int column) { return false; } 
        };

        tableModel.addColumn(I18n.t("Zaměstnanec"));
        dataRozsah = new ArrayList<>();
        LocalDate currentDatum = odDatum;
        while (!currentDatum.isAfter(doDatum)) {
            tableModel.addColumn(currentDatum.format(DateTimeFormatter.ofPattern("dd.MM.")));
            dataRozsah.add(currentDatum);
            currentDatum = currentDatum.plusDays(1);
        }
        tableModel.addColumn(I18n.t("Odpracováno"));

        for (Pracovnik p : setrideniPracovnici) {
            Object[] rowData = new Object[2 + dataRozsah.size()];
            rowData[0] = p.getJmeno() + " " + p.getPrijmeni();
            int odpracovanoHodin = 0;

            for (int i = 0; i < dataRozsah.size(); i++) {
                LocalDate datum = dataRozsah.get(i);
                Map<Integer, TypSmeny> smenyDne = plan.get(datum);
                if (smenyDne != null && smenyDne.containsKey(p.getId())) {
                    TypSmeny ts = smenyDne.get(p.getId());
                    rowData[i + 1] = I18n.t(ts.getNazev());
                    int delka = (int) java.time.Duration.between(ts.getZacatek(), ts.getKonec()).toHours();
                    if (delka < 0) delka += 24;
                    odpracovanoHodin += delka;
                } else {
                    rowData[i + 1] = "-";
                }
            }
            rowData[dataRozsah.size() + 1] = odpracovanoHodin + " h";
            tableModel.addRow(rowData);
        }

        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(25);

        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        int sirkSloupce = pouzivatZkratky ? 55 : 90;
        for (int i = 0; i < dataRozsah.size(); i++) table.getColumnModel().getColumn(i + 1).setPreferredWidth(sirkSloupce);
        table.getColumnModel().getColumn(dataRozsah.size() + 1).setPreferredWidth(100);

        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
                
                if (column > 0 && column <= dataRozsah.size()) {
                    LocalDate datum = dataRozsah.get(column - 1);
                    label.setBackground(UIManager.getColor("TableHeader.background"));
                    label.setForeground(UIManager.getColor("TableHeader.foreground"));
                    if (isStatniSvatek(datum)) { label.setBackground(COLOR_HEADER_SVATEK); label.setForeground(Color.WHITE); } 
                    else if (datum.getDayOfWeek() == DayOfWeek.SATURDAY || datum.getDayOfWeek() == DayOfWeek.SUNDAY) { label.setBackground(COLOR_HEADER_VIKEND); label.setForeground(Color.WHITE); }
                }
                return label;
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                c.setBackground(Color.WHITE); c.setForeground(Color.BLACK);
                
                if (column == table.getColumnCount() - 1) {
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                    c.setBackground(new Color(240, 240, 240));
                    return c;
                }

                if (column > 0 && column < table.getColumnCount() - 1) {
                    LocalDate datum = dataRozsah.get(column - 1);
                    if (isStatniSvatek(datum)) c.setBackground(COLOR_SVATEK_BG); 
                    else if (datum.getDayOfWeek() == DayOfWeek.SATURDAY || datum.getDayOfWeek() == DayOfWeek.SUNDAY) c.setBackground(COLOR_VIKEND_BG);
                    
                    if (value != null && !value.toString().equals("-")) {
                        String t = value.toString();
                        Color bg = cacheBarev.computeIfAbsent(t, key -> {
                            if (key.startsWith("Ranní") || key.startsWith("Morning")) return new Color(255, 255, 153);
                            if (key.startsWith("Odpolední") || key.startsWith("Afternoon")) return new Color(173, 216, 230);
                            if (key.startsWith("Noční") || key.startsWith("Night")) return new Color(100, 100, 100);
                            int hash = Math.abs(t.hashCode());
                            return new Color(200 + (hash % 50), 200 + ((hash / 50) % 50), 200 + ((hash / 2500) % 50));
                        });
                        c.setBackground(bg);
                        if ((t.equalsIgnoreCase("Noční") || t.equalsIgnoreCase("Night"))) c.setForeground(Color.WHITE);
                        
                        if (pouzivatZkratky) setText(t.substring(0, 1).toUpperCase());
                    }
                }
                if (isSelected) { c.setBackground(table.getSelectionBackground()); c.setForeground(table.getSelectionForeground()); }
                return c;
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        exportPanel.setBorder(BorderFactory.createTitledBorder(I18n.t("Exporty")));
        
        JButton exportExcelBtn = new JButton("📊 " + I18n.t("Excel (.csv)"));
        exportExcelBtn.addActionListener(e -> exportDoCSV());
        
        // --- ČISTÝ PDF TISK (bez okna) ---
        JButton exportPdfBtn = new JButton("📄 " + I18n.t("PDF"));
        exportPdfBtn.setToolTipText(I18n.t("Otevře systémový dialog tisku. Zvolte 'Microsoft Print to PDF' pro uložení souboru."));
        exportPdfBtn.addActionListener(e -> {
            try {
                MessageFormat header = new MessageFormat(I18n.t("📅 Prohlížet historii (Schválené plány)"));
                MessageFormat footer = new MessageFormat(I18n.t("Strana") + " {0}");
                PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();
                attr.add(OrientationRequested.LANDSCAPE); 
                table.print(JTable.PrintMode.FIT_WIDTH, header, footer, true, attr, true, null);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, I18n.t("Chyba tisku: ") + ex.getMessage());
            }
        });
        
        exportPanel.add(exportExcelBtn);
        exportPanel.add(exportPdfBtn);

        JPanel akcePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton closeButton = new JButton(I18n.t("Zavřít historii"));
        closeButton.addActionListener(e -> dispose());
        akcePanel.add(closeButton);

        bottomContainer.add(exportPanel, BorderLayout.WEST);
        bottomContainer.add(akcePanel, BorderLayout.EAST);
        
        add(bottomContainer, BorderLayout.SOUTH);
    }

    private boolean isStatniSvatek(LocalDate date) {
        Boolean cached = cacheSvatku.get(date);
        if (cached != null) return cached;
        
        int d = date.getDayOfMonth(), m = date.getMonthValue();
        if ((d == 1 && m == 1) || (d == 1 && m == 5) || (d == 8 && m == 5) || (d == 5 && m == 7) || (d == 6 && m == 7) || 
            (d == 28 && m == 9) || (d == 28 && m == 10) || (d == 17 && m == 11) || (d == 24 && m == 12) || (d == 25 && m == 12) || (d == 26 && m == 12)) {
            cacheSvatku.put(date, true); return true;
        }
        int y = date.getYear(), a = y % 19, b = y / 100, c = y % 100, d1 = b / 4, e = b % 4, f = (b + 8) / 25, g = (b - f + 1) / 3;
        int h = (19 * a + b - d1 - g + 15) % 30, i = c / 4, k = c % 4, l = (32 + 2 * e + 2 * i - h - k) % 7, m1 = (a + 11 * h + 22 * l) / 451;
        LocalDate easterSunday = LocalDate.of(y, (h + l - 7 * m1 + 114) / 31, ((h + l - 7 * m1 + 114) % 31) + 1);
        boolean isSvatek = date.equals(easterSunday.plusDays(1)) || date.equals(easterSunday.minusDays(2));
        cacheSvatku.put(date, isSvatek);
        return isSvatek;
    }

    private void exportDoCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("historie_smen.csv"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();
            if (!f.getName().toLowerCase().endsWith(".csv")) f = new File(f.getParentFile(), f.getName() + ".csv");
            try (FileWriter fw = new FileWriter(f, java.nio.charset.StandardCharsets.UTF_8)) {
                fw.write('\ufeff'); 
                for (int i = 0; i < tableModel.getColumnCount(); i++) fw.write(tableModel.getColumnName(i) + (i < tableModel.getColumnCount() - 1 ? ";" : ""));
                fw.write("\n");
                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    for (int col = 0; col < tableModel.getColumnCount(); col++) {
                        Object val = tableModel.getValueAt(row, col);
                        String strVal = (val != null ? val.toString() : "");
                        
                        if (pouzivatZkratky && col > 0 && col < tableModel.getColumnCount() - 1 && !strVal.equals("-") && !strVal.contains(" h")) {
                            strVal = strVal.substring(0, 1).toUpperCase();
                        }
                        
                        fw.write(strVal + (col < tableModel.getColumnCount() - 1 ? ";" : ""));
                    }
                    fw.write("\n");
                }
                JOptionPane.showMessageDialog(this, I18n.t("Historie úspěšně exportována!"), I18n.t("Úspěch"), JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {}
        }
    }
}