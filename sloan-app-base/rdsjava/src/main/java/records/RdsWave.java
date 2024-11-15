package records;

public class RdsWave {
	private String waveName;
	private String waveType;
	private int fileSeq;
	
	public RdsWave(String waveName) {
		this.waveName = waveName;
		this.waveType = "";
		this.fileSeq = -1;
	}
	
	public void setWaveType( String waveType ) { this.waveType = waveType; }
	public void setFileSeq( int fileSeq ) { this.fileSeq = fileSeq; }
	
	public String getWaveName() { return this.waveName; }
	public String getWaveType() { return this.waveType; }
	public int getFileSeq() { return this.fileSeq; }
}
