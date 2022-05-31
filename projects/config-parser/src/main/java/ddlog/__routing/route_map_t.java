// automatically generated by the FlatBuffers compiler, do not modify

package ddlog.__routing;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class route_map_t extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_2_0_0(); }
  public static route_map_t getRootAsroute_map_t(ByteBuffer _bb) { return getRootAsroute_map_t(_bb, new route_map_t()); }
  public static route_map_t getRootAsroute_map_t(ByteBuffer _bb, route_map_t obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public route_map_t __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public boolean permit() { int o = __offset(4); return o != 0 ? 0!=bb.get(o + bb_pos) : false; }
  public ddlog.__routing.__Table_match_attr_t matchCondition(int j) { return matchCondition(new ddlog.__routing.__Table_match_attr_t(), j); }
  public ddlog.__routing.__Table_match_attr_t matchCondition(ddlog.__routing.__Table_match_attr_t obj, int j) { int o = __offset(6); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int matchConditionLength() { int o = __offset(6); return o != 0 ? __vector_len(o) : 0; }
  public ddlog.__routing.__Table_match_attr_t.Vector matchConditionVector() { return matchConditionVector(new ddlog.__routing.__Table_match_attr_t.Vector()); }
  public ddlog.__routing.__Table_match_attr_t.Vector matchConditionVector(ddlog.__routing.__Table_match_attr_t.Vector obj) { int o = __offset(6); return o != 0 ? obj.__assign(__vector(o), 4, bb) : null; }
  public ddlog.__routing.__Table_set_attr_t setAction(int j) { return setAction(new ddlog.__routing.__Table_set_attr_t(), j); }
  public ddlog.__routing.__Table_set_attr_t setAction(ddlog.__routing.__Table_set_attr_t obj, int j) { int o = __offset(8); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int setActionLength() { int o = __offset(8); return o != 0 ? __vector_len(o) : 0; }
  public ddlog.__routing.__Table_set_attr_t.Vector setActionVector() { return setActionVector(new ddlog.__routing.__Table_set_attr_t.Vector()); }
  public ddlog.__routing.__Table_set_attr_t.Vector setActionVector(ddlog.__routing.__Table_set_attr_t.Vector obj) { int o = __offset(8); return o != 0 ? obj.__assign(__vector(o), 4, bb) : null; }

  public static int createroute_map_t(FlatBufferBuilder builder,
      boolean permit,
      int match_conditionOffset,
      int set_actionOffset) {
    builder.startTable(3);
    route_map_t.addSetAction(builder, set_actionOffset);
    route_map_t.addMatchCondition(builder, match_conditionOffset);
    route_map_t.addPermit(builder, permit);
    return route_map_t.endroute_map_t(builder);
  }

  public static void startroute_map_t(FlatBufferBuilder builder) { builder.startTable(3); }
  public static void addPermit(FlatBufferBuilder builder, boolean permit) { builder.addBoolean(0, permit, false); }
  public static void addMatchCondition(FlatBufferBuilder builder, int matchConditionOffset) { builder.addOffset(1, matchConditionOffset, 0); }
  public static int createMatchConditionVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startMatchConditionVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addSetAction(FlatBufferBuilder builder, int setActionOffset) { builder.addOffset(2, setActionOffset, 0); }
  public static int createSetActionVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startSetActionVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endroute_map_t(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public route_map_t get(int j) { return get(new route_map_t(), j); }
    public route_map_t get(route_map_t obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

