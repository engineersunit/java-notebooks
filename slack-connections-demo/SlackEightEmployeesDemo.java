import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Demo initializing a Slack interaction graph with 8 employees across different teams.
 *
 * Reuses the ConnectionGraph/Employee/SlackUser/etc. types defined in SlackConnectionsDemo.java.
 *
 * How to compile/run (from repo root):
 *   javac slack-connections-demo/SlackConnectionsDemo.java slack-connections-demo/SlackEightEmployeesDemo.java
 *   java -cp slack-connections-demo SlackEightEmployeesDemo
 */
public class SlackEightEmployeesDemo {

    public static void main(String[] args) {
        ConnectionGraph graph = new ConnectionGraph();

        // Employees (8 total, different teams)
        var e1 = new Employee("E-2001", "Alice", "alice@acme.com", "Platform");
        var e2 = new Employee("E-2002", "Bob",   "bob@acme.com",   "SRE");
        var e3 = new Employee("E-2003", "Cara",  "cara@acme.com",  "Payments");
        var e4 = new Employee("E-2004", "Dave",  "dave@acme.com",  "Platform");
        var e5 = new Employee("E-2005", "Eve",   "eve@acme.com",   "Security");
        var e6 = new Employee("E-2006", "Frank", "frank@acme.com", "Mobile");
        var e7 = new Employee("E-2007", "Grace", "grace@acme.com", "Data");
        var e8 = new Employee("E-2008", "Heidi", "heidi@acme.com", "UX");

        graph.addEmployee(e1);
        graph.addEmployee(e2);
        graph.addEmployee(e3);
        graph.addEmployee(e4);
        graph.addEmployee(e5);
        graph.addEmployee(e6);
        graph.addEmployee(e7);
        graph.addEmployee(e8);

        // Slack users mapping
        graph.addSlackUser(new SlackUser("U101", "alice", "E-2001"));
        graph.addSlackUser(new SlackUser("U102", "bob",   "E-2002"));
        graph.addSlackUser(new SlackUser("U103", "cara",  "E-2003"));
        graph.addSlackUser(new SlackUser("U104", "dave",  "E-2004"));
        graph.addSlackUser(new SlackUser("U105", "eve",   "E-2005"));
        graph.addSlackUser(new SlackUser("U106", "frank", "E-2006"));
        graph.addSlackUser(new SlackUser("U107", "grace", "E-2007"));
        graph.addSlackUser(new SlackUser("U108", "heidi", "E-2008"));

        Instant now = Instant.now();

        // Recent interactions (within last ~14 days)
        graph.recordInteractionBySlack("JIRA-800", "U101", "U102", InteractionType.JIRA,        now.minus(Duration.ofDays(3)));
        graph.recordInteractionBySlack("BUG-801",  "U101", "U102", InteractionType.BUG,         now.minus(Duration.ofDays(1)));
        graph.recordInteractionBySlack("CR-802",   "U101", "U103", InteractionType.CODE_REVIEW, now.minus(Duration.ofDays(2)));
        graph.recordInteractionBySlack("JIRA-803", "U101", "U104", InteractionType.JIRA,        now.minus(Duration.ofDays(10)));
        graph.recordInteractionBySlack("BUG-804",  "U102", "U104", InteractionType.BUG,         now.minus(Duration.ofDays(5)));
        graph.recordInteractionBySlack("DISC-805", "U102", "U105", InteractionType.DISCUSSION,  now.minus(Duration.ofHours(7)));
        graph.recordInteractionBySlack("JIRA-806", "U103", "U107", InteractionType.JIRA,        now.minus(Duration.ofDays(6)));
        graph.recordInteractionBySlack("CR-807",   "U104", "U106", InteractionType.CODE_REVIEW, now.minus(Duration.ofDays(4)));
        graph.recordInteractionBySlack("JIRA-808", "U105", "U106", InteractionType.JIRA,        now.minus(Duration.ofDays(1)));
        graph.recordInteractionBySlack("BUG-809",  "U105", "U108", InteractionType.BUG,         now.minus(Duration.ofDays(2)));
        graph.recordInteractionBySlack("DISC-810", "U106", "U107", InteractionType.DISCUSSION,  now.minus(Duration.ofDays(3)));
        graph.recordInteractionBySlack("CR-811",   "U107", "U108", InteractionType.CODE_REVIEW, now.minus(Duration.ofDays(1)));
        // Add a few more to create stronger weights
        graph.recordInteractionBySlack("JIRA-812", "U101", "U102", InteractionType.JIRA,        now.minus(Duration.ofDays(2)));
        graph.recordInteractionBySlack("DISC-813", "U105", "U106", InteractionType.DISCUSSION,  now.minus(Duration.ofHours(12)));

        // Older interactions (outside last 14 days)
        graph.recordInteractionBySlack("JIRA-750", "U101", "U108", InteractionType.JIRA,        now.minus(Duration.ofDays(40)));
        graph.recordInteractionBySlack("BUG-751",  "U102", "U103", InteractionType.BUG,         now.minus(Duration.ofDays(20)));
        graph.recordInteractionBySlack("DISC-752", "U104", "U107", InteractionType.DISCUSSION,  now.minus(Duration.ofDays(25)));
        graph.recordInteractionBySlack("JIRA-753", "U106", "U102", InteractionType.JIRA,        now.minus(Duration.ofDays(31)));

        // Overview
        System.out.println("All employees: " + graph.getEmployees().values().stream().map(Employee::name).toList());

        // Neighbors and weights for a couple of employees
        System.out.println("\nNeighbors for Alice:");
        graph.getNeighbors(e1.id()).forEach((neighborId, weight) ->
            System.out.println("  - " + graph.nameOf(neighborId) + " (weight=" + weight + ")")
        );

        System.out.println("\nNeighbors for Eve:");
        graph.getNeighbors(e5.id()).forEach((neighborId, weight) ->
            System.out.println("  - " + graph.nameOf(neighborId) + " (weight=" + weight + ")")
        );

        // Top collaborators
        System.out.println("\nTop 3 collaborators for Grace:");
        graph.getTopCollaborators(e7.id(), 3).forEach(entry ->
            System.out.println("  - " + graph.nameOf(entry.getKey()) + " (weight=" + entry.getValue() + ")")
        );

        // Shortest path example
        System.out.println("\nShortest path (Frank -> Bob):");
        List<String> path = graph.shortestPath(e6.id(), e2.id());
        if (path.isEmpty()) {
            System.out.println("  No path");
        } else {
            System.out.println("  " + path.stream().map(graph::nameOf).collect(Collectors.joining(" -> ")));
        }

        // Time-window filtered view: last 14 days
        System.out.println("\nNeighbors for Alice (last 14 days):");
        Map<String, Map<String, Integer>> last14Adj = graph.buildAdjacencyFiltered(
                i -> i.timestamp().isAfter(now.minus(Duration.ofDays(14)))
        );
        Map<String, Integer> aliceRecent = last14Adj.getOrDefault(e1.id(), Map.of());
        aliceRecent.forEach((neighborId, weight) ->
            System.out.println("  - " + graph.nameOf(neighborId) + " (weight=" + weight + ")")
        );

        // Mermaid exports (for notebooks with inline markdown)
        String mAll = graph.toMermaid();
        System.out.println("\nMermaid (all interactions):\n```mermaid\n" + mAll + "```");
        try {
            java.nio.file.Files.writeString(java.nio.file.Path.of("slack-connections-demo/graph-all.mmd"), mAll);
            System.out.println("Wrote Mermaid to slack-connections-demo/graph-all.mmd");
        } catch (IOException __ex) {
            System.err.println("Failed to write graph-all.mmd: " + __ex.getMessage());
        }

        String m14 = graph.toMermaidFiltered(i -> i.timestamp().isAfter(now.minus(Duration.ofDays(14))));
        System.out.println("\nMermaid (last 14 days):\n```mermaid\n" + m14 + "```");
        try {
            java.nio.file.Files.writeString(java.nio.file.Path.of("slack-connections-demo/graph-14d.mmd"), m14);
            System.out.println("Wrote Mermaid to slack-connections-demo/graph-14d.mmd");
        } catch (IOException __ex) {
            System.err.println("Failed to write graph-14d.mmd: " + __ex.getMessage());
        }

        // Persistence: save and reload
        try {
            Path jsonPath = Path.of("slack-connections-demo/sample-data-8.json");
            graph.saveJson(jsonPath);
            System.out.println("\nSaved JSON to: " + jsonPath);

            ConnectionGraph loaded = ConnectionGraph.loadJson(jsonPath);
            System.out.println("Reloaded, Eve neighbors:");
            loaded.getNeighbors(e5.id()).forEach((neighborId, weight) ->
                System.out.println("  - " + loaded.nameOf(neighborId) + " (weight=" + weight + ")")
            );
        } catch (IOException ex) {
            System.err.println("Persistence error: " + ex.getMessage());
        }
    }
}
