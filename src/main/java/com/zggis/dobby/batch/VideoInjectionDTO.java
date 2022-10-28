package com.zggis.dobby.batch;

public class VideoInjectionDTO {

	private String standardHevcFileName;

	private String rpuFileName;

	public VideoInjectionDTO(String standardHevcFileName, String rpuFileName) {
		super();
		this.standardHevcFileName = standardHevcFileName;
		this.rpuFileName = rpuFileName;
	}

	public String getStandardHevcFileName() {
		return standardHevcFileName;
	}

	public void setStandardHevcFileName(String standardHevcFileName) {
		this.standardHevcFileName = standardHevcFileName;
	}

	public String getRpuFileName() {
		return rpuFileName;
	}

	public void setRpuFileName(String rpuFileName) {
		this.rpuFileName = rpuFileName;
	}

}
