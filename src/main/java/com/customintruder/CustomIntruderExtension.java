package com.customintruder;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import com.customintruder.ui.ContextMenuProvider;
import com.customintruder.ui.CustomIntruderUI;

public class CustomIntruderExtension implements BurpExtension {
    
    @Override
    public void initialize(MontoyaApi api) {
        api.extension().setName("Custom Intruder Clone");
        
        // Create main UI
        CustomIntruderUI mainUI = new CustomIntruderUI(api);
        
        // Register UI Tab
        api.userInterface().registerSuiteTab("Custom Intruder", mainUI);
        
        // Register Context Menu Provider
        api.userInterface().registerContextMenuItemsProvider(new ContextMenuProvider(api, mainUI));
        
        api.logging().logToOutput("Custom Intruder Clone loaded successfully.");
    }
}
