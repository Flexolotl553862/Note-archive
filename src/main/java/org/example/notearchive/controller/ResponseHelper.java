package org.example.notearchive.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ResponseHelper {
    public ResponseEntity<Map<String, Object>> ok(String title, String text) {
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "title", title,
                "text", text
        ));
    }

    public ResponseEntity<Map<String, Object>> error(String text) {
        return ResponseEntity.ok(Map.of(
                "ok", false,
                "title", "Error!",
                "text", text
        ));
    }
}
