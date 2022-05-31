package org.ants.parser.datamodel;

import ddlog.routing.ip_tWriter;
import ddlog.routing.prefix_tWriter;
import ddlog.routing.routingUpdateBuilder;
import org.batfish.datamodel.Ip;

import java.util.Arrays;
import java.util.Objects;

public class Prefix {
  public String ip;
  public String mask;

  public Prefix(String prefixText) {
    org.batfish.datamodel.Prefix prefix = org.batfish.datamodel.Prefix.parse(prefixText);
    this.ip = prefix.getStartIp().toString();
    long mask = prefix.getPrefixWildcard().asLong() ^ (((long) 1 << 32) - 1);
    this.mask = Ip.create(mask).toString();
  }

  public Prefix(String ip, String prefixText) {
    org.batfish.datamodel.Prefix prefix = org.batfish.datamodel.Prefix.parse(prefixText);
    this.ip = ip;
    long mask = prefix.getPrefixWildcard().asLong() ^ (((long) 1 << 32) - 1);
    this.mask = Ip.create(mask).toString();
  }

  public static String proc(String ip) {
    return ip.replace('.', ',');
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Prefix)) {
      return false;
    }
    Prefix prefix = (Prefix) o;
    return Objects.equals(ip, prefix.ip) && Objects.equals(mask, prefix.mask);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ip, mask);
  }

  @Override
  public String toString() {
    return String.format("Prefix{Ip{%s}, Ip{%s}}", proc(ip), proc(mask));
  }

  public static ip_tWriter ipWrapper(String rawIp, routingUpdateBuilder builder) {
    Integer[] ip = Arrays.stream(rawIp.split("\\.")).map(Integer::parseInt).toArray(Integer[]::new);
    return builder.create_ip_t(ip[0], ip[1], ip[2], ip[3]);
  }

  public prefix_tWriter Dtype(routingUpdateBuilder builder){
    ip_tWriter addr = ipWrapper(this.ip, builder);
    ip_tWriter mask = ipWrapper(this.mask, builder);
    return builder.create_prefix_t(addr, mask);
  }
}
