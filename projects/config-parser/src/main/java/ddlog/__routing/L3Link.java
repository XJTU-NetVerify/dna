// automatically generated by the FlatBuffers compiler, do not modify

package ddlog.__routing;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class L3Link extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_2_0_0(); }
  public static L3Link getRootAsL3Link(ByteBuffer _bb) { return getRootAsL3Link(_bb, new L3Link()); }
  public static L3Link getRootAsL3Link(ByteBuffer _bb, L3Link obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public L3Link __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public String node1() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer node1AsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public ByteBuffer node1InByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 1); }
  public String int1() { int o = __offset(6); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer int1AsByteBuffer() { return __vector_as_bytebuffer(6, 1); }
  public ByteBuffer int1InByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 6, 1); }
  public String node2() { int o = __offset(8); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer node2AsByteBuffer() { return __vector_as_bytebuffer(8, 1); }
  public ByteBuffer node2InByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 8, 1); }
  public String int2() { int o = __offset(10); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer int2AsByteBuffer() { return __vector_as_bytebuffer(10, 1); }
  public ByteBuffer int2InByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 10, 1); }

  public static int createL3Link(FlatBufferBuilder builder,
      int node1Offset,
      int int1Offset,
      int node2Offset,
      int int2Offset) {
    builder.startTable(4);
    L3Link.addInt2(builder, int2Offset);
    L3Link.addNode2(builder, node2Offset);
    L3Link.addInt1(builder, int1Offset);
    L3Link.addNode1(builder, node1Offset);
    return L3Link.endL3Link(builder);
  }

  public static void startL3Link(FlatBufferBuilder builder) { builder.startTable(4); }
  public static void addNode1(FlatBufferBuilder builder, int node1Offset) { builder.addOffset(0, node1Offset, 0); }
  public static void addInt1(FlatBufferBuilder builder, int int1Offset) { builder.addOffset(1, int1Offset, 0); }
  public static void addNode2(FlatBufferBuilder builder, int node2Offset) { builder.addOffset(2, node2Offset, 0); }
  public static void addInt2(FlatBufferBuilder builder, int int2Offset) { builder.addOffset(3, int2Offset, 0); }
  public static int endL3Link(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public L3Link get(int j) { return get(new L3Link(), j); }
    public L3Link get(L3Link obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

