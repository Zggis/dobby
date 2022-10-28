package com.zggis.dobby.batch;

import java.util.ArrayList;
import java.util.List;

public class HevcVideoConversion {

	private String key;

	private String standardFileName;

	private String dolbyVisionFileName;

	private List<HevcFileDTO> results = new ArrayList<>();

	public HevcVideoConversion(String key, String standardFileName, String dolbyVisionFileName) {
		super();
		this.standardFileName = standardFileName;
		this.dolbyVisionFileName = dolbyVisionFileName;
		this.key = key;
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

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public List<HevcFileDTO> getResults() {
		return results;
	}

	public void setResults(List<HevcFileDTO> results) {
		this.results = results;
	}

}
