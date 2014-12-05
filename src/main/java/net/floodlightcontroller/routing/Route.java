package net.floodlightcontroller.routing;

import java.util.ArrayList;
import java.util.List;

import net.floodlightcontroller.topology.NodePortTuple;

/**
 * Represents a route between two switches
 * 两个交换机之间的路径就是一系列端口的结合
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class Route implements Comparable<Route> {
    protected RouteId id;
    protected List<NodePortTuple> switchPorts;

    public Route(RouteId id, List<NodePortTuple> switchPorts) {
        super();
        this.id = id;
        this.switchPorts = switchPorts;
    }

    public Route(Long src, Long dst) {
        super();
        this.id = new RouteId(src, dst);
        this.switchPorts = new ArrayList<NodePortTuple>();
    }

    /**
     * @return the id
     */
    public RouteId getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(RouteId id) {
        this.id = id;
    }

    /**
     * @return the path
     */
    public List<NodePortTuple> getPath() {
        return switchPorts;
    }

    /**
     * @param path the path to set
     */
    public void setPath(List<NodePortTuple> switchPorts) {
        this.switchPorts = switchPorts;
    }

    @Override
    public int hashCode() {
        final int prime = 5791;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((switchPorts == null) ? 0 : switchPorts.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Route other = (Route) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (switchPorts == null) {
            if (other.switchPorts != null)
                return false;
        } else if (!switchPorts.equals(other.switchPorts))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Route [id=" + id + ", switchPorts=" + switchPorts + "]";
    }

    /**
     * Compares the path lengths between Routes.
     */
    @Override
    public int compareTo(Route o) {
        return ((Integer)switchPorts.size()).compareTo(o.switchPorts.size());
    }
}
