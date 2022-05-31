// automatically generated by the FlatBuffers compiler, do not modify

package ddlog.__routing;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class BgpRedis extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_2_0_0(); }
  public static BgpRedis getRootAsBgpRedis(ByteBuffer _bb) { return getRootAsBgpRedis(_bb, new BgpRedis()); }
  public static BgpRedis getRootAsBgpRedis(ByteBuffer _bb, BgpRedis obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public BgpRedis __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public ddlog.__routing.vnode_t node() { return node(new ddlog.__routing.vnode_t()); }
  public ddlog.__routing.vnode_t node(ddlog.__routing.vnode_t obj) { int o = __offset(4); return o != 0 ? obj.__assign(__indirect(o + bb_pos), bb) : null; }
  public String protocol() { int o = __offset(6); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer protocolAsByteBuffer() { return __vector_as_bytebuffer(6, 1); }
  public ByteBuffer protocolInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 6, 1); }

  public static int createBgpRedis(FlatBufferBuilder builder,
      int nodeOffset,
      int protocolOffset) {
    builder.startTable(2);
    BgpRedis.addProtocol(builder, protocolOffset);
    BgpRedis.addNode(builder, nodeOffset);
    return BgpRedis.endBgpRedis(builder);
  }

  public static void startBgpRedis(FlatBufferBuilder builder) { builder.startTable(2); }
  public static void addNode(FlatBufferBuilder builder, int nodeOffset) { builder.addOffset(0, nodeOffset, 0); }
  public static void addProtocol(FlatBufferBuilder builder, int protocolOffset) { builder.addOffset(1, protocolOffset, 0); }
  public static int endBgpRedis(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public BgpRedis get(int j) { return get(new BgpRedis(), j); }
    public BgpRedis get(BgpRedis obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

