package org.ants.generator;

import ddlog.routing.FibReader;
import ddlog.routing.routingRelation;
import ddlog.routing.routingUpdateBuilder;
import ddlog.routing.routingUpdateParser;
import ddlogapi.DDlogAPI;
import ddlogapi.DDlogCommand;
import ddlogapi.DDlogCommand.Kind;
import ddlogapi.DDlogException;
import org.ants.parser.relation.Relation;
import org.ants.parser.utils.IpHelper;

import java.util.*;

public class DDlogWrapper {

    private DDlogAPI ddlogAPI;
    private routingUpdateBuilder builder;
    private ArrayList<String> fibEntries;

    public DDlogWrapper() {
        try {
            this.ddlogAPI = new DDlogAPI(1, false);
        } catch (DDlogException e) {
            e.printStackTrace();
        }
    }

    private void collectFibs(DDlogCommand<Object> command, FibReader fib) {
        String action = command.kind().equals(Kind.Insert) ? "+" : "-";
        String[] prefix = fib.prefix().split("/");
        fibEntries.add(String.format("%s fwd %s %s %s %s %s %s", action, fib.node(), IpHelper.ipToNumber(prefix[0]), prefix[1], fib.intf(), prefix[1], fib.next_hop_ip()));
    }

    private void onCommit(DDlogCommand<Object> command) {
        switch (command.relid()) {
            case routingRelation.Fib:
                collectFibs(command, (FibReader) command.value());
                break;
            default:
        }
    }

    public ArrayList<String> run(Map<String, List<Relation>> updates) {
        fibEntries = new ArrayList<>();
        if (updates != null) {
            try {
                this.ddlogAPI.transactionStart();
                builder = new routingUpdateBuilder();
                insertRecords(updates.get("insert"));
                deleteRecords(updates.get("delete"));
                builder.applyUpdates(this.ddlogAPI);
                routingUpdateParser.transactionCommitDumpChanges(this.ddlogAPI, this::onCommit);
            } catch (DDlogException e) {
                e.printStackTrace();
            }
        }
        return fibEntries;
    }

    public void stop() {
        try {
            this.ddlogAPI.stop();
        } catch (DDlogException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getFibEntries() {
        return fibEntries;
    }

    private void insertRecords(List<Relation> inserts) {
        if (inserts != null) {
            inserts.stream().filter(Objects::nonNull).forEach(r -> r.insertRecord(builder));
        }
    }

    private void deleteRecords(List<Relation> deletes) {
        if (deletes != null) {
            deletes.stream().filter(Objects::nonNull).forEach(r -> r.deleteRecord(builder));
        }
    }
}
