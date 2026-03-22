package com.customintruder.ui;

import burp.api.montoya.MontoyaApi;
import javax.swing.*;
import java.awt.*;

public class OptionsPanel extends JPanel {
    private final MontoyaApi api;
    private final JSpinner threadCountSpinner;
    private final JSpinner throttleMsSpinner;

    public OptionsPanel(MontoyaApi api) {
        this.api = api;
        this.setLayout(new BorderLayout());
        
        JPanel configPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        configPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        configPanel.add(new JLabel("Number of Threads:"));
        threadCountSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
        configPanel.add(threadCountSpinner);
        
        configPanel.add(new JLabel("Throttle Request Delay (ms):"));
        throttleMsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 100)); // 0 means no limit
        configPanel.add(throttleMsSpinner);
        
        this.add(configPanel, BorderLayout.NORTH);
    }
    
    public int getThreadCount() {
        return (Integer) threadCountSpinner.getValue();
    }
    
    public int getThrottleMs() {
        return (Integer) throttleMsSpinner.getValue();
    }
}
