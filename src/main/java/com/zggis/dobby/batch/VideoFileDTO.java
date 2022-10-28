package com.zggis.dobby.batch;

import com.zggis.dobby.dto.ActiveAreaDTO;

public class VideoFileDTO extends FileDTO {

	private ActiveAreaDTO activeArea;

	public VideoFileDTO(String name, String key) {
		this.name = name;
		this.key = key;
	}

	public ActiveAreaDTO getActiveArea() {
		return activeArea;
	}

	public void setActiveArea(ActiveAreaDTO activeArea) {
		this.activeArea = activeArea;
	}

}
