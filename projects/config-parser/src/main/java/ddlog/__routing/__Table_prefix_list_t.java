// automatically generated by the FlatBuffers compiler, do not modify

package ddlog.__routing;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class __Table_prefix_list_t extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_2_0_0(); }
  public static __Table_prefix_list_t getRootAs__Table_prefix_list_t(ByteBuffer _bb) { return getRootAs__Table_prefix_list_t(_bb, new __Table_prefix_list_t()); }
  public static __Table_prefix_list_t getRootAs__Table_prefix_list_t(ByteBuffer _bb, __Table_prefix_list_t obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public __Table_prefix_list_t __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public byte vType() { int o = __offset(4); return o != 0 ? bb.get(o + bb_pos) : 0; }
  public Table v(Table obj) { int o = __offset(6); return o != 0 ? __union(obj, o + bb_pos) : null; }

  public static int create__Table_prefix_list_t(FlatBufferBuilder builder,
      byte v_type,
      int vOffset) {
    builder.startTable(2);
    __Table_prefix_list_t.addV(builder, vOffset);
    __Table_prefix_list_t.addVType(builder, v_type);
    return __Table_prefix_list_t.end__Table_prefix_list_t(builder);
  }

  public static void start__Table_prefix_list_t(FlatBufferBuilder builder) { builder.startTable(2); }
  public static void addVType(FlatBufferBuilder builder, byte vType) { builder.addByte(0, vType, 0); }
  public static void addV(FlatBufferBuilder builder, int vOffset) { builder.addOffset(1, vOffset, 0); }
  public static int end__Table_prefix_list_t(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public __Table_prefix_list_t get(int j) { return get(new __Table_prefix_list_t(), j); }
    public __Table_prefix_list_t get(__Table_prefix_list_t obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

