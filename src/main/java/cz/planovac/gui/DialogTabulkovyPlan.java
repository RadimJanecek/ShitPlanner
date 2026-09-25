package cz.planovac.gui;

import cz.planovac.model.Nedostupnost;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class DialogTabulkovyPlan extends JDialog {
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final List<LocalDate> dataRozsah;
    
    private final Map<LocalDate, Map<Integer, TypSmeny>> plan;
    private final List<Pracovnik> pracovnici;
    private final List<Nedostupnost> nedostupnosti;
    private final List<TypSmeny> typySmen;
    private final int minOdpocinek;
    private final String nazevAktualnihoKonceptu; 
    private final LocalDate editovatOd;
    private final LocalDate doDatum;
    private final boolean pouzivatZkratky; 
    private boolean zmenaZProgramu = false; 

    // --- CACHE PRO RYCHLÝ TISK ---
    private final Map<LocalDate, Boolean> cacheSvatku = new java.util.HashMap<>();
    private final Map<String, Color> cacheBarev = new java.util.HashMap<>();
    private static final Color COLOR_SVATEK_BG = new Color(255, 230, 230);
    private static final Color COLOR_VIKEND_BG = new Color(230, 255, 230);
    private static final Color COLOR_HEADER_SVATEK = new Color(230, 80, 80);
    private static final Color COLOR_HEADER_VIKEND = new Color(80, 180, 80);

    public DialogTabulkovyPlan(HlavniOkno owner, Map<LocalDate, Map<Integer, TypSmeny>> plan, List<Pracovnik> pracovnici, 
                               LocalDate editovatOd, LocalDate doDatum, List<Nedostupnost> nedostupnosti, 
                               List<TypSmeny> typySmen, int minOdpocinek, String nazevKonceptu, boolean pouzivatZkratky) {
        super(owner, I18n.t("Plán směn"), true);
        this.plan = plan;
        this.pracovnici = pracovnici;
        this.nedostupnosti = nedostupnosti;
        this.typySmen = typySmen;
        this.minOdpocinek = minOdpocinek;
        this.nazevAktualnihoKonceptu = nazevKonceptu;
        this.editovatOd = editovatOd;
        this.doDatum = doDatum;
        this.pouzivatZkratky = pouzivatZkratky;
        
        setSize(1350, 750); 
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        pracovnici.sort(Comparator.comparing(Pracovnik::getPrijmeni).thenComparing(Pracovnik::getJmeno));

        LocalDate zobrazitOd = editovatOd.minusDays(3);

        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (column == 0 || column == getColumnCount() - 1) return false; 
                LocalDate d = dataRozsah.get(column - 1);
                return !d.isBefore(editovatOd); 
            }
        };

        tableModel.addColumn(I18n.t("Zaměstnanec"));
        dataRozsah = new ArrayList<>();
        LocalDate currentDatum = zobrazitOd;
        while (!currentDatum.isAfter(doDatum)) {
            tableModel.addColumn(currentDatum.format(DateTimeFormatter.ofPattern("dd.MM.")));
            dataRozsah.add(currentDatum);
            currentDatum = currentDatum.plusDays(1);
        }
        tableModel.addColumn(I18n.t("Odpracováno")); 

        for (Pracovnik pracovnik : pracovnici) {
            Object[] rowData = new Object[2 + dataRozsah.size()];
            rowData[0] = pracovnik.getJmeno() + " " + pracovnik.getPrijmeni();
            for (int i = 0; i < dataRozsah.size(); i++) {
                LocalDate datum = dataRozsah.get(i);
                Map<Integer, TypSmeny> smenyDne = plan.get(datum);
                if (smenyDne != null && smenyDne.containsKey(pracovnik.getId())) {
                    rowData[i + 1] = I18n.t(smenyDne.get(pracovnik.getId()).getNazev()); 
                } else {
                    rowData[i + 1] = "-";
                }
            }
            rowData[dataRozsah.size() + 1] = "0 / 0 h"; 
            tableModel.addRow(rowData);
        }

        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(25);

        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        int sirkSloupce = pouzivatZkratky ? 55 : 90;
        for (int i = 0; i < dataRozsah.size(); i++) table.getColumnModel().getColumn(i + 1).setPreferredWidth(sirkSloupce);
        table.getColumnModel().getColumn(dataRozsah.size() + 1).setPreferredWidth(120); 

        JComboBox<String> editorBox = new JComboBox<>();
        editorBox.addItem("-");
        for (TypSmeny typ : typySmen) editorBox.addItem(I18n.t(typ.getNazev()));
        DefaultCellEditor cellEditor = new DefaultCellEditor(editorBox);
        for (int i = 1; i <= dataRozsah.size(); i++) table.getColumnModel().getColumn(i).setCellEditor(cellEditor);

        tableModel.addTableModelListener(e -> {
            if (zmenaZProgramu) return; 
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() > 0 && e.getColumn() <= dataRozsah.size()) {
                zkontrolujManualniZmenu(e.getFirstRow(), e.getColumn());
                prepocitejRadek(e.getFirstRow()); 
            }
        });

        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
                
                if (column == table.getColumnCount() - 1) { 
                    label.setBackground(new Color(200, 200, 200));
                    label.setForeground(Color.BLACK);
                } else if (column > 0) {
                    LocalDate datum = dataRozsah.get(column - 1);
                    if (datum.isBefore(editovatOd)) {
                        label.setBackground(new Color(200, 200, 200));
                        label.setForeground(Color.DARK_GRAY);
                        label.setText("⏳ " + label.getText());
                    } else {
                        label.setBackground(UIManager.getColor("TableHeader.background"));
                        label.setForeground(UIManager.getColor("TableHeader.foreground"));
                        if (isStatniSvatek(datum)) { label.setBackground(COLOR_HEADER_SVATEK); label.setForeground(Color.WHITE); } 
                        else if (datum.getDayOfWeek() == DayOfWeek.SATURDAY || datum.getDayOfWeek() == DayOfWeek.SUNDAY) { label.setBackground(COLOR_HEADER_VIKEND); label.setForeground(Color.WHITE); }
                    }
                }
                return label;
            }
        });

        PlanCellRenderer customRenderer = new PlanCellRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        
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
                MessageFormat header = new MessageFormat(I18n.t("Plán směn") + " " + editovatOd.getMonth().toString());
                MessageFormat footer = new MessageFormat(I18n.t("Strana") + " {0}");
                PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();
                attr.add(OrientationRequested.LANDSCAPE); 
                table.print(JTable.PrintMode.FIT_WIDTH, header, footer, true, attr, true, null);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, I18n.t("Chyba při exportu/tisku: ") + ex.getMessage());
            }
        });
        
        exportPanel.add(exportExcelBtn);
        exportPanel.add(exportPdfBtn);

        JPanel akcePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        
        JButton saveDraftButton = new JButton("💾 " + I18n.t("Uložit koncept"));
        saveDraftButton.setBackground(new Color(230, 255, 230)); 
        saveDraftButton.addActionListener(e -> {
            String navrzenyNazev = (nazevAktualnihoKonceptu != null) ? nazevAktualnihoKonceptu : "Plán " + editovatOd.getMonth().toString();
            String zadanNazev = (String) JOptionPane.showInputDialog(this, I18n.t("Zadejte název:"), I18n.t("Uložit"), JOptionPane.PLAIN_MESSAGE, null, null, navrzenyNazev);
            
            if (zadanNazev != null && !zadanNazev.trim().isEmpty()) {
                String finalNazev = zadanNazev.trim();
                if (owner.existujeKoncept(finalNazev)) {
                    int volba = JOptionPane.showConfirmDialog(this, I18n.t("Koncept s názvem '") + finalNazev + I18n.t("' již existuje.\nChcete jej přepsat?"), I18n.t("Přepsat koncept?"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (volba != JOptionPane.YES_OPTION) return; 
                }
                owner.ulozKoncept(finalNazev, extrahujAktualniPlan());
                dispose(); 
            }
        });
        
        JButton approveButton = new JButton("✅ " + I18n.t("Zapsat do historie"));
        approveButton.setBackground(new Color(200, 255, 200));
        approveButton.addActionListener(e -> {
            int volba = JOptionPane.showConfirmDialog(this, I18n.t("Zapsat jako finální? Tento krok plán vloží do historie."), I18n.t("Schválení plánu"), JOptionPane.YES_NO_OPTION);
            if (volba == JOptionPane.YES_OPTION) {
                owner.ulozDoHlavnihoPlanu(extrahujAktualniPlan());
                dispose();
            }
        });

        akcePanel.add(saveDraftButton);
        akcePanel.add(approveButton);

        bottomContainer.add(exportPanel, BorderLayout.WEST);
        bottomContainer.add(akcePanel, BorderLayout.EAST);
        add(bottomContainer, BorderLayout.SOUTH);

        for (int i = 0; i < pracovnici.size(); i++) prepocitejRadek(i);
    }

    private void prepocitejRadek(int row) {
        Pracovnik p = pracovnici.get(row);
        long pocetDni = java.time.temporal.ChronoUnit.DAYS.between(editovatOd, doDatum) + 1;
        long dnyAbsence = 0;
        for (Nedostupnost n : nedostupnosti) {
            if (n.getPracovnikId() == p.getId()) {
                LocalDate start = editovatOd.isAfter(n.getOdDatum()) ? editovatOd : n.getOdDatum();
                LocalDate end = doDatum.isBefore(n.getDoDatum()) ? doDatum : n.getDoDatum();
                if (!start.isAfter(end)) dnyAbsence += java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
            }
        }
        long pracovniDny = pocetDni - dnyAbsence;
        int ciloveHodiny = (int) Math.round((pracovniDny * 5.0 / 7.0) * p.getUvazek() * 8); 

        int odpracovano = 0;
        for (int col = 1; col <= dataRozsah.size(); col++) {
            LocalDate d = dataRozsah.get(col - 1);
            if (!d.isBefore(editovatOd) && !d.isAfter(doDatum)) {
                Object val = tableModel.getValueAt(row, col);
                if (val != null && !val.toString().equals("-")) {
                    String nazevSmeny = val.toString();
                    TypSmeny ts = typySmen.stream().filter(t -> I18n.t(t.getNazev()).equals(nazevSmeny)).findFirst().orElse(null);
                    if (ts != null) {
                        int delka = (int) java.time.Duration.between(ts.getZacatek(), ts.getKonec()).toHours();
                        if (delka < 0) delka += 24;
                        odpracovano += delka;
                    }
                }
            }
        }

        zmenaZProgramu = true; 
        tableModel.setValueAt(odpracovano + " / " + ciloveHodiny + " h", row, dataRozsah.size() + 1);
        zmenaZProgramu = false;
    }

    private Map<LocalDate, Map<Integer, TypSmeny>> extrahujAktualniPlan() {
        Map<LocalDate, Map<Integer, TypSmeny>> extrakt = new java.util.TreeMap<>();
        for (LocalDate d = editovatOd; !d.isAfter(doDatum); d = d.plusDays(1)) {
            if (plan.containsKey(d)) extrakt.put(d, new java.util.HashMap<>(plan.get(d)));
        }
        return extrakt;
    }

    private void zkontrolujManualniZmenu(int row, int col) {
        String novaHodnota = (String) tableModel.getValueAt(row, col);
        Pracovnik pracovnik = pracovnici.get(row);
        LocalDate datum = dataRozsah.get(col - 1);
        
        Map<Integer, TypSmeny> smenyDne = plan.computeIfAbsent(datum, k -> new java.util.HashMap<>());
        TypSmeny staryTyp = smenyDne.get(pracovnik.getId());
        String staraHodnota = (staryTyp != null) ? I18n.t(staryTyp.getNazev()) : "-";
        
        if (novaHodnota.equals(staraHodnota)) return; 

        TypSmeny novyTyp = typySmen.stream().filter(t -> I18n.t(t.getNazev()).equals(novaHodnota)).findFirst().orElse(null);
        StringBuilder varovani = new StringBuilder();

        if (novyTyp != null) {
            boolean maAbsenci = nedostupnosti.stream().anyMatch(n -> n.getPracovnikId() == pracovnik.getId() && !datum.isBefore(n.getOdDatum()) && !datum.isAfter(n.getDoDatum()));
            if (maAbsenci) varovani.append(I18n.t("⚠️ Zaměstnanec má v tento den nastavenou absenci!\n"));
        }

        if (novyTyp != null) {
            LocalDate vcera = datum.minusDays(1);
            Map<Integer, TypSmeny> smenyVcera = plan.get(vcera);
            if (smenyVcera != null && smenyVcera.containsKey(pracovnik.getId())) {
                TypSmeny vcerejsiSmena = smenyVcera.get(pracovnik.getId());
                LocalDateTime konecVcera = vcera.atTime(vcerejsiSmena.getKonec());
                if (vcerejsiSmena.getKonec().isBefore(vcerejsiSmena.getZacatek())) konecVcera = konecVcera.plusDays(1);
                LocalDateTime zacatekDnes = datum.atTime(novyTyp.getZacatek());
                long odpocinek = java.time.Duration.between(konecVcera, zacatekDnes).toHours();
                if (odpocinek < minOdpocinek) varovani.append(I18n.t("⚠️ Porušení odpočinku!\n"));
            }
        }

        if (varovani.length() > 0) {
            int volba = JOptionPane.showConfirmDialog(this, varovani.toString() + I18n.t("Pokračovat?"), I18n.t("Porušení pravidel"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (volba == JOptionPane.NO_OPTION) {
                zmenaZProgramu = true; tableModel.setValueAt(staraHodnota, row, col); zmenaZProgramu = false;
                return; 
            }
        }

        if (novyTyp == null) smenyDne.remove(pracovnik.getId());
        else smenyDne.put(pracovnik.getId(), novyTyp);
    }

    class PlanCellRenderer extends DefaultTableCellRenderer {
        public PlanCellRenderer() { setHorizontalAlignment(JLabel.CENTER); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setBackground(Color.WHITE); c.setForeground(Color.BLACK);
            
            if (column == table.getColumnCount() - 1) {
                c.setFont(c.getFont().deriveFont(Font.BOLD));
                try {
                    String[] parts = value.toString().replace(" h", "").split(" / ");
                    int actual = Integer.parseInt(parts[0].trim());
                    int target = Integer.parseInt(parts[1].trim());
                    if (actual > target) { c.setBackground(COLOR_SVATEK_BG); c.setForeground(new Color(180, 0, 0)); } 
                    else if (actual < target) { c.setBackground(new Color(255, 245, 210)); c.setForeground(new Color(180, 100, 0)); } 
                    else { c.setBackground(COLOR_VIKEND_BG); c.setForeground(new Color(0, 120, 0)); }
                } catch (Exception ex) {}
                return c;
            }

            if (column > 0 && column < table.getColumnCount() - 1) {
                LocalDate datum = dataRozsah.get(column - 1);
                boolean isHistory = datum.isBefore(editovatOd);
                
                if (isHistory) {
                    c.setBackground(new Color(245, 245, 245)); c.setForeground(Color.GRAY);
                } else if (isStatniSvatek(datum)) c.setBackground(COLOR_SVATEK_BG); 
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
                    if ((t.equalsIgnoreCase("Noční") || t.equalsIgnoreCase("Night")) && !isHistory) c.setForeground(Color.WHITE);
                    
                    if (pouzivatZkratky) setText(t.substring(0, 1).toUpperCase());
                }
            }
            if (isSelected) { c.setBackground(table.getSelectionBackground()); c.setForeground(table.getSelectionForeground()); }
            return c;
        }
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
        fileChooser.setSelectedFile(new File("plan_smen.csv"));
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
                JOptionPane.showMessageDialog(this, I18n.t("Plán byl úspěšně exportován!"), I18n.t("Úspěch"), JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {}
        }
    }
}