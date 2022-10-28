package com.zggis.dobby.batch;

public class VideoMergeDTO {

	private String standardFileName;

	private String blRPUFileName;

	public VideoMergeDTO(String standardFileName, String blRPUFileName) {
		super();
		this.standardFileName = standardFileName;
		this.blRPUFileName = blRPUFileName;
	}

	public String getStandardFileName() {
		return standardFileName;
	}

	public void setStandardFileName(String standardFileName) {
		this.standardFileName = standardFileName;
	}

	public String getBlRPUFileName() {
		return blRPUFileName;
	}

	public void setBlRPUFileName(String blRPUFileName) {
		this.blRPUFileName = blRPUFileName;
	}

}
