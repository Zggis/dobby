package com.zggis.dobby.dto.batch;

import java.util.ArrayList;
import java.util.List;

public class TVShowConversionDTO {

	private String key;

	private VideoFileDTO standardFile;

	private VideoFileDTO dolbyVisionFile;

	private List<HevcFileDTO> results = new ArrayList<>();

	public TVShowConversionDTO(String key, VideoFileDTO standardFile, VideoFileDTO dolbyVisionFile) {
		super();
		this.standardFile = standardFile;
		this.dolbyVisionFile = dolbyVisionFile;
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public VideoFileDTO getStandardFile() {
		return standardFile;
	}

	public void setStandardFile(VideoFileDTO standardFile) {
		this.standardFile = standardFile;
	}

	public VideoFileDTO getDolbyVisionFile() {
		return dolbyVisionFile;
	}

	public void setDolbyVisionFile(VideoFileDTO dolbyVisionFile) {
		this.dolbyVisionFile = dolbyVisionFile;
	}

	public List<HevcFileDTO> getResults() {
		return results;
	}

	public void setResults(List<HevcFileDTO> results) {
		this.results = results;
	}

}
