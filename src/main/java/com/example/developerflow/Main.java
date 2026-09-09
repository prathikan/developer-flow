package com.example.developerflow;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;

public final class Main {
    private Main() {}

    public static void main(String[] args) throws Exception {
        Options options = Options.parse(args);
        var events = CsvEventReader.read(options.input());
        if (options.since() != null) {
            var since = options.since().atStartOfDay().toInstant(ZoneOffset.UTC);
            events = events.stream().filter(event -> !event.occurredAt().isBefore(since)).toList();
        }
        var report = new DeveloperFlowAnalyzer().analyze(events);
        System.out.println(options.json() ? ReportFormatter.json(report) : ReportFormatter.text(report));
    }

    private record Options(Path input, boolean json, LocalDate since) {
        static Options parse(String[] args) {
            Path input = Path.of("data/events.csv");
            boolean json = false;
            LocalDate since = null;
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--input" -> input = Path.of(next(args, ++i, "--input"));
                    case "--format" -> json = "json".equalsIgnoreCase(next(args, ++i, "--format"));
                    case "--since" -> since = LocalDate.parse(next(args, ++i, "--since"));
                    case "--help", "-h" -> {
                        System.out.println("Usage: ./bin/run.sh [--input file] [--format text|json] [--since YYYY-MM-DD]");
                        System.exit(0);
                    }
                    default -> throw new IllegalArgumentException("Unknown option: " + args[i]
                        + "\nUse --help for usage.");
                }
            }
            return new Options(input, json, since);
        }

        private static String next(String[] args, int index, String option) {
            if (index >= args.length) {
                throw new IllegalArgumentException(option + " requires a value. Arguments: "
                    + Arrays.toString(args));
            }
            return args[index];
        }
    }
}