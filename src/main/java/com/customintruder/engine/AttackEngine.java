package com.customintruder.engine;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.HttpService;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import com.customintruder.ui.ResultsPanel;

import javax.swing.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class AttackEngine {
    private final MontoyaApi api;
    private final ResultsPanel resultsPanel;
    private final String attackType;
    
    private ExecutorService executor;
    private volatile boolean isRunning = false;
    private final AtomicInteger requestIdCounter = new AtomicInteger(1);

    public AttackEngine(MontoyaApi api, ResultsPanel resultsPanel, String attackType) {
        this.api = api;
        this.resultsPanel = resultsPanel;
        this.attackType = attackType;
    }

    public void startAttack(HttpService targetService, RequestTemplater templater, List<List<String>> payloadSets, int threads, int throttle) {
        this.isRunning = true;
        this.executor = Executors.newFixedThreadPool(threads);
        
        new Thread(() -> {
            try {
                if (attackType.equalsIgnoreCase("Sniper")) {
                    runSniper(targetService, templater, payloadSets.isEmpty() ? new ArrayList<>() : payloadSets.get(0), throttle);
                } else if (attackType.equalsIgnoreCase("Battering ram")) {
                    runBatteringRam(targetService, templater, payloadSets.isEmpty() ? new ArrayList<>() : payloadSets.get(0), throttle);
                } else if (attackType.equalsIgnoreCase("Pitchfork")) {
                    runPitchfork(targetService, templater, payloadSets, throttle);
                } else if (attackType.equalsIgnoreCase("Cluster bomb")) {
                    runClusterBomb(targetService, templater, payloadSets, throttle);
                }
            } catch (Exception e) {
                api.logging().logToError("Engine error: " + e.getMessage());
            } finally {
                executor.shutdown();
                SwingUtilities.invokeLater(() -> {
                    api.logging().logToOutput("Attack finished.");
                });
            }
        }).start();
    }
    
    public void stopAttack() {
        this.isRunning = false;
        if (this.executor != null) {
            this.executor.shutdownNow();
        }
    }
    
    private void runSniper(HttpService targetService, RequestTemplater templater, List<String> wordlist, int throttle) {
        int markerCount = templater.getMarkerCount();
        if (markerCount == 0) return;
        
        for (String payload : wordlist) {
            if (!isRunning) return;
            for (int i = 0; i < markerCount; i++) {
                if (!isRunning) return;
                List<String> mapped = new ArrayList<>();
                for (int j = 0; j < markerCount; j++) mapped.add(j == i ? payload : "");
                sendRequest(targetService, templater, mapped, payload);
                if (throttle > 0) { try { Thread.sleep(throttle); } catch(Exception ignored){} }
            }
        }
    }
    
    private void runBatteringRam(HttpService targetService, RequestTemplater templater, List<String> wordlist, int throttle) {
        int markerCount = templater.getMarkerCount();
        for (String payload : wordlist) {
            if (!isRunning) return;
            List<String> mapped = new ArrayList<>();
            for (int j = 0; j < markerCount; j++) mapped.add(payload);
            sendRequest(targetService, templater, mapped, payload);
            if (throttle > 0) { try { Thread.sleep(throttle); } catch(Exception ignored){} }
        }
    }
    
    private void runPitchfork(HttpService targetService, RequestTemplater templater, List<List<String>> payloadSets, int throttle) {
        int markerCount = templater.getMarkerCount();
        int minRows = Integer.MAX_VALUE;
        for (int i = 0; i < markerCount; i++) {
            int size = (i < payloadSets.size()) ? payloadSets.get(i).size() : 0;
            minRows = Math.min(minRows, size);
        }
        if (minRows == Integer.MAX_VALUE || minRows == 0) return;
        
        for (int row = 0; row < minRows; row++) {
            if (!isRunning) return;
            List<String> mapped = new ArrayList<>();
            for (int i = 0; i < markerCount; i++) {
                mapped.add(payloadSets.get(i).get(row));
            }
            sendRequest(targetService, templater, mapped, String.join(" | ", mapped));
            if (throttle > 0) { try { Thread.sleep(throttle); } catch(Exception ignored){} }
        }
    }
    
    private void runClusterBomb(HttpService targetService, RequestTemplater templater, List<List<String>> payloadSets, int throttle) {
        int markerCount = templater.getMarkerCount();
        if (markerCount == 0 || payloadSets.isEmpty()) return;
        
        generateClusterBombRecursive(targetService, templater, payloadSets, markerCount, 0, new ArrayList<>(), throttle);
    }
    
    private void generateClusterBombRecursive(HttpService targetService, RequestTemplater templater, List<List<String>> payloadSets, int markerCount, int currentMarkerIndex, List<String> currentMapped, int throttle) {
        if (!isRunning) return;
        if (currentMarkerIndex == markerCount) {
            sendRequest(targetService, templater, new ArrayList<>(currentMapped), String.join(" | ", currentMapped));
            if (throttle > 0) { try { Thread.sleep(throttle); } catch(Exception ignored){} }
            return;
        }
        
        List<String> currentSet = (currentMarkerIndex < payloadSets.size()) ? payloadSets.get(currentMarkerIndex) : new ArrayList<>();
        if (currentSet.isEmpty()) currentSet.add(""); 
        
        for (String payload : currentSet) {
            if (!isRunning) return;
            currentMapped.add(payload);
            generateClusterBombRecursive(targetService, templater, payloadSets, markerCount, currentMarkerIndex + 1, currentMapped, throttle);
            currentMapped.remove(currentMapped.size() - 1);
        }
    }

    private void sendRequest(HttpService targetService, RequestTemplater templater, List<String> mappedPayloads, String displayPayload) {
        final int id = requestIdCounter.getAndIncrement();
        String rawRequestString = templater.buildRequest(mappedPayloads);
        HttpRequest request = HttpRequest.httpRequest(targetService, ByteArray.byteArray(rawRequestString.getBytes(StandardCharsets.ISO_8859_1)));
        
        executor.submit(() -> {
            if (!isRunning) return;
            long startTime = System.currentTimeMillis();
            try {
                HttpResponse response = api.http().sendRequest(request).response();
                long duration = System.currentTimeMillis() - startTime;
                
                short statusCode = response.statusCode();
                int length = response.body().length();
                
                SwingUtilities.invokeLater(() -> {
                    resultsPanel.addResult(id, displayPayload, statusCode, length, duration, request, response);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    resultsPanel.addResult(id, displayPayload + " (Error)", (short) 0, 0, System.currentTimeMillis() - startTime, request, null);
                });
            }
        });
    }
}
