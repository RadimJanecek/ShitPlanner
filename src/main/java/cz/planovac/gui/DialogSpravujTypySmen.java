package cz.planovac.gui;

import cz.planovac.model.TypSmeny;
import cz.planovac.utils.I18n;
import java.awt.*;
import java.time.LocalTime;
import java.util.List;
import javax.swing.*;

public class DialogSpravujTypySmen extends JDialog {
    private final List<TypSmeny> typySmen;
    private boolean ulozeno = false;

    public DialogSpravujTypySmen(Frame owner, List<TypSmeny> typySmen) {
        super(owner, I18n.t("Spravovat typy směn"), true);
        this.typySmen = typySmen;
        setSize(400, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel zadavaciPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        zadavaciPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTextField nazevField = new JTextField();
        JTextField zacatekField = new JTextField("06:00");
        JTextField konecField = new JTextField("14:00");

        zadavaciPanel.add(new JLabel(I18n.t("Název (např. Ranní):"))); zadavaciPanel.add(nazevField);
        zadavaciPanel.add(new JLabel(I18n.t("Začátek (HH:MM):"))); zadavaciPanel.add(zacatekField);
        zadavaciPanel.add(new JLabel(I18n.t("Konec (HH:MM):"))); zadavaciPanel.add(konecField);

        DefaultListModel<TypSmeny> model = new DefaultListModel<>();
        for (TypSmeny t : typySmen) model.addElement(t);
        JList<TypSmeny> list = new JList<>(model);
        
        // ZDE JE OPRAVA PRO ANGLIČTINU
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TypSmeny) {
                    TypSmeny t = (TypSmeny) value;
                    setText(I18n.t(t.getNazev()) + " (" + t.getZacatek() + " - " + t.getKonec() + ")");
                }
                return this;
            }
        });

        JButton pridejBtn = new JButton(I18n.t("Přidat"));
        pridejBtn.addActionListener(e -> {
            try {
                String n = nazevField.getText().trim();
                LocalTime z = LocalTime.parse(zacatekField.getText().trim());
                LocalTime k = LocalTime.parse(konecField.getText().trim());
                if (!n.isEmpty()) {
                    TypSmeny ts = new TypSmeny(n, z, k);
                    model.addElement(ts);
                    nazevField.setText("");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Format: HH:MM");
            }
        });

        JPanel top = new JPanel(new BorderLayout());
        top.add(zadavaciPanel, BorderLayout.CENTER);
        top.add(pridejBtn, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createTitledBorder(I18n.t("Typy směn")));
        center.add(new JScrollPane(list), BorderLayout.CENTER);
        
        JButton smazBtn = new JButton(I18n.t("Smazat směnu"));
        smazBtn.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx != -1) model.remove(idx);
        });
        center.add(smazBtn, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        JPanel spodni = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ulozitBtn = new JButton(I18n.t("Uložit nastavení"));
        ulozitBtn.addActionListener(e -> {
            this.typySmen.clear();
            for (int i = 0; i < model.getSize(); i++) this.typySmen.add(model.getElementAt(i));
            this.ulozeno = true;
            dispose();
        });
        JButton zrusitBtn = new JButton(I18n.t("Zrušit"));
        zrusitBtn.addActionListener(e -> dispose());
        spodni.add(ulozitBtn);
        spodni.add(zrusitBtn);
        add(spodni, BorderLayout.SOUTH);
    }

    public boolean isUlozeno() { return ulozeno; }
    public List<TypSmeny> getTypySmen() { return typySmen; }
}