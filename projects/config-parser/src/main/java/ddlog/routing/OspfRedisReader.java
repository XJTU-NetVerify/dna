// Automatically generated by the DDlog compiler.
package ddlog.routing;
import com.google.flatbuffers.*;
public final class OspfRedisReader
{
    protected OspfRedisReader(ddlog.__routing.OspfRedis inner) { this.inner = inner; }
    private ddlog.__routing.OspfRedis inner;
    public vnode_tReader node ()
    {
        return new vnode_tReader(this.inner.node());
    }
    public String protocol ()
    {
        return this.inner.protocol();
    }
    public long process ()
    {
        return (long)this.inner.process();
    }
}