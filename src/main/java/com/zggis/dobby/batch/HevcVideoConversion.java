package com.zggis.dobby.batch;

public class HevcVideoConversion {

	private String standardFileName;

	private String dolbyVisionFileName;

	public HevcVideoConversion(String standardFileName, String dolbyVisionFileName) {
		super();
		this.standardFileName = standardFileName;
		this.dolbyVisionFileName = dolbyVisionFileName;
	}

	public String getStandardFileName() {
		return standardFileName;
	}

	public void setStandardFileName(String standardFileName) {
		this.standardFileName = standardFileName;
	}

	public String getDolbyVisionFileName() {
		return dolbyVisionFileName;
	}

	public void setDolbyVisionFileName(String dolbyVisionFileName) {
		this.dolbyVisionFileName = dolbyVisionFileName;
	}

}
