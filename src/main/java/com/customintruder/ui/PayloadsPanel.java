package com.customintruder.ui;

import burp.api.montoya.MontoyaApi;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

public class PayloadsPanel extends JPanel {
    private final MontoyaApi api;
    
    // UI Components
    private JComboBox<String> payloadSetCombo;
    private JComboBox<String> payloadTypeCombo;
    private JLabel payloadCountLabel;
    
    private JPanel cardsPanel;
    private CardLayout cardLayout;
    
    // Simple List UI
    private JTextArea payloadsArea;
    // Numbers UI
    private JTextField numFromField;
    private JTextField numToField;
    private JTextField numStepField;
    private JTextField numMinFracField;
    private JTextField numMaxFracField;
    
    // State
    private int currentSetIndex = 1;
    private Map<Integer, PayloadConfig> payloadSets = new HashMap<>();

    public PayloadsPanel(MontoyaApi api) {
        this.api = api;
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        payloadSets.put(1, new PayloadConfig());
        
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        
        // --- Payload sets Section ---
        JPanel setPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        setPanel.setBorder(BorderFactory.createTitledBorder("Payloads"));
        setPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        payloadSetCombo = new JComboBox<>(new String[]{"1"});
        payloadTypeCombo = new JComboBox<>(new String[]{"Simple list", "Numbers"});
        payloadCountLabel = new JLabel("0");
        
        setPanel.add(new JLabel("Payload set:"));
        setPanel.add(payloadSetCombo);
        setPanel.add(new JLabel("Payload type:"));
        setPanel.add(payloadTypeCombo);
        setPanel.add(new JLabel("Payload count:"));
        setPanel.add(payloadCountLabel);
        setPanel.add(new JLabel("Request count:"));
        setPanel.add(new JLabel("0 (Computed at runtime)"));
        topPanel.add(setPanel);
        
        topPanel.add(Box.createVerticalStrut(10));
        
        // --- Payload Options Section (Card Layout) ---
        JPanel optionsPanel = new JPanel(new BorderLayout(10, 10));
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Payload configuration"));
        optionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        
        // Card 1: Simple list
        cardsPanel.add(createSimpleListCard(), "Simple list");
        
        // Card 2: Numbers
        cardsPanel.add(createNumbersCard(), "Numbers");
        
        optionsPanel.add(cardsPanel, BorderLayout.CENTER);
        topPanel.add(optionsPanel);
        
        JScrollPane mainScroll = new JScrollPane(topPanel);
        mainScroll.setBorder(null);
        this.add(mainScroll, BorderLayout.CENTER);
        
        // Listeners
        payloadSetCombo.addActionListener(e -> {
            if (payloadSetCombo.getSelectedItem() != null) {
                saveCurrentState();
                currentSetIndex = Integer.parseInt(payloadSetCombo.getSelectedItem().toString());
                loadState();
            }
        });
        
        payloadTypeCombo.addActionListener(e -> {
            String selectedType = (String) payloadTypeCombo.getSelectedItem();
            cardLayout.show(cardsPanel, selectedType);
        });
        
        // Load initial
        loadState();
    }
    
    private JPanel createSimpleListCard() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        
        JTextArea desc = new JTextArea("This payload type lets you configure a simple list of strings that are used as payloads.");
        desc.setEditable(false);
        desc.setOpaque(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        panel.add(desc, BorderLayout.NORTH);
        
        JPanel btnsPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        JButton pasteBtn = new JButton("Paste");
        JButton loadBtn = new JButton("Load...");
        JButton removeBtn = new JButton("Remove");
        JButton clearBtn = new JButton("Clear");
        JButton dedupBtn = new JButton("Deduplicate");
        
        payloadsArea = new JTextArea(10, 20);
        
        pasteBtn.addActionListener(e -> payloadsArea.paste());
        loadBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = chooser.getSelectedFile();
                    List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                    payloadsArea.append((payloadsArea.getText().isEmpty() ? "" : "\n") + String.join("\n", lines));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error loading file: " + ex.getMessage());
                }
            }
        });
        clearBtn.addActionListener(e -> payloadsArea.setText(""));
        removeBtn.addActionListener(e -> payloadsArea.replaceSelection(""));
        dedupBtn.addActionListener(e -> {
            String[] lines = payloadsArea.getText().split("\n");
            Set<String> unique = new LinkedHashSet<>();
            for (String l : lines) if (!l.trim().isEmpty()) unique.add(l);
            payloadsArea.setText(String.join("\n", unique));
        });
        
        btnsPanel.add(pasteBtn);
        btnsPanel.add(loadBtn);
        btnsPanel.add(removeBtn);
        btnsPanel.add(clearBtn);
        btnsPanel.add(dedupBtn);
        
        JPanel sideLayout = new JPanel(new BorderLayout());
        sideLayout.add(btnsPanel, BorderLayout.NORTH);
        panel.add(sideLayout, BorderLayout.WEST);
        
        panel.add(new JScrollPane(payloadsArea), BorderLayout.CENTER);
        
        JPanel addPanel = new JPanel(new BorderLayout(5, 0));
        JButton addBtn = new JButton("Add");
        JTextField newItemField = new JTextField("Enter a new item");
        
        addBtn.addActionListener(e -> {
            String text = newItemField.getText();
            if (!text.trim().isEmpty() && !text.equals("Enter a new item")) {
                payloadsArea.append((payloadsArea.getText().isEmpty() ? "" : "\n") + text);
                newItemField.setText("");
            }
        });
        
        newItemField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (newItemField.getText().equals("Enter a new item")) newItemField.setText("");
            }
        });
        
        addPanel.add(addBtn, BorderLayout.WEST);
        addPanel.add(newItemField, BorderLayout.CENTER);
        panel.add(addPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createNumbersCard() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        
        JTextArea desc = new JTextArea("This payload type generates numeric payloads within a specified range and in a specified format.");
        desc.setEditable(false);
        desc.setOpaque(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        panel.add(desc, BorderLayout.NORTH);
        
        JPanel grid = new JPanel(new GridLayout(6, 2, 5, 5));
        grid.add(new JLabel("Type:"));
        grid.add(new JComboBox<>(new String[]{"Sequential"}));
        
        grid.add(new JLabel("From:"));
        numFromField = new JTextField("0");
        grid.add(numFromField);
        
        grid.add(new JLabel("To:"));
        numToField = new JTextField("10");
        grid.add(numToField);
        
        grid.add(new JLabel("Step:"));
        numStepField = new JTextField("1");
        grid.add(numStepField);
        
        grid.add(new JLabel("Min fraction digits:"));
        numMinFracField = new JTextField("0");
        grid.add(numMinFracField);
        
        grid.add(new JLabel("Max fraction digits:"));
        numMaxFracField = new JTextField("0");
        grid.add(numMaxFracField);
        
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(grid, BorderLayout.NORTH);
        panel.add(wrapper, BorderLayout.CENTER);
        
        return panel;
    }
    
    public void updatePayloadSetsCount(int markerCount) {
        int sets = Math.max(1, markerCount); // At least 1 set
        
        // Save current state before modifying combobox
        saveCurrentState();
        
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) payloadSetCombo.getModel();
        int currentSize = model.getSize();
        
        if (sets != currentSize) {
            payloadSetCombo.removeAllItems();
            for (int i = 1; i <= sets; i++) {
                payloadSetCombo.addItem(String.valueOf(i));
                if (!payloadSets.containsKey(i)) {
                    payloadSets.put(i, new PayloadConfig());
                }
            }
            if (currentSetIndex <= sets) {
                payloadSetCombo.setSelectedItem(String.valueOf(currentSetIndex));
            } else {
                currentSetIndex = sets;
                payloadSetCombo.setSelectedItem(String.valueOf(currentSetIndex));
            }
        }
        
        loadState();
    }
    
    private void saveCurrentState() {
        PayloadConfig config = payloadSets.computeIfAbsent(currentSetIndex, k -> new PayloadConfig());
        config.type = (String) payloadTypeCombo.getSelectedItem();
        config.simpleListText = payloadsArea.getText();
        
        try { config.numFrom = Double.parseDouble(numFromField.getText()); } catch(Exception ignored){}
        try { config.numTo = Double.parseDouble(numToField.getText()); } catch(Exception ignored){}
        try { config.numStep = Double.parseDouble(numStepField.getText()); } catch(Exception ignored){}
        try { config.numMinFraction = Integer.parseInt(numMinFracField.getText()); } catch(Exception ignored){}
        try { config.numMaxFraction = Integer.parseInt(numMaxFracField.getText()); } catch(Exception ignored){}
    }
    
    private void loadState() {
        PayloadConfig config = payloadSets.computeIfAbsent(currentSetIndex, k -> new PayloadConfig());
        
        payloadTypeCombo.setSelectedItem(config.type);
        payloadsArea.setText(config.simpleListText);
        
        numFromField.setText(String.valueOf(config.numFrom));
        numToField.setText(String.valueOf(config.numTo));
        numStepField.setText(String.valueOf(config.numStep));
        numMinFracField.setText(String.valueOf(config.numMinFraction));
        numMaxFracField.setText(String.valueOf(config.numMaxFraction));
        
        cardLayout.show(cardsPanel, config.type);
        payloadCountLabel.setText(String.valueOf(config.generatePayloads().size()));
    }
    
    public List<List<String>> getAllPayloadSets() {
        saveCurrentState();
        List<List<String>> allSets = new ArrayList<>();
        int count = payloadSetCombo.getItemCount();
        for (int i = 1; i <= count; i++) {
            PayloadConfig config = payloadSets.get(i);
            allSets.add(config != null ? config.generatePayloads() : new ArrayList<>());
        }
        return allSets;
    }
    
    // Fallback for single-list queries
    public List<String> getPayloads() {
        saveCurrentState();
        return payloadSets.get(1).generatePayloads();
    }

    public static class PayloadConfig {
        public String type = "Simple list";
        public String simpleListText = "";
        
        public double numFrom = 0;
        public double numTo = 10;
        public double numStep = 1;
        public int numMinFraction = 0;
        public int numMaxFraction = 0;
        
        public List<String> generatePayloads() {
            List<String> result = new ArrayList<>();
            if (type.equals("Simple list")) {
                for (String line : simpleListText.split("\n")) {
                    if (!line.trim().isEmpty()) {
                        result.add(line);
                    }
                }
            } else if (type.equals("Numbers")) {
                if (numStep == 0) return result; // safety
                if (numFrom <= numTo && numStep > 0) {
                    for (double i = numFrom; i <= numTo; i += numStep) {
                        result.add(formatNumber(i));
                    }
                } else if (numFrom >= numTo && numStep < 0) {
                    for (double i = numFrom; i >= numTo; i += numStep) {
                        result.add(formatNumber(i));
                    }
                }
            }
            return result;
        }
        
        private String formatNumber(double val) {
            java.text.DecimalFormat df = new java.text.DecimalFormat();
            df.setGroupingUsed(false);
            df.setMinimumFractionDigits(numMinFraction);
            df.setMaximumFractionDigits(numMaxFraction);
            return df.format(val);
        }
    }
}
