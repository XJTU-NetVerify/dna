package org.ants.parser.datamodel;

import java.math.BigInteger;

public class DCommunity {
  public String as;
  public String tag;

  public DCommunity(String as) {
    this.as = as;
    this.tag = ".*";
  }

  public DCommunity(String as, String tag) {
    this.as = as;
    this.tag = tag;
  }

  public static DCommunity parse(BigInteger value) {
    long cmt = value.longValue();
    long as = cmt >> 16;
    long tag = cmt & 0xFFFF;
    return new DCommunity(String.valueOf(as), String.valueOf(tag));
  }

  public boolean reasonalbe() {
    return as != null;
  }

  @Override
  public String toString() {
    String as_ = as.replaceAll("\\\\", "\\\\\\\\");
    String tag_ = tag.replaceAll("\\\\", "\\\\\\\\");
    return String.format("Community{\"%s\",\"%s\"}", as_, tag_);
  }
}
