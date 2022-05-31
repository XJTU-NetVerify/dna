package org.ants.parser.relation;

import java.util.Objects;

public class ConnectedRoute {
  public String getNode() {
    return node;
  }

  public String getPrefix() {
    return prefix;
  }

  public String getIntf() {
    return intf;
  }

  public String node;
  public String prefix;
  public String intf;

  public ConnectedRoute(String node, String prefix, String intf) {
    this.node = node;
    this.prefix = prefix;
    this.intf = intf;
  }

  @Override public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ConnectedRoute)) {
      return false;
    }
    ConnectedRoute that = (ConnectedRoute) o;
    return Objects.equals(node, that.node) && Objects.equals(prefix, that.prefix) && Objects.equals(
        intf,
        that.intf);
  }

  @Override public int hashCode() {
    return Objects.hash(node, prefix, intf);
  }
}
