package cz.planovac.gui;

import cz.planovac.model.PozadavekNaObsazeniSmeny;
import cz.planovac.model.TypSmeny;
import cz.planovac.utils.I18n;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class DialogUpravPozadavkySmen extends JDialog {
    private final List<PozadavekNaObsazeniSmeny> updatedPozadavky = new ArrayList<>();
    private boolean saved = false;

    public DialogUpravPozadavkySmen(Frame owner, List<PozadavekNaObsazeniSmeny> current, List<TypSmeny> typySmen) {
        super(owner, I18n.t("Upravit požadavky na obsazení směn"), true);
        setSize(450, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        String[] cols = {I18n.t("Typ směny"), I18n.t("Počet zaměstnanců")};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == 1; }
        };
        
        for (TypSmeny ts : typySmen) {
            int pocet = current.stream().filter(p -> p.getTypSmeny().getNazev().equals(ts.getNazev())).findFirst().map(PozadavekNaObsazeniSmeny::getPocetZamestnancu).orElse(0);
            model.addRow(new Object[]{I18n.t(ts.getNazev()), pocet}); // Překlad tabulky
        }

        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton(I18n.t("Uložit"));
        saveBtn.addActionListener(e -> {
            if (table.isEditing()) table.getCellEditor().stopCellEditing();
            for (int i = 0; i < model.getRowCount(); i++) {
                String nazev = (String) model.getValueAt(i, 0); // Tohle je teď anglicky ("Morning")
                int pocet = 0;
                try { pocet = Integer.parseInt(model.getValueAt(i, 1).toString()); } catch (Exception ex) {}
                
                // Musíme to filtrovat přes překlad zpět, abychom našli tu správnou českou směnu "Ranní" v databázi
                TypSmeny ts = typySmen.stream().filter(t -> I18n.t(t.getNazev()).equals(nazev)).findFirst().orElse(null);
                if (ts != null) updatedPozadavky.add(new PozadavekNaObsazeniSmeny(ts, pocet));
            }
            saved = true;
            dispose();
        });
        JButton cancelBtn = new JButton(I18n.t("Zrušit"));
        cancelBtn.addActionListener(e -> dispose());
        
        bottom.add(saveBtn); bottom.add(cancelBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    public boolean isSaved() { return saved; }
    public List<PozadavekNaObsazeniSmeny> getUpdatedPozadavky() { return updatedPozadavky; }
}