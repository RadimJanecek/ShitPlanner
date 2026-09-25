package cz.planovac.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;


public class TableColumnAdjuster {
    private final JTable table;
    private final int spacing = 5;

    public TableColumnAdjuster(JTable table) {
        this.table = table;
    }

    public void adjustColumns() {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < table.getColumnCount(); i++) {
            adjustColumn(i);
        }
    }

    private void adjustColumn(int column) {
        TableColumn tableColumn = table.getColumnModel().getColumn(column);
        int preferredWidth = tableColumn.getMinWidth();
        int maxWidth = tableColumn.getMaxWidth();

        for (int row = 0; row < table.getRowCount(); row++) {
            TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
            Component c = table.prepareRenderer(cellRenderer, row, column);
            preferredWidth = Math.max(preferredWidth, c.getPreferredSize().width + spacing);
        }


        TableCellRenderer headerRenderer = tableColumn.getHeaderRenderer();
        if (headerRenderer == null) {
            headerRenderer = table.getTableHeader().getDefaultRenderer();
        }
        Component headerComp = headerRenderer.getTableCellRendererComponent(
                table, tableColumn.getHeaderValue(), false, false, -1, column);
        preferredWidth = Math.max(preferredWidth, headerComp.getPreferredSize().width + spacing);


        if (preferredWidth >= maxWidth) {
            preferredWidth = maxWidth;
        }

        tableColumn.setPreferredWidth(preferredWidth);
    }
}