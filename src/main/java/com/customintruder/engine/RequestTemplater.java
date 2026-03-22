package com.customintruder.engine;

import java.util.ArrayList;
import java.util.List;

public class RequestTemplater {
    private final String template;
    private final List<Marker> markers;
    
    public static class Marker {
        public final int start;
        public final int end;
        public final String originalValue;
        
        public Marker(int start, int end, String originalValue) {
            this.start = start; 
            this.end = end;
            this.originalValue = originalValue;
        }
    }
    
    public RequestTemplater(String requestWithMarkers) {
        this.markers = new ArrayList<>();
        StringBuilder cleanTemplate = new StringBuilder();
        
        int i = 0;
        int markerStart = -1;
        StringBuilder originalValueBuffer = new StringBuilder();
        
        while (i < requestWithMarkers.length()) {
            char c = requestWithMarkers.charAt(i);
            if (c == '§') {
                if (markerStart == -1) {
                    markerStart = cleanTemplate.length();
                    originalValueBuffer.setLength(0); // Clear buffer
                } else {
                    markers.add(new Marker(markerStart, cleanTemplate.length(), originalValueBuffer.toString()));
                    markerStart = -1;
                }
            } else {
                cleanTemplate.append(c);
                if (markerStart != -1) {
                    originalValueBuffer.append(c);
                }
            }
            i++;
        }
        
        this.template = cleanTemplate.toString();
    }
    
    public int getMarkerCount() {
        return markers.size();
    }
    
    public List<Marker> getMarkers() {
        return markers;
    }
    
    /**
     * Injects payloads into the request. If a payload is null, the original value is used.
     * @param payloads A list of payloads matching the size of markers.
     * @return The final templated HTTP request string
     */
    public String buildRequest(List<String> payloads) {
        if (payloads.size() != markers.size()) {
            throw new IllegalArgumentException("Payloads size must match markers size");
        }
        
        StringBuilder result = new StringBuilder();
        int lastPos = 0;
        
        for (int i = 0; i < markers.size(); i++) {
            Marker m = markers.get(i);
            result.append(template, lastPos, m.start);
            
            String p = payloads.get(i);
            result.append(p != null ? p : m.originalValue);
            
            lastPos = m.end;
        }
        
        result.append(template.substring(lastPos));
        return result.toString();
    }
}
