package org.ants.parser.relation;

import ddlog.routing.routingUpdateBuilder;
import java.util.Objects;

public class BgpRedis implements Relation{
    public Node node;
    public String protocol;

    public BgpRedis(Node node, String protocol) {
        this.node = node;
        this.protocol = protocol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BgpRedis bgpRedis = (BgpRedis) o;
        return Objects.equals(node, bgpRedis.node) &&
                Objects.equals(protocol, bgpRedis.protocol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node, protocol);
    }

    @Override public String toString() {
        return String.format("BgpRedis(%s,\"%s\"),\n", node, protocol);
    }

    @Override public void insertRecord(routingUpdateBuilder builder) {
        builder.insert_BgpRedis(node.DType(builder), protocol);
    }

    @Override public void deleteRecord(routingUpdateBuilder builder) {
        builder.delete_BgpRedis(node.DType(builder), protocol);
    }
}
