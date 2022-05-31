package org.ants.generator;

import org.ants.parser.relation.Relation;

import java.util.*;

public class DPGenerator {
    private final DDlogWrapper ddlog;

    public DPGenerator() {
        ddlog = new DDlogWrapper();
    }

    public ArrayList<String> generateFibUpdates(Map<String, List<Relation>> updates) {
        return ddlog.run(updates);
    }

    public ArrayList<String> getFib() {
        return ddlog.getFibEntries();
    }
}
