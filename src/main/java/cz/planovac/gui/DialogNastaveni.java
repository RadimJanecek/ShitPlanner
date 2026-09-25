package cz.planovac.gui;

import cz.planovac.utils.I18n;
import java.awt.*;
import javax.swing.*;

public class DialogNastaveni extends JDialog {
    private boolean brigadnikNesmiBytSam;
    private int minimalniOdpocinek;
    private boolean pouzivatZkratky;
    private String vybranyJazyk;
    private boolean ulozeno = false;

    public DialogNastaveni(Frame owner, boolean aktualniBrigadnik, int aktualniOdpocinek, boolean aktualniZkratky, String aktualniJazyk) {
        super(owner, I18n.t("Nastavení aplikace"), true);
        setSize(400, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JCheckBox brigadnikCheck = new JCheckBox(I18n.t("Brigádník (úvazek < 1.0) nesmí být na směně sám"), aktualniBrigadnik);
        JCheckBox zkratkyCheck = new JCheckBox(I18n.t("Zobrazovat pouze zkratky směn (první písmeno, ...)"), aktualniZkratky);
        
        JPanel odpocinekPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        odpocinekPanel.add(new JLabel(I18n.t("Minimální odpočinek mezi směnami (hodiny):")));
        JSpinner odpocinekSpinner = new JSpinner(new SpinnerNumberModel(aktualniOdpocinek, 8, 24, 1));
        odpocinekPanel.add(odpocinekSpinner);

        JPanel jazykPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        jazykPanel.add(new JLabel(I18n.t("Jazyk / Language:")));
        JComboBox<String> jazykBox = new JComboBox<>(new String[]{"Čeština (CS)", "English (EN)"});
        jazykBox.setSelectedIndex(aktualniJazyk.equals("EN") ? 1 : 0);
        jazykPanel.add(jazykBox);

        panel.add(brigadnikCheck);
        panel.add(zkratkyCheck);
        panel.add(odpocinekPanel);
        panel.add(jazykPanel);

        add(panel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnUlozit = new JButton(I18n.t("Uložit nastavení"));
        btnUlozit.addActionListener(e -> {
            brigadnikNesmiBytSam = brigadnikCheck.isSelected();
            pouzivatZkratky = zkratkyCheck.isSelected();
            minimalniOdpocinek = (int) odpocinekSpinner.getValue();
            vybranyJazyk = jazykBox.getSelectedIndex() == 1 ? "EN" : "CS";
            ulozeno = true;
            dispose();
        });
        JButton btnZrusit = new JButton(I18n.t("Zrušit"));
        btnZrusit.addActionListener(e -> dispose());
        
        btnPanel.add(btnUlozit);
        btnPanel.add(btnZrusit);
        add(btnPanel, BorderLayout.SOUTH);
    }
    
    public boolean isUlozeno() { return ulozeno; }
    public boolean isBrigadnikNesmiBytSam() { return brigadnikNesmiBytSam; }
    public int getMinimalniOdpocinek() { return minimalniOdpocinek; }
    public boolean isPouzivatZkratky() { return pouzivatZkratky; }
    public String getVybranyJazyk() { return vybranyJazyk; }
}