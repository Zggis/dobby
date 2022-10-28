package com.zggis.dobby.dto;

public class OldVideoFileDTO {

	private String season;

	private String episode;

	private String fullName;

	private boolean dolbyVision;

	public OldVideoFileDTO() {
		super();
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public boolean isDolbyVision() {
		return dolbyVision;
	}

	public void setDolbyVision(boolean dolbyVision) {
		this.dolbyVision = dolbyVision;
	}

	public String getSeason() {
		return season;
	}

	public void setSeason(String season) {
		this.season = season;
	}

	public String getEpisode() {
		return episode;
	}

	public void setEpisode(String episode) {
		this.episode = episode;
	}

	@Override
	public String toString() {
		return this.fullName;
	}

}
