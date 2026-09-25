package cz.planovac.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

public class HeaderRenderer extends JTextArea implements TableCellRenderer {

    public HeaderRenderer(JTableHeader header) {
        setWrapStyleWord(true);
        setLineWrap(true);
        setOpaque(true);
        setBorder(UIManager.getBorder("TableHeader.cellBorder"));
        setAlignmentX(Component.CENTER_ALIGNMENT);
        setFont(header.getFont());
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        setText((value == null) ? "" : value.toString());
        return this;
    }
}