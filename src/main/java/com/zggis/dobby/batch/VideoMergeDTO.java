package com.zggis.dobby.batch;

public class VideoMergeDTO {

	private VideoFileDTO standardFile;

	private HevcFileDTO blRPUFile;

	public VideoMergeDTO(VideoFileDTO standardFile, HevcFileDTO blRPUFile) {
		super();
		this.standardFile = standardFile;
		this.blRPUFile = blRPUFile;
	}

	public VideoFileDTO getStandardFile() {
		return standardFile;
	}

	public void setStandardFile(VideoFileDTO standardFile) {
		this.standardFile = standardFile;
	}

	public HevcFileDTO getBlRPUFile() {
		return blRPUFile;
	}

	public void setBlRPUFile(HevcFileDTO blRPUFile) {
		this.blRPUFile = blRPUFile;
	}

}
