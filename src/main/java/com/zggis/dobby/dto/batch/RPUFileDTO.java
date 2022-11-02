package com.zggis.dobby.dto.batch;

import com.zggis.dobby.dto.BorderInfoDTO;
import com.zggis.dobby.dto.mediainfo.MediaInfoDTO;

public class RPUFileDTO extends FileDTO {

	private BorderInfoDTO borderInfo;

	private MediaInfoDTO mediaInfo;

	public RPUFileDTO(String name, String key, MediaInfoDTO mediaInfo) {
		this.name = name;
		this.key = key;
		this.setMediaInfo(mediaInfo);
	}

	public BorderInfoDTO getBorderInfo() {
		return borderInfo;
	}

	public void setBorderInfo(BorderInfoDTO borderInfo) {
		this.borderInfo = borderInfo;
	}

	public MediaInfoDTO getMediaInfo() {
		return mediaInfo;
	}

	public void setMediaInfo(MediaInfoDTO mediaInfo) {
		this.mediaInfo = mediaInfo;
	}

}
