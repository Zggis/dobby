package com.zggis.dobby.dto.batch;

public class VideoMergeDTO {

	private VideoFileDTO standardFile;

	private BLRPUHevcFileDTO blRPUFile;
	
	private boolean valid = false;

	public VideoMergeDTO(VideoFileDTO standardFile, BLRPUHevcFileDTO blRPUFile) {
		super();
		this.standardFile = standardFile;
		this.setBlRPUFile(blRPUFile);
	}

	public VideoFileDTO getStandardFile() {
		return standardFile;
	}

	public void setStandardFile(VideoFileDTO standardFile) {
		this.standardFile = standardFile;
	}

	public BLRPUHevcFileDTO getBlRPUFile() {
		return blRPUFile;
	}

	public void setBlRPUFile(BLRPUHevcFileDTO blRPUFile) {
		this.blRPUFile = blRPUFile;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

}
