package com.zggis.dobby.batch.dto;

public class HevcFileDTO extends FileDTO {
	
	private boolean dolbyVision;

	public HevcFileDTO(String name, String key, boolean dolbyVision) {
		this.name = name;
		this.key = key;
		this.dolbyVision = dolbyVision;
	}

	public boolean isDolbyVision() {
		return dolbyVision;
	}

	public void setDolbyVision(boolean dolbyVision) {
		this.dolbyVision = dolbyVision;
	}

}
