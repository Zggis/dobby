package com.zggis.dobby.batch;

public class HevcVideoResults {

	private String standardHevcFileName;

	private String dolbyVisionHevcFileName;

	public HevcVideoResults(String standardHevcFileName, String dolbyVisionHevcFileName) {
		super();
		this.standardHevcFileName = standardHevcFileName;
		this.dolbyVisionHevcFileName = dolbyVisionHevcFileName;
	}

	public String getStandardHevcFileName() {
		return standardHevcFileName;
	}

	public void setStandardHevcFileName(String standardHevcFileName) {
		this.standardHevcFileName = standardHevcFileName;
	}

	public String getDolbyVisionHevcFileName() {
		return dolbyVisionHevcFileName;
	}

	public void setDolbyVisionHevcFileName(String dolbyVisionHevcFileName) {
		this.dolbyVisionHevcFileName = dolbyVisionHevcFileName;
	}

}
