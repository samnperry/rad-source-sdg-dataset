package edu.baylor.ecs.cloudhubs.radsource.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Edge {
    private String source;
    private String target;
    private String endpoint;
    private int weight;

}