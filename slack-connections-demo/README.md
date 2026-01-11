# Slack Connections Demo (Java)

Represent Slack users (mapped to employees) and their collaboration connections around JIRAs/bugs as an undirected, weighted graph.

Core ideas:
- Node = Employee
- Edge = pair of employees who interacted on an issue
- Weight = number of interactions between the two employees (e.g., across JIRA/BUG events)
- Supports time-window filtering for recent collaboration views
- Export to GraphViz (DOT) for visualization

## Data Model

- Employee: company identity (id, name, email, department)
- SlackUser: Slack identity mapped to an Employee
- Interaction: a single collaboration event between two employees on an issue with a timestamp and type
- ConnectionGraph: undirected, weighted graph maintained from recorded interactions

## Features

- Add employees and Slack-user mappings
- Record interactions using Slack IDs or Employee IDs
- Query:
  - Neighbors of an employee with weights
  - Top collaborators for an employee
  - Shortest path (BFS) between two employees
- Filtered views: build an adjacency using a time predicate (e.g., last 7 days)
- Export to GraphViz DOT (all-time or filtered)

## How to Run

From repository root:

```
javac slack-connections-demo/SlackConnectionsDemo.java
java -cp slack-connections-demo SlackConnectionsDemo
```

## Sample Output

Example run output:

```
All employees: [Dave, Cara, Bob, Alice]
Neighbors for Alice:
  - Dave (weight=1)
  - Cara (weight=1)
  - Bob (weight=2)
Top 2 collaborators for Bob:
  - Alice (weight=2)
  - Dave (weight=1)
Shortest path (Alice -> Dave):
  Alice -> Dave
Neighbors for Alice (last 7 days):
  - Cara (weight=1)
  - Bob (weight=2)

GraphViz DOT (all interactions):
graph EmployeeConnections {
  node [shape=circle, style=filled, fillcolor=lightyellow];
  "E-1004" [label="Dave\nPlatform"];
  "E-1003" [label="Cara\nPayments"];
  "E-1002" [label="Bob\nSRE"];
  "E-1001" [label="Alice\nPlatform"];
  "E-1004" -- "E-1003" [label="1"];
  "E-1004" -- "E-1002" [label="1"];
  "E-1004" -- "E-1001" [label="1"];
  "E-1003" -- "E-1002" [label="1"];
  "E-1003" -- "E-1001" [label="1"];
  "E-1002" -- "E-1001" [label="2"];
}

GraphViz DOT (last 7 days):
graph EmployeeConnections {
  node [shape=circle, style=filled, fillcolor=lightyellow];
  "E-1004" [label="Dave\nPlatform"];
  "E-1003" [label="Cara\nPayments"];
  "E-1002" [label="Bob\nSRE"];
  "E-1001" [label="Alice\nPlatform"];
  "E-1004" -- "E-1003" [label="1"];
  "E-1004" -- "E-1002" [label="1"];
  "E-1003" -- "E-1001" [label="1"];
  "E-1002" -- "E-1001" [label="2"];
}
```

## Exporting DOT to Images (GraphViz)

Install GraphViz (macOS via Homebrew):
```
brew install graphviz
```

Option A: Copy the DOT blocks from the console into files (e.g., `all.dot` and `last7.dot`) and render:
```
dot -Tpng all.dot -o all.png
dot -Tpng last7.dot -o last7.png
```

Option B: Extract DOT blocks automatically via awk:
```
# All interactions
java -cp slack-connections-demo SlackConnectionsDemo \
  | awk '/^graph EmployeeConnections {/,/^}$/' > all.dot

# Last 7 days interactions
java -cp slack-connections-demo SlackConnectionsDemo \
  | awk 'f;/^GraphViz DOT \(last 7 days\):/{f=1;next}' \
  | awk '/^graph EmployeeConnections {/,/^}$/' > last7.dot

dot -Tpng all.dot -o all.png
dot -Tpng last7.dot -o last7.png
```

## Notes and Extensions

- The graph ignores self-interactions.
- Edge weights are cumulative counts; extendable to:
  - Weighted by interaction type (e.g., BUG=2, JIRA=1, CODE_REVIEW=3)
  - Time-decayed weights (recent interactions weigh more)
  - Department-based coloring/styling in DOT export
  - Centrality metrics (degree, betweenness) for identifying hubs
- Integrations: map Slack events/JIRA webhooks to `recordInteraction...` to build this graph from real data.

## Files

- `SlackConnectionsDemo.java`: full demo program with model, queries, and DOT export.
