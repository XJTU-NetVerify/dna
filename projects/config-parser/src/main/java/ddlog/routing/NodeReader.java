// Automatically generated by the DDlog compiler.
package ddlog.routing;
import com.google.flatbuffers.*;
public final class NodeReader
{
    protected NodeReader(ddlog.__routing.Node inner) { this.inner = inner; }
    private ddlog.__routing.Node inner;
    public vnode_tReader node ()
    {
        return new vnode_tReader(this.inner.node());
    }
    public long as_ ()
    {
        return (long)this.inner.as_();
    }
    public long id ()
    {
        return (long)this.inner.id();
    }
}