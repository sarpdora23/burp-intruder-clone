package com.customintruder.ui;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * A lightweight model that survives millions of rows without freezing Burp.
 */
public class VirtualTableModel extends AbstractTableModel {
    private final String[] columnNames = {"Req #", "Payload", "Status", "Length", "Time (ms)"};
    
    // We only keep lightweight primitive-like row records in memory to prevent GC lag
    private final List<ResultRow> rows = Collections.synchronizedList(new ArrayList<>());
    
    public static class ResultRow {
        public final int id;
        public final String payload;
        public final short statusCode;
        public final int length;
        public final long time;
        public final burp.api.montoya.http.message.requests.HttpRequest request;
        public final burp.api.montoya.http.message.responses.HttpResponse response;
        
        public ResultRow(int id, String payload, short statusCode, int length, long time,
                         burp.api.montoya.http.message.requests.HttpRequest request,
                         burp.api.montoya.http.message.responses.HttpResponse response) {
            this.id = id;
            this.payload = payload;
            this.statusCode = statusCode;
            this.length = length;
            this.time = time;
            this.request = request;
            this.response = response;
        }
    }

    public void addRow(ResultRow row) {
        rows.add(row);
        int rowIndex = rows.size() - 1;
        // Fire ONLY inserted event to avoid full UI redraw freeze
        fireTableRowsInserted(rowIndex, rowIndex);
    }
    
    public void clear() {
        int size = rows.size();
        if (size > 0) {
            rows.clear();
            fireTableRowsDeleted(0, size - 1);
        }
    }
    
    public ResultRow getRow(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < rows.size()) {
            return rows.get(rowIndex);
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }
    
    @Override
    public Class<?> getColumnClass(int col) {
        if (col == 0) return Integer.class;
        if (col == 2) return Short.class;
        if (col == 3) return Integer.class;
        if (col == 4) return Long.class;
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= rows.size()) return null;
        ResultRow row = rows.get(rowIndex);
        switch (columnIndex) {
            case 0: return row.id;
            case 1: return row.payload;
            case 2: return row.statusCode;
            case 3: return row.length;
            case 4: return row.time;
            default: return null;
        }
    }
}
