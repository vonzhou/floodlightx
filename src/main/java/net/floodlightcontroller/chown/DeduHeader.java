package net.floodlightcontroller.chown;

public class DeduHeader {
	private int flags;  // 4B
	private int chunkID ; // 4B
	private FingerPrint fp;
	
	public DeduHeader(int flag, int id, FingerPrint fp){
		this.flags = flag;
		this.chunkID = id;
		this.fp = fp;
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public int getChunkID() {
		return chunkID;
	}

	public void setChunkID(int chunkID) {
		this.chunkID = chunkID;
	}

	public FingerPrint getFp() {
		return fp;
	}

	public void setFp(FingerPrint fp) {
		this.fp = fp;
	}
	

}
