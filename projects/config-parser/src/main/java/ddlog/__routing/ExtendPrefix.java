// automatically generated by the FlatBuffers compiler, do not modify

package ddlog.__routing;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class ExtendPrefix extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_2_0_0(); }
  public static ExtendPrefix getRootAsExtendPrefix(ByteBuffer _bb) { return getRootAsExtendPrefix(_bb, new ExtendPrefix()); }
  public static ExtendPrefix getRootAsExtendPrefix(ByteBuffer _bb, ExtendPrefix obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public ExtendPrefix __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public ddlog.__routing.prefix_t prefix() { return prefix(new ddlog.__routing.prefix_t()); }
  public ddlog.__routing.prefix_t prefix(ddlog.__routing.prefix_t obj) { int o = __offset(4); return o != 0 ? obj.__assign(__indirect(o + bb_pos), bb) : null; }
  public long ge() { int o = __offset(6); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public long le() { int o = __offset(8); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }

  public static int createExtendPrefix(FlatBufferBuilder builder,
      int prefixOffset,
      long ge,
      long le) {
    builder.startTable(3);
    ExtendPrefix.addLe(builder, le);
    ExtendPrefix.addGe(builder, ge);
    ExtendPrefix.addPrefix(builder, prefixOffset);
    return ExtendPrefix.endExtendPrefix(builder);
  }

  public static void startExtendPrefix(FlatBufferBuilder builder) { builder.startTable(3); }
  public static void addPrefix(FlatBufferBuilder builder, int prefixOffset) { builder.addOffset(0, prefixOffset, 0); }
  public static void addGe(FlatBufferBuilder builder, long ge) { builder.addInt(1, (int)ge, (int)0L); }
  public static void addLe(FlatBufferBuilder builder, long le) { builder.addInt(2, (int)le, (int)0L); }
  public static int endExtendPrefix(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public ExtendPrefix get(int j) { return get(new ExtendPrefix(), j); }
    public ExtendPrefix get(ExtendPrefix obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

