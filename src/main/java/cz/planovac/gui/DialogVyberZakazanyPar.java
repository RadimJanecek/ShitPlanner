package cz.planovac.gui;

import cz.planovac.model.Pracovnik;
import cz.planovac.utils.I18n;
import java.awt.*;
import java.util.List;
import javax.swing.*;

public class DialogVyberZakazanyPar extends JDialog {
    public DialogVyberZakazanyPar(Frame owner, List<Pracovnik> pracovnici, List<int[]> zakazanePary) {
        super(owner, I18n.t("Spravovat zakázané páry"), true);
        setSize(450, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        topPanel.setBorder(BorderFactory.createTitledBorder(I18n.t("Přidat nový zakázaný pár")));
        
        JComboBox<Pracovnik> cb1 = new JComboBox<>(new java.util.Vector<>(pracovnici));
        JComboBox<Pracovnik> cb2 = new JComboBox<>(new java.util.Vector<>(pracovnici));
        
        // --- OPRAVA: Vynucený překlad slova "Úvazek" pro rozevírací seznam ---
        DefaultListCellRenderer renderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Pracovnik) {
                    Pracovnik p = (Pracovnik) value;
                    setText(p.getJmeno() + " " + p.getPrijmeni() + " (ID: " + p.getId() + ", " + I18n.t("Úvazek") + ": " + p.getUvazek() + ")");
                }
                return this;
            }
        };
        cb1.setRenderer(renderer);
        cb2.setRenderer(renderer);
        // ----------------------------------------------------------------------

        topPanel.add(new JLabel(I18n.t("Zaměstnanec 1:"), SwingConstants.RIGHT)); topPanel.add(cb1);
        topPanel.add(new JLabel(I18n.t("Zaměstnanec 2:"), SwingConstants.RIGHT)); topPanel.add(cb2);
        
        JButton addBtn = new JButton(I18n.t("Přidat pár"));
        topPanel.add(new JLabel("")); topPanel.add(addBtn);

        add(topPanel, BorderLayout.NORTH);

        DefaultListModel<String> model = new DefaultListModel<>();
        Runnable refresh = () -> {
            model.clear();
            for (int[] p : zakazanePary) {
                String j1 = pracovnici.stream().filter(x -> x.getId() == p[0]).findFirst().map(x -> x.getJmeno() + " " + x.getPrijmeni()).orElse("?");
                String j2 = pracovnici.stream().filter(x -> x.getId() == p[1]).findFirst().map(x -> x.getJmeno() + " " + x.getPrijmeni()).orElse("?");
                model.addElement(j1 + I18n.t(" a ") + j2);
            }
        };
        refresh.run();

        JList<String> list = new JList<>(model);
        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createTitledBorder(I18n.t("Existující zakázané páry")));
        center.add(new JScrollPane(list), BorderLayout.CENTER);
        
        JButton delBtn = new JButton(I18n.t("Smazat vybraný pár"));
        delBtn.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx != -1) {
                zakazanePary.remove(idx);
                refresh.run();
            }
        });
        center.add(delBtn, BorderLayout.SOUTH);
        
        add(center, BorderLayout.CENTER);

        addBtn.addActionListener(e -> {
            Pracovnik p1 = (Pracovnik) cb1.getSelectedItem();
            Pracovnik p2 = (Pracovnik) cb2.getSelectedItem();
            if (p1 != null && p2 != null && p1.getId() != p2.getId()) {
                zakazanePary.add(new int[]{p1.getId(), p2.getId()});
                refresh.run();
            }
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton(I18n.t("Zavřít"));
        closeBtn.addActionListener(e -> dispose());
        bottom.add(closeBtn);
        add(bottom, BorderLayout.SOUTH);
    }
}