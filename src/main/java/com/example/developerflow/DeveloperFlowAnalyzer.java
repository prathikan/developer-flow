package com.example.developerflow;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class DeveloperFlowAnalyzer {
    public FlowReport analyze(List<Event> events) {
        Map<String, List<Event>> byIssue = events.stream()
            .sorted(Comparator.comparing(Event::occurredAt))
            .collect(Collectors.groupingBy(Event::issueId, LinkedHashMap::new, Collectors.toList()));

        List<IssueInsight> issues = byIssue.entrySet().stream()
            .map(entry -> insight(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(IssueInsight::issueId))
            .toList();

        List<IssueInsight> completed = issues.stream().filter(IssueInsight::isCompleted).toList();
        double averageCoding = average(completed, IssueInsight::codingHours);
        double averageLead = average(completed, IssueInsight::leadHours);
        double averageReview = average(completed, IssueInsight::reviewHours);
        double averageQueue = average(completed, IssueInsight::queueHours);
        String bottleneck = bottleneck(averageCoding, averageReview, averageQueue);

        Map<String, ContributorInsight> contributors = new TreeMap<>();
        for (IssueInsight issue : issues) {
            ContributorInsight current = contributors.getOrDefault(
                issue.owner(), new ContributorInsight(issue.owner(), 0, 0, 0));
            contributors.put(issue.owner(), current.add(issue));
        }

        return new FlowReport(
            issues,
            issues.size() - completed.size(),
            completed.size(),
            averageCoding,
            averageLead,
            averageReview,
            averageQueue,
            bottleneck,
            List.copyOf(contributors.values())
        );
    }

    private IssueInsight insight(String issueId, List<Event> events) {
        Map<EventType, Instant> first = new LinkedHashMap<>();
        String owner = "unknown";
        for (Event event : events) {
            first.putIfAbsent(event.type(), event.occurredAt());
            if (event.type() == EventType.COMMITTED) {
                owner = event.actor();
            }
        }

        Instant committed = first.get(EventType.COMMITTED);
        Instant requested = first.get(EventType.REVIEW_REQUESTED);
        Instant merged = first.get(EventType.MERGED);
        Instant deployed = first.get(EventType.DEPLOYED);
        Instant finished = deployed != null ? deployed : merged;
        Instant reviewEnd = first.get(EventType.REVIEW_APPROVED);
        if (reviewEnd == null) {
            reviewEnd = merged;
        }
        double coding = hoursBetween(committed, requested != null ? requested : finished);
        double review = hoursBetween(requested != null ? requested : committed, reviewEnd);
        double queue = hoursBetween(merged, deployed);
        double lead = hoursBetween(committed, finished);
        String status = deployed != null ? "deployed" : merged != null ? "merged" : "in-flight";

        return new IssueInsight(issueId, owner, status, coding, review, queue, lead);
    }

    private static double hoursBetween(Instant start, Instant end) {
        if (start == null || end == null || end.isBefore(start)) {
            return 0;
        }
        return Duration.between(start, end).toSeconds() / 3600.0;
    }

    private static double average(List<IssueInsight> issues, Metric metric) {
        return issues.isEmpty() ? 0 : issues.stream().mapToDouble(metric::value).average().orElse(0);
    }

    private static String bottleneck(double coding, double review, double queue) {
        if (coding == 0 && review == 0 && queue == 0) {
            return "not enough completed work";
        }
        if (review >= coding && review >= queue) return "review";
        if (queue >= coding && queue >= review) return "deployment queue";
        return "coding";
    }

    private interface Metric {
        double value(IssueInsight issue);
    }

    public record IssueInsight(
        String issueId,
        String owner,
        String status,
        double codingHours,
        double reviewHours,
        double queueHours,
        double leadHours
    ) {
        public boolean isCompleted() {
            return status.equals("deployed") || status.equals("merged");
        }
    }

    public record ContributorInsight(String owner, int issueCount, int completedCount, double totalLeadHours) {
        ContributorInsight add(IssueInsight issue) {
            return new ContributorInsight(
                owner,
                issueCount + 1,
                completedCount + (issue.isCompleted() ? 1 : 0),
                totalLeadHours + issue.leadHours()
            );
        }

        public double averageLeadHours() {
            return completedCount == 0 ? 0 : totalLeadHours / completedCount;
        }
    }

    public record FlowReport(
        List<IssueInsight> issues,
        int inFlightCount,
        int completedCount,
        double averageCodingHours,
        double averageLeadHours,
        double averageReviewHours,
        double averageQueueHours,
        String bottleneck,
        List<ContributorInsight> contributors
    ) {}
}