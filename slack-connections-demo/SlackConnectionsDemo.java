import java.time.Instant;
import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

/**
 * Demo for representing Slack employees and their interaction connections for JIRAs/bugs.
 *
 * Model overview:
 * - Employee: company identity
 * - SlackUser: Slack identity mapped to Employee
 * - Interaction: one event of collaboration across an issue (JIRA or BUG), with timestamp
 * - ConnectionGraph: undirected weighted graph of employees; weight = number of interactions
 *
 * Features:
 * - Record interactions via Slack or Employee IDs
 * - Query neighbors and top collaborators
 * - Shortest path between employees (BFS)
 * - Export to GraphViz (DOT)
 * - Build filtered views by time window (e.g., last N days)
 *
 * How to run (from repo root):
 *   javac slack-connections-demo/SlackConnectionsDemo.java
 *   java -cp slack-connections-demo SlackConnectionsDemo
 */
public class SlackConnectionsDemo {

    public static void main(String[] args) {
        ConnectionGraph graph = new ConnectionGraph();

        // Employees
        var alice = new Employee("E-1001", "Alice", "alice@acme.com", "Platform");
        var bob   = new Employee("E-1002", "Bob",   "bob@acme.com",   "SRE");
        var cara  = new Employee("E-1003", "Cara",  "cara@acme.com",  "Payments");
        var dave  = new Employee("E-1004", "Dave",  "dave@acme.com",  "Platform");

        graph.addEmployee(alice);
        graph.addEmployee(bob);
        graph.addEmployee(cara);
        graph.addEmployee(dave);

        // Slack identities mapped to employees
        graph.addSlackUser(new SlackUser("U1", "alice", "E-1001"));
        graph.addSlackUser(new SlackUser("U2", "bob",   "E-1002"));
        graph.addSlackUser(new SlackUser("U3", "cara",  "E-1003"));
        graph.addSlackUser(new SlackUser("U4", "dave",  "E-1004"));

        // Simulate interactions across time
        Instant now = Instant.now();

        // Recent interactions (within last 7 days)
        graph.recordInteractionBySlack("JIRA-123", "U1", "U2", InteractionType.JIRA, now.minus(Duration.ofDays(3)));
        graph.recordInteractionBySlack("BUG-77",   "U1", "U2", InteractionType.BUG,  now.minus(Duration.ofDays(2)));
        graph.recordInteractionBySlack("JIRA-200", "U1", "U3", InteractionType.JIRA, now.minus(Duration.ofDays(1)));
        graph.recordInteractionBySlack("BUG-90",   "U2", "U4", InteractionType.BUG,  now.minus(Duration.ofDays(1)));
        graph.recordInteractionBySlack("JIRA-201", "U3", "U4", InteractionType.JIRA, now.minus(Duration.ofHours(6)));

        // Older interactions (outside last 7 days)
        graph.recordInteractionBySlack("JIRA-050", "U1", "U4", InteractionType.JIRA, now.minus(Duration.ofDays(30)));
        graph.recordInteractionBySlack("BUG-10",   "U2", "U3", InteractionType.BUG,  now.minus(Duration.ofDays(15)));

        // Basic queries
        System.out.println("All employees: " + graph.getEmployees().values().stream().map(Employee::name).toList());

        System.out.println("Neighbors for Alice:");
        graph.getNeighbors(alice.id()).forEach((neighborId, weight) ->
            System.out.println("  - " + graph.nameOf(neighborId) + " (weight=" + weight + ")")
        );

        System.out.println("Top 2 collaborators for Bob:");
        graph.getTopCollaborators(bob.id(), 2).forEach(e ->
            System.out.println("  - " + graph.nameOf(e.getKey()) + " (weight=" + e.getValue() + ")")
        );

        System.out.println("Shortest path (Alice -> Dave):");
        List<String> path = graph.shortestPath(alice.id(), dave.id());
        if (path.isEmpty()) {
            System.out.println("  No path");
        } else {
            System.out.println("  " + path.stream().map(graph::nameOf).collect(Collectors.joining(" -> ")));
        }

        // Time-window filtered view: last 7 days
        System.out.println("Neighbors for Alice (last 7 days):");
        Map<String, Map<String, Integer>> last7Adj = graph.buildAdjacencyFiltered(
                i -> i.timestamp().isAfter(now.minus(Duration.ofDays(7)))
        );
        Map<String, Integer> aliceRecent = last7Adj.getOrDefault(alice.id(), Map.of());
        aliceRecent.forEach((neighborId, weight) ->
            System.out.println("  - " + graph.nameOf(neighborId) + " (weight=" + weight + ")")
        );

        // Mermaid export (for notebooks with inline markdown)
        System.out.println("\nMermaid (all interactions):\n```mermaid\n" + graph.toMermaid() + "```");

        System.out.println("\nMermaid (last 7 days):\n```mermaid\n" + graph.toMermaidFiltered(
                i -> i.timestamp().isAfter(now.minus(Duration.ofDays(7)))
        ) + "```");

        // Persistence demo: save to JSON and reload
        try {
            Path jsonPath = Path.of("slack-connections-demo/sample-data.json");
            graph.saveJson(jsonPath);
            System.out.println("\nSaved JSON to: " + jsonPath);

            ConnectionGraph loaded = ConnectionGraph.loadJson(jsonPath);
            System.out.println("Reloaded, Alice neighbors:");
            loaded.getNeighbors(alice.id()).forEach((neighborId, weight) ->
                System.out.println("  - " + loaded.nameOf(neighborId) + " (weight=" + weight + ")")
            );
        } catch (IOException e) {
            System.err.println("Persistence error: " + e.getMessage());
        }
    }
}

// Domain model

enum InteractionType {
    JIRA,
    BUG,
    CODE_REVIEW,
    DISCUSSION
}

record Employee(String id, String name, String email, String department) {}

record SlackUser(String slackId, String handle, String employeeId) {}

record Interaction(String issueKey,
                   String employeeA,
                   String employeeB,
                   InteractionType type,
                   Instant timestamp) {}

/**
 * Undirected, weighted employee connection graph.
 * Weight on edge (u, v) = number of interactions recorded between u and v.
 */
class ConnectionGraph {
    private final Map<String, Employee> employees = new HashMap<>();
    private final Map<String, SlackUser> slackUsers = new HashMap<>();
    private final Map<String, String> slackToEmployee = new HashMap<>();
    private final List<Interaction> interactions = new ArrayList<>();

    // Maintains cumulative adjacency for all-time interactions
    // Map<EmployeeId, Map<EmployeeId, weight>>
    private final Map<String, Map<String, Integer>> adjacency = new HashMap<>();

    // Employee management
    public void addEmployee(Employee e) {
        Objects.requireNonNull(e, "employee");
        if (employees.putIfAbsent(e.id(), e) != null) {
            throw new IllegalArgumentException("Employee already exists: " + e.id());
        }
    }

    public Map<String, Employee> getEmployees() {
        return Collections.unmodifiableMap(employees);
    }

    public String nameOf(String employeeId) {
        Employee e = employees.get(employeeId);
        return e == null ? employeeId : e.name();
    }

    // Slack user management
    public void addSlackUser(SlackUser s) {
        Objects.requireNonNull(s, "slackUser");
        if (!employees.containsKey(s.employeeId())) {
            throw new IllegalArgumentException("No employee for slack user mapping: " + s.employeeId());
        }
        if (slackUsers.putIfAbsent(s.slackId(), s) != null) {
            throw new IllegalArgumentException("Slack user already exists: " + s.slackId());
        }
        slackToEmployee.put(s.slackId(), s.employeeId());
    }

    // Recording interactions
    public void recordInteractionBySlack(String issueKey, String slackA, String slackB,
                                         InteractionType type, Instant timestamp) {
        String empA = slackToEmployee.get(slackA);
        String empB = slackToEmployee.get(slackB);
        if (empA == null || empB == null) {
            throw new IllegalArgumentException("Slack ID missing mapping to employee: " + slackA + ", " + slackB);
        }
        recordInteractionByEmployee(issueKey, empA, empB, type, timestamp);
    }

    public void recordInteractionByEmployee(String issueKey, String employeeA, String employeeB,
                                            InteractionType type, Instant timestamp) {
        Objects.requireNonNull(issueKey, "issueKey");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(timestamp, "timestamp");

        if (!employees.containsKey(employeeA) || !employees.containsKey(employeeB)) {
            throw new IllegalArgumentException("Unknown employee(s) for interaction: " + employeeA + ", " + employeeB);
        }
        if (employeeA.equals(employeeB)) {
            // Ignore self-interaction
            return;
        }

        // Normalize ordering (optional, but adjacency is undirected anyway)
        interactions.add(new Interaction(issueKey, employeeA, employeeB, type, timestamp));
        incrementEdge(employeeA, employeeB, 1);
    }

    // Queries

    public Map<String, Integer> getNeighbors(String employeeId) {
        return adjacency.getOrDefault(employeeId, Map.of());
    }

    public List<Map.Entry<String, Integer>> getTopCollaborators(String employeeId, int limit) {
        return getNeighbors(employeeId).entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .toList();
    }

    public List<String> shortestPath(String fromEmployeeId, String toEmployeeId) {
        if (fromEmployeeId.equals(toEmployeeId)) return List.of(fromEmployeeId);
        if (!employees.containsKey(fromEmployeeId) || !employees.containsKey(toEmployeeId)) return List.of();

        Set<String> visited = new HashSet<>();
        Map<String, String> prev = new HashMap<>();
        Deque<String> q = new ArrayDeque<>();

        visited.add(fromEmployeeId);
        q.add(fromEmployeeId);

        while (!q.isEmpty()) {
            String u = q.poll();
            for (String v : adjacency.getOrDefault(u, Map.of()).keySet()) {
                if (!visited.contains(v)) {
                    visited.add(v);
                    prev.put(v, u);
                    if (v.equals(toEmployeeId)) {
                        return reconstructPath(prev, fromEmployeeId, toEmployeeId);
                    }
                    q.add(v);
                }
            }
        }
        return List.of();
    }

    private List<String> reconstructPath(Map<String, String> prev, String start, String end) {
        LinkedList<String> path = new LinkedList<>();
        String curr = end;
        while (curr != null) {
            path.addFirst(curr);
            curr = prev.get(curr);
        }
        if (!path.isEmpty() && path.getFirst().equals(start)) {
            return path;
        }
        return List.of();
    }

    // Filtered views

    public Map<String, Map<String, Integer>> buildAdjacencyFiltered(Predicate<Interaction> filter) {
        Map<String, Map<String, Integer>> adj = new HashMap<>();
        for (Interaction i : interactions) {
            if (filter.test(i)) {
                incrementEdge(adj, i.employeeA(), i.employeeB(), 1);
            }
        }
        // Return as unmodifiable snapshot
        return deepUnmodifiable(adj);
    }

    // Export

    public String toDOT() {
        return toDOTFromAdjacency(this.adjacency);
    }

    public String toDOTFiltered(Predicate<Interaction> filter) {
        Map<String, Map<String, Integer>> adj = buildAdjacencyFiltered(filter);
        return toDOTFromAdjacency(adj);
    }

    private String toDOTFromAdjacency(Map<String, Map<String, Integer>> adj) {
        StringBuilder sb = new StringBuilder();
        sb.append("graph EmployeeConnections {\n");
        sb.append("  node [shape=circle, style=filled, fillcolor=lightyellow];\n");

        // Emit nodes
        for (Employee e : employees.values()) {
            sb.append("  \"").append(e.id()).append("\"")
              .append(" [label=\"").append(e.name()).append("\\n").append(e.department()).append("\"];\n");
        }

        // Emit undirected edges (u < v to avoid duplicates)
        Set<String> done = new HashSet<>();
        for (Map.Entry<String, Map<String, Integer>> uEntry : adj.entrySet()) {
            String u = uEntry.getKey();
            for (Map.Entry<String, Integer> vEntry : uEntry.getValue().entrySet()) {
                String v = vEntry.getKey();
                int w = vEntry.getValue();
                String key = u.compareTo(v) < 0 ? u + "#" + v : v + "#" + u;
                if (done.add(key)) {
                    sb.append("  \"").append(u).append("\" -- \"").append(v).append("\" [label=\"")
                      .append(w).append("\"];\n");
                }
            }
        }
        sb.append("}\n");
        return sb.toString();
    }

    // Mermaid export

    public String toMermaid() {
        return toMermaidFromAdjacency(this.adjacency);
    }

    public String toMermaidFiltered(Predicate<Interaction> filter) {
        Map<String, Map<String, Integer>> adj = buildAdjacencyFiltered(filter);
        return toMermaidFromAdjacency(adj);
    }

    private String toMermaidFromAdjacency(Map<String, Map<String, Integer>> adj) {
        StringBuilder sb = new StringBuilder();
        sb.append("graph TD;\n");

        // Emit nodes
        for (Employee e : employees.values()) {
            String id = mermaidSafeId(e.id());
            sb.append("  ").append(id)
              .append("[\"").append(escapeMermaid(e.name()))
              .append("<br/>").append(escapeMermaid(e.department()))
              .append("\"];\n");
        }

        // Emit undirected edges once
        Set<String> done = new HashSet<>();
        for (Map.Entry<String, Map<String, Integer>> uEntry : adj.entrySet()) {
            String u = uEntry.getKey();
            for (Map.Entry<String, Integer> vEntry : uEntry.getValue().entrySet()) {
                String v = vEntry.getKey();
                int w = vEntry.getValue();
                String key = u.compareTo(v) < 0 ? u + "#" + v : v + "#" + u;
                if (done.add(key)) {
                    sb.append("  ").append(mermaidSafeId(u))
                      .append(" ---|").append(w).append("| ")
                      .append(mermaidSafeId(v)).append(";\n");
                }
            }
        }
        return sb.toString();
    }

    private static String mermaidSafeId(String s) {
        if (s == null || s.isEmpty()) return "N_";
        String base = s.replaceAll("[^A-Za-z0-9_]", "_");
        if (!Character.isLetter(base.charAt(0))) {
            base = "N_" + base;
        }
        return base;
    }

    private static String escapeMermaid(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // Persistence (JSON)

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        // employees
        sb.append("  \"employees\": [\n");
        List<Employee> elist = new ArrayList<>(employees.values());
        elist.sort(Comparator.comparing(Employee::id));
        for (int i = 0; i < elist.size(); i++) {
            Employee e = elist.get(i);
            sb.append("    {")
              .append("\"id\":\"").append(jsonEscape(e.id())).append("\",")
              .append("\"name\":\"").append(jsonEscape(e.name())).append("\",")
              .append("\"email\":\"").append(jsonEscape(e.email())).append("\",")
              .append("\"department\":\"").append(jsonEscape(e.department())).append("\"")
              .append("}");
            if (i < elist.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n");

        // slack users
        sb.append("  \"slackUsers\": [\n");
        List<SlackUser> sulist = new ArrayList<>(slackUsers.values());
        sulist.sort(Comparator.comparing(SlackUser::slackId));
        for (int i = 0; i < sulist.size(); i++) {
            SlackUser s = sulist.get(i);
            sb.append("    {")
              .append("\"slackId\":\"").append(jsonEscape(s.slackId())).append("\",")
              .append("\"handle\":\"").append(jsonEscape(s.handle())).append("\",")
              .append("\"employeeId\":\"").append(jsonEscape(s.employeeId())).append("\"")
              .append("}");
            if (i < sulist.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n");

        // interactions
        sb.append("  \"interactions\": [\n");
        for (int i = 0; i < interactions.size(); i++) {
            Interaction in = interactions.get(i);
            sb.append("    {")
              .append("\"issueKey\":\"").append(jsonEscape(in.issueKey())).append("\",")
              .append("\"employeeA\":\"").append(jsonEscape(in.employeeA())).append("\",")
              .append("\"employeeB\":\"").append(jsonEscape(in.employeeB())).append("\",")
              .append("\"type\":\"").append(in.type().name()).append("\",")
              .append("\"timestamp\":\"").append(in.timestamp().toString()).append("\"")
              .append("}");
            if (i < interactions.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n");

        sb.append("}\n");
        return sb.toString();
    }

    public void saveJson(Path path) throws IOException {
        Files.writeString(path, toJson(), StandardCharsets.UTF_8);
    }

    public static ConnectionGraph loadJson(Path path) throws IOException {
        String json = Files.readString(path, StandardCharsets.UTF_8);
        return fromJson(json);
    }

    public static ConnectionGraph fromJson(String json) {
        ConnectionGraph g = new ConnectionGraph();

        // employees
        String empArr = extractArray(json, "employees");
        for (Map<String, String> m : parseFlatObjectsArray(empArr)) {
            String id = m.get("id");
            if (id == null || id.isEmpty()) continue;
            g.addEmployee(new Employee(
                    id,
                    m.getOrDefault("name", ""),
                    m.getOrDefault("email", ""),
                    m.getOrDefault("department", "")
            ));
        }

        // slack users
        String suArr = extractArray(json, "slackUsers");
        for (Map<String, String> m : parseFlatObjectsArray(suArr)) {
            String sid = m.get("slackId");
            String empId = m.get("employeeId");
            if (sid != null && empId != null) {
                g.addSlackUser(new SlackUser(
                        sid,
                        m.getOrDefault("handle", ""),
                        empId
                ));
            }
        }

        // interactions
        String inArr = extractArray(json, "interactions");
        for (Map<String, String> m : parseFlatObjectsArray(inArr)) {
            String issueKey = m.get("issueKey");
            String a = m.get("employeeA");
            String b = m.get("employeeB");
            String typeS = m.get("type");
            String ts = m.get("timestamp");
            if (issueKey != null && a != null && b != null && typeS != null && ts != null) {
                InteractionType type = InteractionType.valueOf(typeS);
                Instant t = Instant.parse(ts);
                g.recordInteractionByEmployee(issueKey, a, b, type, t);
            }
        }

        return g;
    }

    private static String jsonEscape(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\' -> out.append("\\\\");
                case '"' -> out.append("\\\"");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> out.append(c);
            }
        }
        return out.toString();
    }

    private static String extractArray(String json, String key) {
        String pattern = "\"" + key + "\"";
        int k = json.indexOf(pattern);
        if (k < 0) return "";
        int lb = json.indexOf('[', k);
        if (lb < 0) return "";
        int depth = 0;
        int i = lb;
        for (; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') {
                depth--;
                if (depth == 0) { i++; break; }
            }
        }
        if (i <= lb) return "";
        // return inside brackets
        return json.substring(lb + 1, i - 1);
    }

    private static List<Map<String, String>> parseFlatObjectsArray(String arr) {
        List<Map<String, String>> out = new ArrayList<>();
        int i = 0, n = arr.length();
        while (i < n) {
            // skip whitespace and commas
            while (i < n && (Character.isWhitespace(arr.charAt(i)) || arr.charAt(i) == ',')) i++;
            if (i >= n) break;
            if (arr.charAt(i) != '{') { i++; continue; }
            int start = i;
            int depth = 0;
            boolean inString = false;
            boolean escape = false;
            for (; i < n; i++) {
                char c = arr.charAt(i);
                if (inString) {
                    if (escape) { escape = false; }
                    else if (c == '\\') { escape = true; }
                    else if (c == '\"') { inString = false; }
                    continue;
                }
                if (c == '\"') { inString = true; continue; }
                if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) { i++; break; }
                }
            }
            if (i <= start) break;
            String obj = arr.substring(start, i);
            out.add(parseFlatObject(obj));
        }
        return out;
    }

    private static Map<String, String> parseFlatObject(String obj) {
        Map<String, String> m = new HashMap<>();
        int i = 0, n = obj.length();
        while (i < n) {
            char ch = obj.charAt(i);
            if (ch == '\"') {
                // parse key
                i++;
                StringBuilder key = new StringBuilder();
                boolean esc = false;
                while (i < n) {
                    char c = obj.charAt(i);
                    if (esc) { key.append(c); esc = false; i++; continue; }
                    if (c == '\\') { esc = true; i++; continue; }
                    if (c == '\"') break;
                    key.append(c); i++;
                }
                i++; // skip closing quote

                // skip spaces and colon
                while (i < n && Character.isWhitespace(obj.charAt(i))) i++;
                if (i < n && obj.charAt(i) == ':') i++;
                while (i < n && Character.isWhitespace(obj.charAt(i))) i++;

                // parse value (quoted string)
                String valStr = "";
                if (i < n && obj.charAt(i) == '\"') {
                    i++;
                    StringBuilder val = new StringBuilder();
                    esc = false;
                    while (i < n) {
                        char c = obj.charAt(i);
                        if (esc) {
                            switch (c) {
                                case '"' -> val.append('"');
                                case '\\' -> val.append('\\');
                                case 'n' -> val.append('\n');
                                case 'r' -> val.append('\r');
                                case 't' -> val.append('\t');
                                default -> val.append(c);
                            }
                            esc = false;
                            i++;
                            continue;
                        }
                        if (c == '\\') { esc = true; i++; continue; }
                        if (c == '\"') break;
                        val.append(c); i++;
                    }
                    i++; // skip closing quote
                    valStr = val.toString();
                }
                m.put(key.toString(), valStr);
            } else {
                i++;
            }
        }
        return m;
    }

    // Internal helpers

    private void incrementEdge(String a, String b, int delta) {
        incrementEdge(this.adjacency, a, b, delta);
    }

    private static void incrementEdge(Map<String, Map<String, Integer>> adj, String a, String b, int delta) {
        if (a.equals(b)) return;
        adj.computeIfAbsent(a, k -> new HashMap<>())
           .merge(b, delta, Integer::sum);
        adj.computeIfAbsent(b, k -> new HashMap<>())
           .merge(a, delta, Integer::sum);
    }

    private static Map<String, Map<String, Integer>> deepUnmodifiable(Map<String, Map<String, Integer>> src) {
        Map<String, Map<String, Integer>> out = new HashMap<>();
        for (var e : src.entrySet()) {
            out.put(e.getKey(), Collections.unmodifiableMap(new HashMap<>(e.getValue())));
        }
        return Collections.unmodifiableMap(out);
    }
}
