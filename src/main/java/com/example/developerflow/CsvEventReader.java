package com.example.developerflow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class CsvEventReader {
    private CsvEventReader() {}

    public static List<Event> read(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        if (lines.isEmpty()) {
            return List.of();
        }
        List<String> headers = parseLine(lines.get(0));
        Map<String, Integer> positions = headers.stream()
            .map(String::trim)
            .collect(Collectors.toMap(header -> header, headers::indexOf));
        requireColumns(positions, "issue_id", "occurred_at", "event_type", "actor");

        List<Event> events = new ArrayList<>();
        for (int lineNumber = 1; lineNumber < lines.size(); lineNumber++) {
            if (lines.get(lineNumber).isBlank()) {
                continue;
            }
            List<String> row = parseLine(lines.get(lineNumber));
            if (row.size() != headers.size()) {
                throw new IllegalArgumentException("Line " + (lineNumber + 1)
                    + " has " + row.size() + " columns; expected " + headers.size());
            }
            try {
                events.add(Event.fromCsv(
                    row.get(positions.get("issue_id")),
                    row.get(positions.get("occurred_at")),
                    row.get(positions.get("event_type")),
                    row.get(positions.get("actor"))
                ));
            } catch (RuntimeException error) {
                throw new IllegalArgumentException("Invalid event on line " + (lineNumber + 1)
                    + ": " + error.getMessage(), error);
            }
        }
        return events;
    }

    private static void requireColumns(Map<String, Integer> positions, String... required) {
        Arrays.stream(required).filter(column -> !positions.containsKey(column)).findFirst()
            .ifPresent(column -> { throw new IllegalArgumentException("Missing CSV column: " + column); });
    }

    static List<String> parseLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);
            if (current == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    value.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (current == ',' && !quoted) {
                values.add(value.toString());
                value.setLength(0);
            } else {
                value.append(current);
            }
        }
        if (quoted) {
            throw new IllegalArgumentException("Unclosed quoted CSV field");
        }
        values.add(value.toString());
        return values;
    }
}