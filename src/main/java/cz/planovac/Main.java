package cz.planovac;

import cz.planovac.gui.HlavniOkno;
import javax.swing.*;

public class Main{
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HlavniOkno okno = new HlavniOkno();
            okno.setVisible(true);
        });
    }
}