package com.zggis.dobby.batch;

public class VideoMergeDTO {

	private FileDTO standardFile;

	private FileDTO blRPUFile;

	public VideoMergeDTO(FileDTO standardFile, FileDTO blRPUFile) {
		super();
		this.standardFile = standardFile;
		this.blRPUFile = blRPUFile;
	}

	public FileDTO getStandardFile() {
		return standardFile;
	}

	public void setStandardFile(FileDTO standardFile) {
		this.standardFile = standardFile;
	}

	public FileDTO getBlRPUFile() {
		return blRPUFile;
	}

	public void setBlRPUFile(FileDTO blRPUFile) {
		this.blRPUFile = blRPUFile;
	}

}
