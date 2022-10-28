package com.zggis.dobby.batch;

public class VideoInjectionDTO {

	private FileDTO standardHevcFile;

	private FileDTO rpuFile;

	public VideoInjectionDTO(FileDTO standardHevcFileName, FileDTO rpuFileName) {
		super();
		this.standardHevcFile = standardHevcFileName;
		this.rpuFile = rpuFileName;
	}

	public FileDTO getStandardHevcFile() {
		return standardHevcFile;
	}

	public void setStandardHevcFile(FileDTO standardHevcFileName) {
		this.standardHevcFile = standardHevcFileName;
	}

	public FileDTO getRpuFile() {
		return rpuFile;
	}

	public void setRpuFile(FileDTO rpuFileName) {
		this.rpuFile = rpuFileName;
	}

}
