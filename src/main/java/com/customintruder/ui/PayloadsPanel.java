package com.customintruder.ui;

import burp.api.montoya.MontoyaApi;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PayloadsPanel extends JPanel {
    private final MontoyaApi api;
    private final JTextArea payloadsArea;

    public PayloadsPanel(MontoyaApi api) {
        this.api = api;
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JPanel configPanel = new JPanel();
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
        
        // Payload sets
        JPanel setPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        setPanel.setBorder(BorderFactory.createTitledBorder("Payloads"));
        setPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        setPanel.add(new JLabel("Payload position:"));
        setPanel.add(new JComboBox<>(new String[]{"All payload positions"}));
        setPanel.add(new JLabel("Payload type:"));
        setPanel.add(new JComboBox<>(new String[]{"Simple list"}));
        setPanel.add(new JLabel("Payload count:"));
        setPanel.add(new JLabel("0"));
        setPanel.add(new JLabel("Request count:"));
        setPanel.add(new JLabel("0"));
        configPanel.add(setPanel);
        
        configPanel.add(Box.createVerticalStrut(10));
        
        // Payload Options
        JPanel optionsPanel = new JPanel(new BorderLayout(10, 10));
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Payload configuration"));
        optionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JTextArea desc = new JTextArea("This payload type lets you configure a simple list of strings that are used as payloads.");
        desc.setEditable(false);
        desc.setOpaque(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        optionsPanel.add(desc, BorderLayout.NORTH);
        
        JPanel btnsPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        JButton pasteBtn = new JButton("Paste");
        JButton loadBtn = new JButton("Load...");
        JButton removeBtn = new JButton("Remove");
        JButton clearBtn = new JButton("Clear");
        JButton dedupBtn = new JButton("Deduplicate");
        
        payloadsArea = new JTextArea(15, 20);
        
        pasteBtn.addActionListener(e -> payloadsArea.paste());
        
        loadBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    java.io.File file = chooser.getSelectedFile();
                    java.util.List<String> lines = java.nio.file.Files.readAllLines(file.toPath(), java.nio.charset.StandardCharsets.UTF_8);
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
            java.util.Set<String> unique = new java.util.LinkedHashSet<>();
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
        optionsPanel.add(sideLayout, BorderLayout.WEST);
        
        optionsPanel.add(new JScrollPane(payloadsArea), BorderLayout.CENTER);
        
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
        optionsPanel.add(addPanel, BorderLayout.SOUTH);
        
        configPanel.add(optionsPanel);
        
        JScrollPane mainScroll = new JScrollPane(configPanel);
        mainScroll.setBorder(null);
        this.add(mainScroll, BorderLayout.CENTER);
    }
    
    public List<String> getPayloads() {
        String text = payloadsArea.getText();
        List<String> payloads = new ArrayList<>();
        if (text != null && !text.isEmpty()) {
            for (String line : text.split("\n")) {
                if (!line.trim().isEmpty()) {
                    payloads.add(line);
                }
            }
        }
        return payloads;
    }
}
