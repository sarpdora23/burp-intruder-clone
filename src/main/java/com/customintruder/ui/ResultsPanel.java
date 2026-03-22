package com.customintruder.ui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;
import com.customintruder.engine.AttackEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ResultsPanel extends JPanel {
    private final MontoyaApi api;
    private final VirtualTableModel tableModel;
    private final JTable resultsTable;
    
    private final HttpRequestEditor reqViewer;
    private final HttpResponseEditor resViewer;
    
    private AttackEngine currentEngine;

    public ResultsPanel(MontoyaApi api) {
        this.api = api;
        this.setLayout(new BorderLayout());
        
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton stopBtn = new JButton("Stop Attack");
        stopBtn.setBackground(new Color(220, 53, 69)); // Bootstrap Red
        stopBtn.setForeground(Color.WHITE);
        stopBtn.addActionListener(e -> {
            if (currentEngine != null) {
                currentEngine.stopAttack();
            }
        });
        topBar.add(stopBtn);
        this.add(topBar, BorderLayout.NORTH);
        
        tableModel = new VirtualTableModel();
        resultsTable = new JTable(tableModel);
        
        // Optimize JTable for speed to prevent UI freezing
        resultsTable.setUpdateSelectionOnSort(false);
        resultsTable.setDoubleBuffered(true);
        resultsTable.setFillsViewportHeight(true);
        resultsTable.setAutoCreateRowSorter(true); // Allow Sorting
        
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        
        // Viewer Panels
        reqViewer = api.userInterface().createHttpRequestEditor();
        resViewer = api.userInterface().createHttpResponseEditor();
        
        JTabbedPane reqResTabs = new JTabbedPane();
        reqResTabs.addTab("Request", reqViewer.uiComponent());
        reqResTabs.addTab("Response", resViewer.uiComponent());
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, reqResTabs);
        splitPane.setResizeWeight(0.5);
        this.add(splitPane, BorderLayout.CENTER);
        
        // Handle clicks on table
        resultsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = resultsTable.getSelectedRow();
                if (row >= 0) {
                    int modelRow = resultsTable.convertRowIndexToModel(row);
                    VirtualTableModel.ResultRow resultData = tableModel.getRow(modelRow);
                    if (resultData != null) {
                        displayDetails(resultData);
                    }
                }
            }
        });
    }
    
    private void displayDetails(VirtualTableModel.ResultRow row) {
        if (row.request != null) {
            reqViewer.setRequest(row.request);
        }
        if (row.response != null) {
            resViewer.setResponse(row.response);
        }
    }
    
    public void addResult(int id, String payload, short statusCode, int length, long time,
                          burp.api.montoya.http.message.requests.HttpRequest request,
                          burp.api.montoya.http.message.responses.HttpResponse response) {
        tableModel.addRow(new VirtualTableModel.ResultRow(id, payload, statusCode, length, time, request, response));
    }
    
    public void clearResults() {
        tableModel.clear();
    }
    
    public void setEngine(AttackEngine engine) {
        this.currentEngine = engine;
    }
}
