package org.ants.parser.relation;

import ddlog.routing.routingUpdateBuilder;

import java.util.Objects;

public class OspfMultipath implements Relation {
    public Node node;
    public long k;
    public long process;

    public OspfMultipath(Node node, long k, long process) {
        this.node = node;
        this.k = k;
        this.process = process;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OspfMultipath that = (OspfMultipath) o;
        return k == that.k
                && process == that.process
                && Objects.equals(node, that.node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node, k, process);
    }

    @Override
    public String toString() {
        return String.format(
                "OspfMultipath(%s,%d,%d),\n", node, k, process);
    }

    @Override
    public void insertRecord(routingUpdateBuilder builder) {
        builder.insert_OspfMultipath(node.DType(builder), k, process);
    }

    @Override
    public void deleteRecord(routingUpdateBuilder builder) {
        builder.delete_OspfMultipath(node.DType(builder), k, process);
    }
}
