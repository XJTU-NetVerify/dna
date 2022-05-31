package org.ants.parser.relation.routeMap.matchCondition;

import ddlog.routing.prefix_list_tWriter;
import ddlog.routing.routingUpdateBuilder;
import java.util.Objects;
import org.ants.parser.datamodel.Prefix;

public class ExtendPrefix extends Prefix {

  public int ge;
  public int le;

  public ExtendPrefix(String prefixText, int ge, int le) {
    super(prefixText);
    this.ge = ge;
    this.le = le;
  }

  @Override public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ExtendPrefix)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    ExtendPrefix that = (ExtendPrefix) o;
    return ge == that.ge && le == that.le;
  }

  @Override public int hashCode() {
    return Objects.hash(super.hashCode(), ge, le);
  }

  @Override public String toString() {
    return String.format("ExtendPrefix{%s, %d, %d}", super.toString(), ge, le);
  }

  public prefix_list_tWriter DType(routingUpdateBuilder builder){
    return builder.create_ExtendPrefix(super.Dtype(builder), ge, le);
  }
}
