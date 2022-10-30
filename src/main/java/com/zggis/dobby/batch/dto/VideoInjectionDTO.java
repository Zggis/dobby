package com.zggis.dobby.batch.dto;

public class VideoInjectionDTO {

	private HevcFileDTO standardHevcFile;

	private RPUFileDTO rpuFile;

	public VideoInjectionDTO(HevcFileDTO standardHevcFile, RPUFileDTO rpuFile) {
		super();
		this.standardHevcFile = standardHevcFile;
		this.rpuFile = rpuFile;
	}

	public HevcFileDTO getStandardHevcFile() {
		return standardHevcFile;
	}

	public void setStandardHevcFile(HevcFileDTO standardHevcFile) {
		this.standardHevcFile = standardHevcFile;
	}

	public RPUFileDTO getRpuFile() {
		return rpuFile;
	}

	public void setRpuFile(RPUFileDTO rpuFile) {
		this.rpuFile = rpuFile;
	}

}
