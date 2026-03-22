package com.customintruder.ui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.Selection;
import burp.api.montoya.core.ByteArray;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class TargetPositionsPanel extends JPanel {
    private final MontoyaApi api;
    private final HttpRequestEditor requestEditor;
    
    private final JTextField hostField;
    private final JTextField portField;
    private final JCheckBox httpsCheckBox;

    public TargetPositionsPanel(MontoyaApi api) {
        this.api = api;
        this.setLayout(new BorderLayout());
        
        // Target Config Panel
        JPanel targetConfigPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        targetConfigPanel.add(new JLabel("Host:"));
        hostField = new JTextField(20);
        targetConfigPanel.add(hostField);
        
        targetConfigPanel.add(new JLabel("Port:"));
        portField = new JTextField(5);
        targetConfigPanel.add(portField);
        
        httpsCheckBox = new JCheckBox("Use HTTPS");
        targetConfigPanel.add(httpsCheckBox);
        
        this.add(targetConfigPanel, BorderLayout.NORTH);
        
        // Request Editor
        requestEditor = api.userInterface().createHttpRequestEditor();
        this.add(requestEditor.uiComponent(), BorderLayout.CENTER);
        
        // Controls Panel (Add §, Clear § etc.)
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addMarkerBtn = new JButton("Add §");
        JButton clearMarkersBtn = new JButton("Clear §");
        JButton autoMarkersBtn = new JButton("Auto §");
        
        // Dummy logic for markers for now
        addMarkerBtn.addActionListener(e -> addMarker());
        clearMarkersBtn.addActionListener(e -> clearMarkers());
        autoMarkersBtn.addActionListener(e -> autoMarkers());
        
        controlsPanel.add(addMarkerBtn);
        controlsPanel.add(clearMarkersBtn);
        controlsPanel.add(autoMarkersBtn);
        
        this.add(controlsPanel, BorderLayout.SOUTH);
    }
    
    public void loadRequest(HttpRequest request) {
        requestEditor.setRequest(request);
        if (request.httpService() != null) {
            hostField.setText(request.httpService().host());
            portField.setText(String.valueOf(request.httpService().port()));
            httpsCheckBox.setSelected(request.httpService().secure());
        }
    }
    
    private void addMarker() {
        if (requestEditor.getRequest() == null) return;
        
        Optional<Selection> selectionOpt = requestEditor.selection();
        if (selectionOpt.isPresent() && selectionOpt.get().offsets().startIndexInclusive() < selectionOpt.get().offsets().endIndexExclusive()) {
            Selection selection = selectionOpt.get();
            int start = selection.offsets().startIndexInclusive();
            int end = selection.offsets().endIndexExclusive();
            
            byte[] currentData = requestEditor.getRequest().toByteArray().getBytes();
            String dataStr = new String(currentData, java.nio.charset.StandardCharsets.ISO_8859_1);
            
            String newDataStr = dataStr.substring(0, start) + "§" + dataStr.substring(start, end) + "§" + dataStr.substring(end);
            
            requestEditor.setRequest(burp.api.montoya.http.message.requests.HttpRequest.httpRequest(
                requestEditor.getRequest().httpService(), 
                burp.api.montoya.core.ByteArray.byteArray(newDataStr.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1))
            ));
        }
    }
    
    private void clearMarkers() {
        if (requestEditor.getRequest() == null) return;
        
        byte[] currentData = requestEditor.getRequest().toByteArray().getBytes();
        String dataStr = new String(currentData, java.nio.charset.StandardCharsets.ISO_8859_1);
        String newDataStr = dataStr.replace("§", "");
        
        requestEditor.setRequest(burp.api.montoya.http.message.requests.HttpRequest.httpRequest(
            requestEditor.getRequest().httpService(), 
            burp.api.montoya.core.ByteArray.byteArray(newDataStr.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1))
        ));
    }
    
    private void autoMarkers() {
        // MVP: Simple placeholder. Proper auto-markers require full HTTP parsing.
    }
    
    public burp.api.montoya.http.message.requests.HttpRequest getBaseRequest() {
        return requestEditor.getRequest();
    }
    
    public burp.api.montoya.http.HttpService getHttpService() {
        return requestEditor.getRequest() != null ? requestEditor.getRequest().httpService() : null;
    }
    
    public int getMarkerCount() {
        if (requestEditor.getRequest() == null) return 0;
        byte[] currentData = requestEditor.getRequest().toByteArray().getBytes();
        int count = 0;
        byte marker = (byte) '§'; // fallback mapping
        try {
            byte[] mBytes = "§".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
            marker = mBytes[0];
        } catch (Exception ignored) {}
        
        for (byte b : currentData) {
            if (b == marker) count++;
        }
        return count / 2; // Pairs of markers
    }
}
