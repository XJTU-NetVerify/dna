// Automatically generated by the DDlog compiler.
package ddlog.routing;
import com.google.flatbuffers.*;
public final class IBgpNeighborReader
{
    protected IBgpNeighborReader(ddlog.__routing.IBgpNeighbor inner) { this.inner = inner; }
    private ddlog.__routing.IBgpNeighbor inner;
    public vnode_tReader node1 ()
    {
        return new vnode_tReader(this.inner.node1());
    }
    public ip_tReader ip1 ()
    {
        return new ip_tReader(this.inner.ip1());
    }
    public vnode_tReader node2 ()
    {
        return new vnode_tReader(this.inner.node2());
    }
    public ip_tReader ip2 ()
    {
        return new ip_tReader(this.inner.ip2());
    }
}