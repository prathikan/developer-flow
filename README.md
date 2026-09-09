# Developer Flow

Developer Flow is a small command-line tool that turns issue event history into
delivery insights. It helps a team see whether time is being spent coding,
waiting for review, or waiting to reach production.

The project is deliberately dependency-free: a Java 17 installation is all
that is needed.

## What it measures

For each issue, the analyzer finds:

- **Lead time**: first commit to merge (or deploy when available)
- **Review time**: review requested to review approved (or merge)
- **Queue time**: merge to deploy
- **Coding time**: first commit to review requested
- **Flow status**: deployed, merged, or in-flight

The summary identifies the slowest average stage and reports contributor-level
completion and lead-time trends. Incomplete issues are retained in the report
instead of being silently discarded.

## Run it

```bash
./bin/run.sh
./bin/run.sh --input data/events.csv --format json
./bin/run.sh --input data/events.csv --since 2025-01-01
```

The default input is `data/events.csv`. JSON output is useful for piping into a
dashboard or a later automation step.

## Event format

```csv
issue_id,occurred_at,event_type,actor
PAY-101,2025-01-06T09:00:00Z,committed,alex
PAY-101,2025-01-06T13:00:00Z,review_requested,alex
PAY-101,2025-01-07T10:00:00Z,review_approved,priya
PAY-101,2025-01-07T15:00:00Z,merged,alex
PAY-101,2025-01-08T09:30:00Z,deployed,release-bot
```

Supported event types are `committed`, `review_requested`, `review_started`,
`review_approved`, `merged`, and `deployed`.

## Test it

```bash
./bin/test.sh
```

The tests cover stage calculations, in-flight work, contributor summaries, and
CSV parsing.

## Repository layout

```text
src/main/java/com/example/developerflow/
  DeveloperFlowAnalyzer.java  # Domain calculations
  Event.java                   # Input event model
  CsvEventReader.java          # Quoted CSV reader
  ReportFormatter.java         # Human and JSON output
  Main.java                    # CLI entry point
src/test/java/.../DeveloperFlowAnalyzerTest.java
data/events.csv
bin/run.sh
bin/test.sh
```

## Interpreting the output

The bottleneck is a useful conversation starter, not a performance score for
individual engineers. Review time can reflect healthy review quality, and
queue time can reflect intentional release batching. Use the results to improve
the system around the team.