package com.customintruder.ui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.ui.contextmenu.MessageEditorHttpRequestResponse;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ContextMenuProvider implements ContextMenuItemsProvider {
    private final MontoyaApi api;
    private final CustomIntruderUI mainUI;

    public ContextMenuProvider(MontoyaApi api, CustomIntruderUI mainUI) {
        this.api = api;
        this.mainUI = mainUI;
    }

    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        List<Component> menuItems = new ArrayList<>();
        
        // Check if there are messages in the event (like selected request from Proxy/Repeater)
        if (event.messageEditorRequestResponse().isPresent()) {
            MessageEditorHttpRequestResponse messageInfo = event.messageEditorRequestResponse().get();
            JMenuItem sendToCustomIntruderItem = new JMenuItem("Send to Custom Intruder");
            
            sendToCustomIntruderItem.addActionListener(l -> {
                mainUI.sendToCustomIntruder(messageInfo.requestResponse().request());
            });
            menuItems.add(sendToCustomIntruderItem);
            
        } else if (event.selectedRequestResponses() != null && !event.selectedRequestResponses().isEmpty()) {
            JMenuItem sendToCustomIntruderItem = new JMenuItem("Send to Custom Intruder");
            
            sendToCustomIntruderItem.addActionListener(l -> {
                mainUI.sendToCustomIntruder(event.selectedRequestResponses().get(0).request());
            });
            menuItems.add(sendToCustomIntruderItem);
        }

        return menuItems;
    }
}
