package net.floodlightcontroller.chown;

public class FirstPacket {
	int pad1;
	int pad2;
	FingerPrint fp;
	char[] filename;
	// other fields
	
	public char[] getFilename() {
		return filename;
	}
	public FingerPrint getFp() {
		return fp;
	}
	public void setFp(FingerPrint fp) {
		this.fp = fp;
	}
	public void setFilename(char[] filename) {
		this.filename = filename;
	}
	
	
	

}
