package com.example.developerflow;

import java.time.Instant;
import java.util.Locale;

public record Event(String issueId, Instant occurredAt, EventType type, String actor) {
    public Event {
        if (issueId == null || issueId.isBlank()) {
            throw new IllegalArgumentException("issue_id is required");
        }
        if (occurredAt == null || type == null) {
            throw new IllegalArgumentException("event time and type are required");
        }
        actor = actor == null || actor.isBlank() ? "unknown" : actor.trim();
    }

    public static Event fromCsv(String issueId, String occurredAt, String eventType, String actor) {
        return new Event(
            issueId.trim(),
            Instant.parse(occurredAt.trim()),
            EventType.valueOf(eventType.trim().toUpperCase(Locale.ROOT)),
            actor
        );
    }
}