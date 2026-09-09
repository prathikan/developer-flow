package com.example.developerflow;

import java.time.Instant;
import java.util.List;

public final class DeveloperFlowAnalyzerTest {
    public static void main(String[] args) {
        completedIssueCalculatesStages();
        inFlightIssueRemainsVisible();
        contributorsAggregateCompletedWork();
        csvParserHandlesQuotedValues();
        System.out.println("Developer Flow tests passed.");
    }

    private static void completedIssueCalculatesStages() {
        var report = new DeveloperFlowAnalyzer().analyze(List.of(
            event("A-1", "2025-01-01T09:00:00Z", EventType.COMMITTED, "sam"),
            event("A-1", "2025-01-01T13:00:00Z", EventType.REVIEW_REQUESTED, "sam"),
            event("A-1", "2025-01-02T13:00:00Z", EventType.REVIEW_APPROVED, "lee"),
            event("A-1", "2025-01-02T14:00:00Z", EventType.MERGED, "sam"),
            event("A-1", "2025-01-03T14:00:00Z", EventType.DEPLOYED, "bot")
        ));
        var issue = report.issues().get(0);
        assertNear(4, issue.codingHours());
        assertNear(24, issue.reviewHours());
        assertNear(24, issue.queueHours());
        assertNear(53, issue.leadHours());
        assertEquals("deployed", issue.status());
        assertEquals("review", report.bottleneck());
    }

    private static void inFlightIssueRemainsVisible() {
        var report = new DeveloperFlowAnalyzer().analyze(List.of(
            event("A-2", "2025-01-01T09:00:00Z", EventType.COMMITTED, "sam"),
            event("A-2", "2025-01-02T09:00:00Z", EventType.REVIEW_REQUESTED, "sam")
        ));
        assertEquals(1, report.inFlightCount());
        assertEquals(0, report.completedCount());
        assertEquals("in-flight", report.issues().get(0).status());
    }

    private static void contributorsAggregateCompletedWork() {
        var report = new DeveloperFlowAnalyzer().analyze(List.of(
            event("A-1", "2025-01-01T00:00:00Z", EventType.COMMITTED, "sam"),
            event("A-1", "2025-01-02T00:00:00Z", EventType.MERGED, "sam"),
            event("A-2", "2025-01-01T00:00:00Z", EventType.COMMITTED, "sam"),
            event("A-2", "2025-01-03T00:00:00Z", EventType.MERGED, "sam")
        ));
        var sam = report.contributors().get(0);
        assertEquals(2, sam.issueCount());
        assertEquals(2, sam.completedCount());
        assertNear(36, sam.averageLeadHours());
    }

    private static void csvParserHandlesQuotedValues() {
        var values = CsvEventReader.parseLine("A-1,\"2025-01-01T00:00:00Z\",committed,\"sam, team\"");
        assertEquals(4, values.size());
        assertEquals("sam, team", values.get(3));
    }

    private static Event event(String issue, String time, EventType type, String actor) {
        return new Event(issue, Instant.parse(time), type, actor);
    }

    private static void assertNear(double expected, double actual) {
        assert Math.abs(expected - actual) < 0.001 :
            "Expected " + expected + " but got " + actual;
    }

    private static void assertEquals(Object expected, Object actual) {
        assert expected.equals(actual) : "Expected " + expected + " but got " + actual;
    }
}