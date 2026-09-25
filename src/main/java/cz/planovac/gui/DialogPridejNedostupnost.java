package cz.planovac.gui;

import cz.planovac.model.Pracovnik;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.*;

public class DialogPridejNedostupnost extends JDialog {
    private JComboBox<Pracovnik> pracovnikComboBox;
    private JTextField datumOdField;
    private JTextField datumDoField;
    private boolean dataValid = false;

    private Pracovnik selectedPracovnik;
    private LocalDate datumOd;
    private LocalDate datumDo;

    public DialogPridejNedostupnost(Frame owner, List<Pracovnik> pracovnici) {
        super(owner, "Přidat nedostupnost", true);
        setLayout(new BorderLayout(10, 10));
        setSize(450, 250); // Mírně zvětšeno pro kalendářová tlačítka
        setLocationRelativeTo(owner);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Pracovník
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Zaměstnanec:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        pracovnikComboBox = new JComboBox<>(pracovnici.toArray(Pracovnik[]::new));
        inputPanel.add(pracovnikComboBox, gbc);

        // Datum Od (s kalendářem)
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Datum od:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        
        JPanel odPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        datumOdField = new JTextField(10);
        JButton kalendarOdButton = new JButton("📅");
        kalendarOdButton.addActionListener(e -> otevrKalendar(datumOdField));
        odPanel.add(datumOdField);
        odPanel.add(kalendarOdButton);
        inputPanel.add(odPanel, gbc);

        // Datum Do (s kalendářem)
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Datum do:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        
        JPanel doPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        datumDoField = new JTextField(10);
        JButton kalendarDoButton = new JButton("📅");
        kalendarDoButton.addActionListener(e -> otevrKalendar(datumDoField));
        doPanel.add(datumDoField);
        doPanel.add(kalendarDoButton);
        inputPanel.add(doPanel, gbc);

        add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton pridejButton = new JButton("Přidat");
        pridejButton.addActionListener(e -> {
            try {
                selectedPracovnik = (Pracovnik) pracovnikComboBox.getSelectedItem();
                datumOd = LocalDate.parse(datumOdField.getText().trim());
                datumDo = LocalDate.parse(datumDoField.getText().trim());

                if (selectedPracovnik == null) {
                    JOptionPane.showMessageDialog(this, "Vyberte zaměstnance.", "Chyba", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (datumOd.isAfter(datumDo)) {
                    JOptionPane.showMessageDialog(this, "Datum 'Od' nemůže být po datu 'Do'.", "Chyba data", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                dataValid = true;
                dispose();
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Zadejte platné datum (použijte kalendář).", "Chybný formát data", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(pridejButton);

        JButton zrusitButton = new JButton("Zrušit");
        zrusitButton.addActionListener(e -> {
            dataValid = false;
            dispose();
        });
        buttonPanel.add(zrusitButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Pomocná metoda pro otevření kalendáře a zápis do textového pole
    private void otevrKalendar(JTextField cilovePole) {
        LocalDate vychozi = null;
        try {
            if (!cilovePole.getText().trim().isEmpty()) {
                vychozi = LocalDate.parse(cilovePole.getText().trim());
            }
        } catch (DateTimeParseException ignored) { } 

        DialogVyberData dialog = new DialogVyberData(this, vychozi);
        dialog.setVisible(true);
        
        LocalDate vybrane = dialog.getVybraneDatum();
        if (vybrane != null) {
            cilovePole.setText(vybrane.toString());
        }
    }

    public boolean isDataValid() {
        return dataValid;
    }

    public Pracovnik getSelectedPracovnik() {
        return selectedPracovnik;
    }

    public LocalDate getDatumOd() {
        return datumOd;
    }

    public LocalDate getDatumDo() {
        return datumDo;
    }
}