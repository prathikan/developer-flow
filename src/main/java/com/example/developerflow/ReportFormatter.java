package com.example.developerflow;

import java.util.Locale;

public final class ReportFormatter {
    private ReportFormatter() {}

    public static String text(DeveloperFlowAnalyzer.FlowReport report) {
        StringBuilder output = new StringBuilder();
        output.append("DEVELOPER FLOW INSIGHTS\n");
        output.append("=======================\n");
        output.append("Completed issues: ").append(report.completedCount()).append('\n');
        output.append("In-flight issues: ").append(report.inFlightCount()).append('\n');
        output.append("Average coding time: ").append(hours(report.averageCodingHours())).append('\n');
        output.append("Average lead time: ").append(hours(report.averageLeadHours())).append('\n');
        output.append("Average review time: ").append(hours(report.averageReviewHours())).append('\n');
        output.append("Average deployment queue: ").append(hours(report.averageQueueHours())).append('\n');
        output.append("Current bottleneck: ").append(report.bottleneck()).append("\n\n");

        output.append("ISSUE DETAIL\n");
        output.append("------------\n");
        output.append(String.format(Locale.ROOT, "%-10s %-12s %-12s %8s %8s %8s %8s%n",
            "Issue", "Owner", "Status", "Coding", "Review", "Queue", "Lead"));
        for (var issue : report.issues()) {
            output.append(String.format(Locale.ROOT, "%-10s %-12s %-12s %7.1fh %7.1fh %7.1fh %7.1fh%n",
                issue.issueId(), issue.owner(), issue.status(),
                issue.codingHours(), issue.reviewHours(), issue.queueHours(), issue.leadHours()));
        }

        output.append("\nCONTRIBUTOR VIEW\n-----------------\n");
        for (var contributor : report.contributors()) {
            output.append(String.format(Locale.ROOT, "%s: %d issues, %d completed, %s average lead%n",
                contributor.owner(), contributor.issueCount(), contributor.completedCount(),
                hours(contributor.averageLeadHours())));
        }
        return output.toString();
    }

    public static String json(DeveloperFlowAnalyzer.FlowReport report) {
        StringBuilder output = new StringBuilder("{");
        output.append("\"completedIssues\":").append(report.completedCount()).append(',');
        output.append("\"inFlightIssues\":").append(report.inFlightCount()).append(',');
        output.append("\"averageCodingHours\":").append(number(report.averageCodingHours())).append(',');
        output.append("\"averageLeadHours\":").append(number(report.averageLeadHours())).append(',');
        output.append("\"averageReviewHours\":").append(number(report.averageReviewHours())).append(',');
        output.append("\"averageQueueHours\":").append(number(report.averageQueueHours())).append(',');
        output.append("\"bottleneck\":\"").append(escape(report.bottleneck())).append("\",");
        output.append("\"issues\":[");
        for (int i = 0; i < report.issues().size(); i++) {
            if (i > 0) output.append(',');
            var issue = report.issues().get(i);
            output.append("{\"issueId\":\"").append(escape(issue.issueId()))
                .append("\",\"owner\":\"").append(escape(issue.owner()))
                .append("\",\"status\":\"").append(escape(issue.status()))
                .append("\",\"codingHours\":").append(number(issue.codingHours()))
                .append(",\"reviewHours\":").append(number(issue.reviewHours()))
                .append(",\"queueHours\":").append(number(issue.queueHours()))
                .append(",\"leadHours\":").append(number(issue.leadHours())).append('}');
        }
        output.append("],\"contributors\":[");
        for (int i = 0; i < report.contributors().size(); i++) {
            if (i > 0) output.append(',');
            var contributor = report.contributors().get(i);
            output.append("{\"owner\":\"").append(escape(contributor.owner()))
                .append("\",\"issues\":").append(contributor.issueCount())
                .append(",\"completed\":").append(contributor.completedCount())
                .append(",\"averageLeadHours\":").append(number(contributor.averageLeadHours())).append('}');
        }
        return output.append("]}").toString();
    }

    private static String hours(double value) {
        return String.format(Locale.ROOT, "%.1f hours", value);
    }

    private static String number(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}