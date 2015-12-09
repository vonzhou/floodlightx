package net.floodlightcontroller.chown;

import java.util.Arrays;

public class FingerPrint {
	private byte[] fp;

	public byte[] getFp() {
		return fp;
	}

	public void setFp(byte[] fp) {
		this.fp = fp;
	}
	
	public int hashCode(){
		return Arrays.hashCode(fp);
	}
	
	public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
       
        if (!(obj instanceof FingerPrint)) {
            return false;
        }
        FingerPrint other = (FingerPrint) obj;
        
        if (!Arrays.equals(fp, other.fp)) {
            return false;
        }
        return true;
	}
	public String toString(){
		return new String(fp);
	}

	
}
