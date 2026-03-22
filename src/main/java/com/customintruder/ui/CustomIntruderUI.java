package com.customintruder.ui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.requests.HttpRequest;

import javax.swing.*;
import java.awt.*;

public class CustomIntruderUI extends JPanel {
    private final MontoyaApi api;
    private final JTabbedPane rootTabs;
    
    // Sub-panels
    private final TargetPositionsPanel targetPositionsPanel;
    private final PayloadsPanel payloadsPanel;
    private final OptionsPanel optionsPanel;
    private final ResultsPanel resultsPanel;
    
    private final JComboBox<String> attackTypeCombo;

    public CustomIntruderUI(MontoyaApi api) {
        this.api = api;
        this.setLayout(new BorderLayout());
        
        targetPositionsPanel = new TargetPositionsPanel(api);
        payloadsPanel = new PayloadsPanel(api);
        optionsPanel = new OptionsPanel(api);
        resultsPanel = new ResultsPanel(api);
        
        rootTabs = new JTabbedPane();
        
        // --- 1. CONFIGURATION TAB
        JPanel configTab = new JPanel(new BorderLayout());
        
        // Top Action Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JPanel leftTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        leftTop.add(new JLabel("Attack type:"));
        attackTypeCombo = new JComboBox<>(new String[]{"Sniper attack", "Battering ram attack", "Pitchfork attack", "Cluster bomb attack"});
        leftTop.add(attackTypeCombo);
        
        JButton startAttackBtn = new JButton("Start attack");
        startAttackBtn.setBackground(new Color(255, 102, 51)); // Orange
        startAttackBtn.setForeground(Color.WHITE);
        startAttackBtn.setFont(startAttackBtn.getFont().deriveFont(Font.BOLD));
        startAttackBtn.addActionListener(e -> launchAttack());
        
        topBar.add(leftTop, BorderLayout.WEST);
        topBar.add(startAttackBtn, BorderLayout.EAST);
        configTab.add(topBar, BorderLayout.NORTH);
        
        // SplitPane for Left (Target+Positions) and Right (Payloads)
        JTabbedPane rightSideTabs = new JTabbedPane(JTabbedPane.TOP);
        rightSideTabs.addTab("Payloads", payloadsPanel);
        rightSideTabs.addTab("Resource pool", optionsPanel);
        rightSideTabs.addTab("Settings", new JPanel());
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, targetPositionsPanel, rightSideTabs);
        splitPane.setResizeWeight(0.65);
        splitPane.setContinuousLayout(true);
        configTab.add(splitPane, BorderLayout.CENTER);
        
        // --- 2. ADD TO ROOT TABS
        rootTabs.addTab("Configuration", configTab);
        rootTabs.addTab("Results", resultsPanel);
        
        this.add(rootTabs, BorderLayout.CENTER);
    }
    
    public void sendToCustomIntruder(HttpRequest request) {
        rootTabs.setSelectedIndex(0);
        targetPositionsPanel.loadRequest(request);
    }
    
    private void launchAttack() {
        try {
            HttpRequest baseReq = targetPositionsPanel.getBaseRequest();
            if (baseReq == null || baseReq.toByteArray().length() == 0) {
                JOptionPane.showMessageDialog(this, "Request is empty!");
                return;
            }
            
            burp.api.montoya.http.HttpService service = targetPositionsPanel.getHttpService();
            if (service == null) {
                JOptionPane.showMessageDialog(this, "Target HTTP Service (Host/Port) is missing.");
                return;
            }
            
            String reqStr = new String(baseReq.toByteArray().getBytes(), java.nio.charset.StandardCharsets.ISO_8859_1);
            com.customintruder.engine.RequestTemplater templater = new com.customintruder.engine.RequestTemplater(reqStr);
            
            if (templater.getMarkerCount() == 0) {
                JOptionPane.showMessageDialog(this, "No § markers defined in the request.");
                return;
            }
            
            java.util.List<String> wordlist = payloadsPanel.getPayloads();
            if (wordlist.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Payload list is empty!");
                return;
            }
            
            String comboSelection = (String) attackTypeCombo.getSelectedItem();
            String attackType = comboSelection.replace(" attack", "");
            int threads = optionsPanel.getThreadCount();
            int throttle = optionsPanel.getThrottleMs();
            
            rootTabs.setSelectedComponent(resultsPanel);
            resultsPanel.clearResults();
            
            com.customintruder.engine.AttackEngine engine = new com.customintruder.engine.AttackEngine(api, resultsPanel, attackType);
            resultsPanel.setEngine(engine);
            engine.startAttack(service, templater, wordlist, threads, throttle);
            
        } catch (Exception ex) {
            api.logging().logToError("Error launching attack: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
