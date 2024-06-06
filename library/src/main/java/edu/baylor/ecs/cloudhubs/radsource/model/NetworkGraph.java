package edu.baylor.ecs.cloudhubs.radsource.model;

import java.util.Set;

public class NetworkGraph {
    private final String label;
    private final boolean directed;
    private final boolean multigraph;
    private final Set<String> nodes;
    private final String timestamp;
    private final Set<Edge> edges;
    
    public NetworkGraph(String label, boolean directed, boolean multigraph, Set<String> nodes, String timestamp, Set<Edge> edges) {
        this.label = label;
        this.directed = directed;
        this.multigraph = multigraph;
        this.nodes = nodes;
        this.timestamp = timestamp;
        this.edges = edges;
    }

    public NetworkGraph(String label, Set<String> nodes, String timestamp, Set<Edge> edges) {
        this.label = label;
        this.directed = true;
        this.multigraph = false;
        this.nodes = nodes;
        this.timestamp = timestamp;
        this.edges = edges;
    }


}
