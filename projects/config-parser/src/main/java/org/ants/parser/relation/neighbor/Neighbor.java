package org.ants.parser.relation.neighbor;

import java.util.Objects;
import org.ants.parser.relation.Node;

public class Neighbor implements Cloneable{
  public Node node1;
  public String ip1;
  public Node node2;
  public String ip2;

  public Neighbor(){}

  public Neighbor(Node node1, String ip1, Node node2, String ip2) {
    this.node1 = node1;
    this.ip1 = ip1;
    this.node2 = node2;
    this.ip2 = ip2;
  }

  @Override public Neighbor clone() throws CloneNotSupportedException{
    return (Neighbor)super.clone();
  }

  @Override public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Neighbor)) {
      return false;
    }
    Neighbor neighbor = (Neighbor) o;
    return Objects.equals(node1, neighbor.node1) && Objects.equals(ip1, neighbor.ip1) && Objects
        .equals(node2, neighbor.node2) && Objects.equals(
        ip2,
        neighbor.ip2);
  }

  @Override public int hashCode() {
    return Objects.hash(node1, ip1, node2, ip2);
  }

  public static String proc(String ip) {
    return ip.replace('.', ',');
  }

  @Override public String toString(){
    return String.format("(%s,Ip{%s},%s,Ip{%s}),\n"
    ,node1,proc(ip1),node2,proc(ip2));
  }
}
