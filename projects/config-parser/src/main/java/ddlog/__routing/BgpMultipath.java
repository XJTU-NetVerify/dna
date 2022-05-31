// automatically generated by the FlatBuffers compiler, do not modify

package ddlog.__routing;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class BgpMultipath extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_2_0_0(); }
  public static BgpMultipath getRootAsBgpMultipath(ByteBuffer _bb) { return getRootAsBgpMultipath(_bb, new BgpMultipath()); }
  public static BgpMultipath getRootAsBgpMultipath(ByteBuffer _bb, BgpMultipath obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public BgpMultipath __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public ddlog.__routing.vnode_t node() { return node(new ddlog.__routing.vnode_t()); }
  public ddlog.__routing.vnode_t node(ddlog.__routing.vnode_t obj) { int o = __offset(4); return o != 0 ? obj.__assign(__indirect(o + bb_pos), bb) : null; }
  public long k() { int o = __offset(6); return o != 0 ? bb.getLong(o + bb_pos) : 0L; }
  public boolean relax() { int o = __offset(8); return o != 0 ? 0!=bb.get(o + bb_pos) : false; }

  public static int createBgpMultipath(FlatBufferBuilder builder,
      int nodeOffset,
      long k,
      boolean relax) {
    builder.startTable(3);
    BgpMultipath.addK(builder, k);
    BgpMultipath.addNode(builder, nodeOffset);
    BgpMultipath.addRelax(builder, relax);
    return BgpMultipath.endBgpMultipath(builder);
  }

  public static void startBgpMultipath(FlatBufferBuilder builder) { builder.startTable(3); }
  public static void addNode(FlatBufferBuilder builder, int nodeOffset) { builder.addOffset(0, nodeOffset, 0); }
  public static void addK(FlatBufferBuilder builder, long k) { builder.addLong(1, k, 0L); }
  public static void addRelax(FlatBufferBuilder builder, boolean relax) { builder.addBoolean(2, relax, false); }
  public static int endBgpMultipath(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public BgpMultipath get(int j) { return get(new BgpMultipath(), j); }
    public BgpMultipath get(BgpMultipath obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

