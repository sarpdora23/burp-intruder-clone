package com.customintruder.engine;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.http.HttpService;
import burp.api.montoya.core.ByteArray;

import com.customintruder.ui.ResultsPanel;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SwingUtilities;

public class AttackEngine {
    private final MontoyaApi api;
    private final ResultsPanel resultsPanel;
    private final String attackType;
    
    private ExecutorService executor;
    private AtomicInteger requestIdCounter;
    private volatile boolean isRunning;
    
    // Rate limit configuration
    private int throttleDelayMs;

    public AttackEngine(MontoyaApi api, ResultsPanel resultsPanel, String attackType) {
        this.api = api;
        this.resultsPanel = resultsPanel;
        this.attackType = attackType;
    }
    
    public void startAttack(HttpService targetService, RequestTemplater templater, List<String> wordlist, int threadCount, int throttleMs) {
        this.throttleDelayMs = throttleMs;
        this.executor = Executors.newFixedThreadPool(threadCount);
        this.requestIdCounter = new AtomicInteger(1);
        this.isRunning = true;
        
        // For MVP, implement "Sniper" logic directly: iterate wordlist for each marker one by one.
        executor.submit(() -> {
            try {
                if ("Sniper".equals(attackType)) {
                    runSniper(targetService, templater, wordlist);
                } else if ("Battering Ram".equals(attackType)) {
                    runBatteringRam(targetService, templater, wordlist);
                }
                // Pitchfork and Clusterbomb logic goes here...
            } finally {
                executor.shutdown();
                api.logging().logToOutput("Attack engine finished.");
            }
        });
    }
    
    private void runSniper(HttpService targetService, RequestTemplater templater, List<String> wordlist) {
        int markerCount = templater.getMarkerCount();
        if (markerCount == 0) return;
        
        // Sniper: Target each marker sequentially. Other markers use their original base values (null passed to templater).
        for (int m = 0; m < markerCount && isRunning; m++) {
            for (String payload : wordlist) {
                if (!isRunning) break;
                
                List<String> payloads = new ArrayList<>(markerCount);
                for (int i = 0; i < markerCount; i++) {
                    payloads.add(i == m ? payload : null);
                }
                
                sendRequest(targetService, templater, payloads, payload);
                enforceRateLimit();
            }
        }
    }
    
    private void runBatteringRam(HttpService targetService, RequestTemplater templater, List<String> wordlist) {
        int markerCount = templater.getMarkerCount();
        if (markerCount == 0) return;
        
        for (String payload : wordlist) {
            if (!isRunning) break;
            
            List<String> payloads = new ArrayList<>(markerCount);
            for (int i = 0; i < markerCount; i++) {
                payloads.add(payload); // Battering ram: all markers get the SAME payload
            }
            
            sendRequest(targetService, templater, payloads, payload);
            enforceRateLimit();
        }
    }
    
    private void sendRequest(HttpService targetService, RequestTemplater templater, List<String> mappedPayloads, String displayPayload) {
        final int id = requestIdCounter.getAndIncrement();
        String rawRequestString = templater.buildRequest(mappedPayloads);
        HttpRequest request = HttpRequest.httpRequest(targetService, ByteArray.byteArray(rawRequestString.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1)));
        
        executor.submit(() -> {
            if (!isRunning) return;
            
            long startTime = System.currentTimeMillis();
            burp.api.montoya.http.message.HttpRequestResponse reqRes = api.http().sendRequest(request);
            long duration = System.currentTimeMillis() - startTime;
            
            HttpResponse response = reqRes.response();
            short statusCode = response != null ? response.statusCode() : 0;
            int length = response != null ? response.toByteArray().length() : 0;
            
            // Add to UI Virtual Table
            SwingUtilities.invokeLater(() -> {
                resultsPanel.addResult(id, displayPayload, statusCode, length, duration, request, response);
            });
        });
    }
    
    private void enforceRateLimit() {
        if (throttleDelayMs > 0) {
            try {
                Thread.sleep(throttleDelayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public void stopAttack() {
        isRunning = false;
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
