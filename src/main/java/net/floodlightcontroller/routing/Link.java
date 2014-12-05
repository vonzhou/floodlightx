package net.floodlightcontroller.routing;

import net.floodlightcontroller.core.web.serializers.DPIDSerializer;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.openflow.util.HexString;

public class Link {
    private long src;
    private short srcPort;
    private long dst;
    private short dstPort;

    public Link(long srcId, short srcPort, long dstId, short dstPort) {
        this.src = srcId;
        this.srcPort = srcPort;
        this.dst = dstId;
        this.dstPort = dstPort;
    }

    
    @JsonProperty("src-switch")
    @JsonSerialize(using=DPIDSerializer.class)
    public long getSrc() {
        return src;
    }

    @JsonProperty("src-port")
    public short getSrcPort() {
        return srcPort;
    }

    @JsonProperty("dst-switch")
    @JsonSerialize(using=DPIDSerializer.class)
    public long getDst() {
        return dst;
    }
    @JsonProperty("dst-port")
    public short getDstPort() {
        return dstPort;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (dst ^ (dst >>> 32));
        result = prime * result + dstPort;
        result = prime * result + (int) (src ^ (src >>> 32));
        result = prime * result + srcPort;
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
        Link other = (Link) obj;
        if (dst != other.dst)
            return false;
        if (dstPort != other.dstPort)
            return false;
        if (src != other.src)
            return false;
        if (srcPort != other.srcPort)
            return false;
        return true;
    }


    @Override
    public String toString() {
        return "Link [src=" + HexString.toHexString(this.src) 
                + " outPort="
                + srcPort
                + ", dst=" + HexString.toHexString(this.dst)
                + ", inPort="
                + dstPort
                + "]";
    }
}

