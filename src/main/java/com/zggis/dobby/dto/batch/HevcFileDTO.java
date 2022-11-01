package com.zggis.dobby.dto.batch;

import com.zggis.dobby.dto.mediainfo.MediaInfoDTO;

public class HevcFileDTO extends FileDTO {

	private boolean dolbyVision;

	private MediaInfoDTO mediaInfo;

	public HevcFileDTO(String name, String key, MediaInfoDTO mediaInfo, boolean dolbyVision) {
		this.name = name;
		this.key = key;
		this.dolbyVision = dolbyVision;
		this.mediaInfo = mediaInfo;
	}

	public boolean isDolbyVision() {
		return dolbyVision;
	}

	public void setDolbyVision(boolean dolbyVision) {
		this.dolbyVision = dolbyVision;
	}

	public MediaInfoDTO getMediaInfo() {
		return mediaInfo;
	}

	public void setMediaInfo(MediaInfoDTO mediaInfo) {
		this.mediaInfo = mediaInfo;
	}

}
