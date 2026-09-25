package cz.planovac.gui;

import cz.planovac.model.Nedostupnost;
import cz.planovac.model.PozadavekNaObsazeniSmeny;
import cz.planovac.model.Pracovnik;
import cz.planovac.model.TypSmeny;
import cz.planovac.planner.Planovac;
import cz.planovac.pravidla.DveNocniPoSobePravidlo;
import cz.planovac.pravidla.Pravidlo;
import cz.planovac.utils.I18n;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import com.google.gson.*;

public class HlavniOkno extends JFrame {
    private final JTextField jmenoField;
    private final JTextField prijmeniField;
    private final JTextField uvazekField;
    private final DefaultListModel<Pracovnik> seznamZamestnancuModel;
    private final JList<Pracovnik> seznamZamestnancuList;

    private final List<Pracovnik> seznamZamestnancu;
    private final List<Nedostupnost> seznamNedostupnosti;
    private final List<int[]> zakazanePary;
    private final List<PozadavekNaObsazeniSmeny> pozadavkyNaObsazeniSmen;
    private final List<TypSmeny> typySmen = new ArrayList<>();
    
    private Map<LocalDate, Map<Integer, TypSmeny>> celkovyPlan = new TreeMap<>();
    private Map<String, Map<LocalDate, Map<Integer, TypSmeny>>> ulozeneKoncepty = new HashMap<>();

    private final JTextField datumOdField;
    private final JTextField datumDoField;

    private final JList<Nedostupnost> seznamNedostupnostiList;
    private final DefaultListModel<Nedostupnost> seznamNedostupnostiModel;
    private final JLabel kapacitaLabel;
    
    private int minimalniOdpocinekHodin = 11;
    private boolean brigadnikNesmiBytSam = true;
    private boolean pouzivatZkratkySmen = false;

    private static final String DATA_FILE = "data.json";
    private static final AtomicInteger nextPracovnikId = new AtomicInteger(1);

    public HlavniOkno() {
        super(); // Prázdné vytvoření
        nactiDataRychleProJazyk(); // Načte jazyk ze souboru
        setTitle(I18n.t("Plánovač směn")); // AŽ POTÉ se nastaví titulek v liště!

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 750);
        setMinimumSize(new Dimension(850, 600));
        setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();
        
        JMenu menuMoznosti = new JMenu(I18n.t("Možnosti"));
        JMenuItem nastaveniItem = new JMenuItem(I18n.t("⚙️ Nastavení aplikace"));
        nastaveniItem.addActionListener(e -> otevriNastaveni());
        menuMoznosti.add(nastaveniItem);

        JMenu menuPravidla = new JMenu(I18n.t("Pravidla")); 
        JMenuItem typySmenItem = new JMenuItem(I18n.t("Typy směn"));
        typySmenItem.addActionListener(e -> spravovatTypySmen());
        JMenuItem zakazaneParyItem = new JMenuItem(I18n.t("Zakázané páry"));
        zakazaneParyItem.addActionListener(e -> spravovatZakazanePary());
        JMenuItem pozadavkyItem = new JMenuItem(I18n.t("Požadavky na obsazení směn"));
        pozadavkyItem.addActionListener(e -> spravovatPozadavky());
        menuPravidla.add(typySmenItem);
        menuPravidla.add(zakazaneParyItem);
        menuPravidla.add(pozadavkyItem);

        JMenu menuKoncepty = new JMenu(I18n.t("Koncepty"));
        JMenuItem spravovatKonceptyItem = new JMenuItem(I18n.t("📂 Zobrazit koncepty"));
        spravovatKonceptyItem.addActionListener(e -> spravovatKoncepty());
        menuKoncepty.add(spravovatKonceptyItem);

        JMenu menuHistorie = new JMenu(I18n.t("Historie"));
        JMenuItem zobrazitHistoriiItem = new JMenuItem(I18n.t("📅 Zobrazit historii"));
        zobrazitHistoriiItem.addActionListener(e -> spravovatHistorii());
        menuHistorie.add(zobrazitHistoriiItem);

        menuBar.add(menuMoznosti);
        menuBar.add(menuPravidla);
        menuBar.add(menuKoncepty);
        menuBar.add(menuHistorie);
        setJMenuBar(menuBar);

        seznamZamestnancu = new ArrayList<>();
        seznamNedostupnosti = new ArrayList<>();
        zakazanePary = new ArrayList<>();
        pozadavkyNaObsazeniSmen = new ArrayList<>();
        seznamNedostupnostiModel = new DefaultListModel<>();

        jmenoField = new JTextField(8);
        prijmeniField = new JTextField(8);
        uvazekField = new JTextField("1.0", 3);
        seznamZamestnancuModel = new DefaultListModel<>();
        
        seznamZamestnancuList = new JList<>(seznamZamestnancuModel);
        seznamZamestnancuList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Pracovnik) {
                    Pracovnik p = (Pracovnik) value;
                    setText(p.getJmeno() + " " + p.getPrijmeni() + " (ID: " + p.getId() + ", " + I18n.t("Úvazek") + ": " + p.getUvazek() + ")");
                }
                return this;
            }
        });

        LocalDate dnes = LocalDate.now();
        LocalDate zaMesic = dnes.plusMonths(1);
        datumOdField = new JTextField(dnes.toString(), 8);
        datumDoField = new JTextField(zaMesic.toString(), 8);

        javax.swing.event.DocumentListener dateListener = new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { aktualizujKapacitu(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { aktualizujKapacitu(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { aktualizujKapacitu(); }
        };
        datumOdField.getDocument().addDocumentListener(dateListener);
        datumDoField.getDocument().addDocumentListener(dateListener);

        seznamNedostupnostiList = new JList<>(seznamNedostupnostiModel);
        JScrollPane scrollNedostupnostiList = new JScrollPane(seznamNedostupnostiList);

        JPanel mainContent = new JPanel(new BorderLayout(5, 5));
        mainContent.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(mainContent);

        JPanel levyPanel = new JPanel(new BorderLayout(5, 5));
        JPanel pridejZamestnancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        pridejZamestnancePanel.setBorder(BorderFactory.createTitledBorder(I18n.t("Přidat zaměstnance")));
        pridejZamestnancePanel.add(new JLabel(I18n.t("Jméno:"))); pridejZamestnancePanel.add(jmenoField);
        pridejZamestnancePanel.add(new JLabel(I18n.t("Příjmení:"))); pridejZamestnancePanel.add(prijmeniField);
        pridejZamestnancePanel.add(new JLabel(I18n.t("Úvazek:"))); pridejZamestnancePanel.add(uvazekField);
        JButton pridejBtn = new JButton(I18n.t("Přidat"));
        pridejBtn.addActionListener(e -> pridejZamestnance());
        pridejZamestnancePanel.add(pridejBtn);

        JScrollPane scrollPridej = new JScrollPane(pridejZamestnancePanel);
        scrollPridej.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPridej.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPridej.setBorder(null);
        levyPanel.add(scrollPridej, BorderLayout.NORTH);

        JPanel seznamZamPanel = new JPanel(new BorderLayout());
        seznamZamPanel.setBorder(BorderFactory.createTitledBorder(I18n.t("Seznam zaměstnanců")));
        seznamZamPanel.add(new JScrollPane(seznamZamestnancuList), BorderLayout.CENTER);
        JButton smazZamBtn = new JButton(I18n.t("Smazat vybraného zaměstnance"));
        smazZamBtn.addActionListener(e -> smazVybranehoZamestnance());
        seznamZamPanel.add(smazZamBtn, BorderLayout.SOUTH);
        levyPanel.add(seznamZamPanel, BorderLayout.CENTER);

        JPanel pravyPanel = new JPanel(new BorderLayout(5, 5));
        JPanel datumPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        datumPanel.setBorder(BorderFactory.createTitledBorder(I18n.t("Nastavení plánování")));
        datumPanel.add(new JLabel(I18n.t("Od:"))); datumPanel.add(datumOdField);
        JButton calOd = new JButton("📅"); calOd.addActionListener(e -> otevrKalendar(datumOdField));
        datumPanel.add(calOd);
        datumPanel.add(new JLabel(I18n.t(" Do:"))); datumPanel.add(datumDoField);
        JButton calDo = new JButton("📅"); calDo.addActionListener(e -> otevrKalendar(datumDoField));
        datumPanel.add(calDo);

        JScrollPane scrollDatum = new JScrollPane(datumPanel);
        scrollDatum.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollDatum.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollDatum.setBorder(null);
        pravyPanel.add(scrollDatum, BorderLayout.NORTH);

        JPanel absencePanel = new JPanel(new BorderLayout());
        absencePanel.setBorder(BorderFactory.createTitledBorder(I18n.t("Nastavené absence")));
        absencePanel.add(scrollNedostupnostiList, BorderLayout.CENTER);
        JPanel absenceBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        JButton addAbs = new JButton(I18n.t("Přidat absenci")); addAbs.addActionListener(e -> spravovatNedostupnosti());
        JButton remAbs = new JButton(I18n.t("Smazat absence")); remAbs.addActionListener(e -> smazVybranouNedostupnost());
        absenceBtns.add(addAbs); absenceBtns.add(remAbs);
        absencePanel.add(absenceBtns, BorderLayout.SOUTH);
        pravyPanel.add(absencePanel, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, levyPanel, pravyPanel);
        splitPane.setResizeWeight(0.45);
        splitPane.setContinuousLayout(true);
        mainContent.add(splitPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        kapacitaLabel = new JLabel(I18n.t("📊 Počítám kapacitu..."));
        kapacitaLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JPanel kapaWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        kapaWrapper.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5,0,5,0), BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true)));
        kapaWrapper.setBackground(new Color(250, 250, 250));
        kapaWrapper.add(kapacitaLabel);
        bottomPanel.add(kapaWrapper);

        JPanel akcePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnGen = new JButton(I18n.t("📆 Vytvořit nový plán")); 
        btnGen.setFont(new Font("Arial", Font.BOLD, 18)); 
        btnGen.setBackground(new Color(200, 255, 200)); 
        btnGen.setPreferredSize(new Dimension(300, 45)); 
        btnGen.addActionListener(e -> generujPlan());
        akcePanel.add(btnGen);
        bottomPanel.add(akcePanel);

        mainContent.add(bottomPanel, BorderLayout.SOUTH);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { ulozData(); }
        });

        nactiDataPlne();
        if (typySmen.isEmpty()) {
            typySmen.add(new TypSmeny("Ranní", java.time.LocalTime.of(6, 0), java.time.LocalTime.of(14, 0)));
            typySmen.add(new TypSmeny("Odpolední", java.time.LocalTime.of(14, 0), java.time.LocalTime.of(22, 0)));
            typySmen.add(new TypSmeny("Noční", java.time.LocalTime.of(22, 0), java.time.LocalTime.of(6, 0)));
        }
        inicializujPozadavkyNaObsazeni();
        aktualizujVsechnaZobrazeni();
    }

    private void otevriNastaveni() {
        String staryJazyk = I18n.getLanguage().name();
        DialogNastaveni d = new DialogNastaveni(this, brigadnikNesmiBytSam, minimalniOdpocinekHodin, pouzivatZkratkySmen, staryJazyk);
        d.setVisible(true);
        if (d.isUlozeno()) {
            this.brigadnikNesmiBytSam = d.isBrigadnikNesmiBytSam();
            this.minimalniOdpocinekHodin = d.getMinimalniOdpocinek();
            this.pouzivatZkratkySmen = d.isPouzivatZkratky();
            if (!staryJazyk.equals(d.getVybranyJazyk())) {
                I18n.setLanguage(d.getVybranyJazyk().equals("EN") ? I18n.Language.EN : I18n.Language.CS);
                ulozData(); dispose(); new HlavniOkno().setVisible(true);
            } else { ulozData(); }
        }
    }

    private void aktualizujKapacitu() {
        try {
            LocalDate od = LocalDate.parse(datumOdField.getText().trim());
            LocalDate doo = LocalDate.parse(datumDoField.getText().trim());
            if (od.isAfter(doo)) return;
            long pocetDni = java.time.temporal.ChronoUnit.DAYS.between(od, doo) + 1;
            int denniPotreba = pozadavkyNaObsazeniSmen.stream().mapToInt(PozadavekNaObsazeniSmeny::getPocetZamestnancu).sum();
            int celkovaPotreba = (int) (pocetDni * denniPotreba);
            double celkovaKapacita = 0;
            for (Pracovnik p : seznamZamestnancu) {
                long dnyAbsence = 0;
                for (Nedostupnost n : seznamNedostupnosti) {
                    if (n.getPracovnikId() == p.getId()) {
                        LocalDate start = od.isAfter(n.getOdDatum()) ? od : n.getOdDatum();
                        LocalDate end = doo.isBefore(n.getDoDatum()) ? doo : n.getDoDatum();
                        if (!start.isAfter(end)) dnyAbsence += java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
                    }
                }
                long pracovniDny = pocetDni - dnyAbsence;
                if (pracovniDny > 0) celkovaKapacita += (pracovniDny * 5.0 / 7.0) * p.getUvazek();
            }
            int kapa = (int) Math.round(celkovaKapacita);
            int rozdil = kapa - celkovaPotreba;
            String text = I18n.getLanguage() == I18n.Language.EN ? 
                String.format("📊 Capacity: %d shifts | Required: %d", kapa, celkovaPotreba) :
                String.format("📊 Kapacita: %d směn | Požadováno: %d", kapa, celkovaPotreba);
            kapacitaLabel.setText(text + (rozdil < 0 ? " (⚠️ -" + Math.abs(rozdil) + ")" : " (✅ +" + rozdil + ")"));
            kapacitaLabel.setForeground(rozdil < 0 ? new Color(200, 0, 0) : new Color(0, 150, 0));
        } catch (Exception e) {}
    }

    private void pridejZamestnance() {
        try {
            String j = jmenoField.getText().trim(), p = prijmeniField.getText().trim();
            double u = Double.parseDouble(uvazekField.getText().trim().replace(",", "."));
            if (j.isEmpty() || p.isEmpty()) return;
            Pracovnik novy = new Pracovnik(nextPracovnikId.getAndIncrement(), j, p, u);
            seznamZamestnancu.add(novy); seznamZamestnancuModel.addElement(novy);
            jmenoField.setText(""); prijmeniField.setText(""); uvazekField.setText("1.0");
            ulozData(); aktualizujKapacitu();
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Chyba / Error"); }
    }

    private void smazVybranehoZamestnance() {
        int idx = seznamZamestnancuList.getSelectedIndex();
        if (idx != -1) {
            Pracovnik p = seznamZamestnancuModel.getElementAt(idx);
            seznamZamestnancu.remove(p); seznamZamestnancuModel.remove(idx);
            seznamNedostupnosti.removeIf(n -> n.getPracovnikId() == p.getId());
            zakazanePary.removeIf(pair -> pair[0] == p.getId() || pair[1] == p.getId());
            ulozData(); aktualizujVsechnaZobrazeni();
        }
    }

    private void spravovatNedostupnosti() { DialogPridejNedostupnost d = new DialogPridejNedostupnost(this, seznamZamestnancu); d.setVisible(true); if (d.isDataValid()) { seznamNedostupnosti.add(new Nedostupnost(d.getSelectedPracovnik().getId(), d.getDatumOd(), d.getDatumDo())); ulozData(); aktualizujSeznamNedostupnosti(); aktualizujKapacitu(); } }
    private void smazVybranouNedostupnost() { List<Nedostupnost> sel = seznamNedostupnostiList.getSelectedValuesList(); if (!sel.isEmpty()) { for (Nedostupnost n : sel) seznamNedostupnosti.remove(n); ulozData(); aktualizujSeznamNedostupnosti(); aktualizujKapacitu(); } }

    private void spravovatTypySmen() { 
        try {
            DialogSpravujTypySmen d = new DialogSpravujTypySmen(this, new ArrayList<>(typySmen)); d.setVisible(true); 
            if (d.isUlozeno()) { 
                typySmen.clear(); typySmen.addAll(d.getTypySmen()); 
                pozadavkyNaObsazeniSmen.removeIf(p -> typySmen.stream().noneMatch(t -> t.getNazev().equals(p.getTypSmeny().getNazev())));
                for (TypSmeny t : typySmen) if (pozadavkyNaObsazeniSmen.stream().noneMatch(p -> p.getTypSmeny().getNazev().equals(t.getNazev()))) pozadavkyNaObsazeniSmen.add(new PozadavekNaObsazeniSmeny(t, 0));
                ulozData(); aktualizujKapacitu(); 
            } 
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error");
        }
    }

    private void spravovatZakazanePary() { new DialogVyberZakazanyPar(this, seznamZamestnancu, zakazanePary).setVisible(true); ulozData(); }
    private void spravovatPozadavky() { DialogUpravPozadavkySmen d = new DialogUpravPozadavkySmen(this, new ArrayList<>(pozadavkyNaObsazeniSmen), typySmen); d.setVisible(true); if (d.isSaved()) { pozadavkyNaObsazeniSmen.clear(); pozadavkyNaObsazeniSmen.addAll(d.getUpdatedPozadavky()); ulozData(); aktualizujKapacitu(); } }

    private void spravovatKoncepty() {
        if (ulozeneKoncepty.isEmpty()) {
            return;
        }
        
        JDialog dialog = new JDialog(this, I18n.t("Zobrazit koncepty"), true);
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        DefaultListModel<String> model = new DefaultListModel<>();
        ulozeneKoncepty.keySet().stream().sorted().forEach(model::addElement);

        JList<String> list = new JList<>(model);
        list.setFont(new Font("Arial", Font.PLAIN, 16));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder(I18n.t("Rozpracované koncepty")));
        listPanel.add(new JScrollPane(list), BorderLayout.CENTER);
        dialog.add(listPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton btnOtevrit = new JButton("📂 " + I18n.t("Otevřít"));
        btnOtevrit.setBackground(new Color(173, 216, 230));
        btnOtevrit.addActionListener(e -> {
            String vybrany = list.getSelectedValue();
            if (vybrany != null) {
                Map<LocalDate, Map<Integer, TypSmeny>> k = ulozeneKoncepty.get(vybrany);
                LocalDate min = Collections.min(k.keySet()), max = Collections.max(k.keySet());
                Map<LocalDate, Map<Integer, TypSmeny>> m = new TreeMap<>(celkovyPlan); m.putAll(k);
                dialog.dispose();
                new DialogTabulkovyPlan(this, m, seznamZamestnancu, min, max, seznamNedostupnosti, typySmen, minimalniOdpocinekHodin, vybrany, pouzivatZkratkySmen).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(dialog, I18n.t("Nejprve vyberte položku ze seznamu."));
            }
        });

        JButton btnSmazat = new JButton("🗑️ " + I18n.t("Smazat"));
        btnSmazat.setBackground(new Color(255, 200, 200));
        btnSmazat.addActionListener(e -> {
            String vybrany = list.getSelectedValue();
            if (vybrany != null) {
                int volba = JOptionPane.showConfirmDialog(dialog, I18n.t("Opravdu smazat koncept: ") + vybrany + "?", I18n.t("Potvrzení"), JOptionPane.YES_NO_OPTION);
                if (volba == JOptionPane.YES_OPTION) {
                    ulozeneKoncepty.remove(vybrany);
                    ulozData();
                    model.removeElement(vybrany);
                    if (model.isEmpty()) dialog.dispose();
                }
            } else {
                JOptionPane.showMessageDialog(dialog, I18n.t("Nejprve vyberte položku ze seznamu."));
            }
        });

        btnPanel.add(btnOtevrit);
        btnPanel.add(btnSmazat);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }

    private void spravovatHistorii() {
        if (celkovyPlan.isEmpty()) {
            return;
        }
        
        JDialog dialog = new JDialog(this, I18n.t("Zobrazit historii"), true);
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        DefaultListModel<String> model = new DefaultListModel<>();
        Set<java.time.YearMonth> mSet = new TreeSet<>();
        for (LocalDate d : celkovyPlan.keySet()) mSet.add(java.time.YearMonth.from(d));
        for (java.time.YearMonth ym : mSet) model.addElement(String.format("%02d / %d", ym.getMonthValue(), ym.getYear()));

        JList<String> list = new JList<>(model);
        list.setFont(new Font("Arial", Font.PLAIN, 16));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder(I18n.t("Schválená historie (Měsíce)")));
        listPanel.add(new JScrollPane(list), BorderLayout.CENTER);
        dialog.add(listPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton btnOtevrit = new JButton("📅 " + I18n.t("Otevřít historii"));
        btnOtevrit.setBackground(new Color(220, 220, 255));
        btnOtevrit.addActionListener(e -> {
            String vybrany = list.getSelectedValue();
            if (vybrany != null) {
                String[] p = vybrany.split(" / ");
                java.time.YearMonth ym = java.time.YearMonth.of(Integer.parseInt(p[1]), Integer.parseInt(p[0]));
                dialog.dispose();
                new DialogZobrazPlan(this, celkovyPlan, seznamZamestnancu, ym.atDay(1), ym.atEndOfMonth(), pouzivatZkratkySmen).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(dialog, I18n.t("Nejprve vyberte položku ze seznamu."));
            }
        });

        JButton btnSmazat = new JButton("❌ " + I18n.t("Smazat měsíc"));
        btnSmazat.setBackground(new Color(255, 200, 200));
        btnSmazat.addActionListener(e -> {
            String vybrany = list.getSelectedValue();
            if (vybrany != null) {
                int volba = JOptionPane.showConfirmDialog(dialog, I18n.t("Opravdu trvale smazat historii pro měsíc ") + vybrany + "?", I18n.t("Potvrzení"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (volba == JOptionPane.YES_OPTION) {
                    String[] p = vybrany.split(" / ");
                    java.time.YearMonth ym = java.time.YearMonth.of(Integer.parseInt(p[1]), Integer.parseInt(p[0]));
                    celkovyPlan.entrySet().removeIf(entry -> java.time.YearMonth.from(entry.getKey()).equals(ym));
                    ulozData();
                    model.removeElement(vybrany);
                    if (model.isEmpty()) dialog.dispose();
                }
            } else {
                JOptionPane.showMessageDialog(dialog, I18n.t("Nejprve vyberte položku ze seznamu."));
            }
        });

        btnPanel.add(btnOtevrit);
        btnPanel.add(btnSmazat);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }

    private void generujPlan() {
        try {
            LocalDate od = LocalDate.parse(datumOdField.getText().trim());
            LocalDate doo = LocalDate.parse(datumDoField.getText().trim());
            List<Pravidlo> r = new ArrayList<>(); r.add(new DveNocniPoSobePravidlo()); r.add(new cz.planovac.pravidla.MaximalniTydenniUvazekPravidlo());
            Map<LocalDate, Map<Integer, TypSmeny>> n = Planovac.naplanuj(seznamZamestnancu, seznamNedostupnosti, od, doo, r, typySmen, zakazanePary, pozadavkyNaObsazeniSmen, celkovyPlan, brigadnikNesmiBytSam);
            new DialogTabulkovyPlan(this, n, seznamZamestnancu, od, doo, seznamNedostupnosti, typySmen, minimalniOdpocinekHodin, null, pouzivatZkratkySmen).setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean existujeKoncept(String nazev) {
        return ulozeneKoncepty.containsKey(nazev);
    }

    public void ulozKoncept(String nazev, Map<LocalDate, Map<Integer, TypSmeny>> planKonceptu) {
        ulozeneKoncepty.put(nazev, new TreeMap<>(planKonceptu));
        ulozData();
    }

    public void ulozDoHlavnihoPlanu(Map<LocalDate, Map<Integer, TypSmeny>> schvalenyPlan) {
        for (Map.Entry<LocalDate, Map<Integer, TypSmeny>> e : schvalenyPlan.entrySet()) {
            celkovyPlan.put(e.getKey(), new HashMap<>(e.getValue()));
        }
        ulozData();
    }

    private void otevrKalendar(JTextField f) { 
        DialogVyberData d = new DialogVyberData(this, null); d.setVisible(true); 
        if (d.getVybraneDatum() != null) f.setText(d.getVybraneDatum().toString()); 
    }

    private void inicializujPozadavkyNaObsazeni() {
        if (pozadavkyNaObsazeniSmen.isEmpty()) {
            for (TypSmeny t : typySmen) pozadavkyNaObsazeniSmen.add(new PozadavekNaObsazeniSmeny(t, 0));
        }
    }

    // --- PERSISTENCE ---
    private static class UlozenaData {
        List<Pracovnik> seznamZamestnancu; List<Nedostupnost> seznamNedostupnosti; List<int[]> zakazanePary;
        List<PozadavekNaObsazeniSmeny> pozadavkyNaObsazeniSmen; List<TypSmeny> typySmen;
        int minimalniOdpocinekHodin; int dalsiIdPracovnika;
        Map<LocalDate, Map<Integer, TypSmeny>> celkovyPlan; Map<String, Map<LocalDate, Map<Integer, TypSmeny>>> ulozeneKoncepty;
        Boolean brigadnikNesmiBytSam; Boolean pouzivatZkratkySmen; String jazykAplikace;
    }

    private Gson vytvorGson() { return new GsonBuilder().registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (s, t, c) -> new JsonPrimitive(s.toString())).registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (j, t, c) -> LocalDate.parse(j.getAsString())).registerTypeAdapter(java.time.LocalTime.class, (JsonSerializer<java.time.LocalTime>) (s, t, c) -> new JsonPrimitive(s.toString())).registerTypeAdapter(java.time.LocalTime.class, (JsonDeserializer<java.time.LocalTime>) (j, t, c) -> java.time.LocalTime.parse(j.getAsString())).setPrettyPrinting().create(); }

    private void ulozData() {
        UlozenaData d = new UlozenaData(); d.seznamZamestnancu = seznamZamestnancu; d.seznamNedostupnosti = seznamNedostupnosti; d.zakazanePary = zakazanePary; d.pozadavkyNaObsazeniSmen = pozadavkyNaObsazeniSmen; d.typySmen = typySmen; d.minimalniOdpocinekHodin = minimalniOdpocinekHodin; d.dalsiIdPracovnika = nextPracovnikId.get(); d.celkovyPlan = celkovyPlan; d.ulozeneKoncepty = ulozeneKoncepty; d.brigadnikNesmiBytSam = brigadnikNesmiBytSam; d.pouzivatZkratkySmen = pouzivatZkratkySmen; d.jazykAplikace = I18n.getLanguage().name();
        try (FileWriter fw = new FileWriter(DATA_FILE)) { vytvorGson().toJson(d, fw); } catch (Exception e) {}
    }

    private void nactiDataRychleProJazyk() { try (FileReader fr = new FileReader(DATA_FILE)) { UlozenaData d = vytvorGson().fromJson(fr, UlozenaData.class); if (d != null && d.jazykAplikace != null && d.jazykAplikace.equals("EN")) I18n.setLanguage(I18n.Language.EN); } catch (Exception e) {} }

    private void nactiDataPlne() {
        try (FileReader fr = new FileReader(DATA_FILE)) {
            UlozenaData d = vytvorGson().fromJson(fr, UlozenaData.class);
            if (d != null) {
                if (d.seznamZamestnancu != null) seznamZamestnancu.addAll(d.seznamZamestnancu);
                if (d.seznamNedostupnosti != null) seznamNedostupnosti.addAll(d.seznamNedostupnosti);
                if (d.zakazanePary != null) zakazanePary.addAll(d.zakazanePary);
                if (d.pozadavkyNaObsazeniSmen != null) pozadavkyNaObsazeniSmen.addAll(d.pozadavkyNaObsazeniSmen);
                if (d.typySmen != null && !d.typySmen.isEmpty()) { typySmen.clear(); typySmen.addAll(d.typySmen); }
                minimalniOdpocinekHodin = d.minimalniOdpocinekHodin > 0 ? d.minimalniOdpocinekHodin : 11;
                if (d.dalsiIdPracovnika > 0) nextPracovnikId.set(d.dalsiIdPracovnika);
                if (d.celkovyPlan != null) celkovyPlan = d.celkovyPlan;
                if (d.ulozeneKoncepty != null) ulozeneKoncepty = d.ulozeneKoncepty;
                if (d.brigadnikNesmiBytSam != null) brigadnikNesmiBytSam = d.brigadnikNesmiBytSam;
                if (d.pouzivatZkratkySmen != null) pouzivatZkratkySmen = d.pouzivatZkratkySmen;
            }
        } catch (Exception e) {}
    }

    private void aktualizujVsechnaZobrazeni() { 
        aktualizujSeznamZamestnancu(); 
        aktualizujSeznamNedostupnosti(); 
        aktualizujKapacitu(); 
    }
    
    private void aktualizujSeznamZamestnancu() { seznamZamestnancuModel.clear(); for (Pracovnik p : seznamZamestnancu) seznamZamestnancuModel.addElement(p); }
    
    private void aktualizujSeznamNedostupnosti() { 
        seznamNedostupnostiModel.clear(); 
        seznamNedostupnosti.sort(Comparator.comparing(n -> seznamZamestnancu.stream().filter(p -> p.getId() == n.getPracovnikId()).findFirst().map(Pracovnik::getJmeno).orElse("")));
        for (Nedostupnost n : seznamNedostupnosti) {
            String jm = seznamZamestnancu.stream().filter(p -> p.getId() == n.getPracovnikId()).findFirst().map(p -> p.getJmeno() + " " + p.getPrijmeni()).orElse("?");
            seznamNedostupnostiModel.addElement(new Nedostupnost(n.getPracovnikId(), n.getOdDatum(), n.getDoDatum()) { @Override public String toString() { return jm + " (" + getOdDatum() + " - " + getDoDatum() + ")"; } });
        }
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> { try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {} new HlavniOkno().setVisible(true); }); }
}