package com.zggis.dobby.batch.dto;

import com.zggis.dobby.dto.BorderInfoDTO;

public class RPUFileDTO extends FileDTO {

	private BorderInfoDTO borderInfo;

	public RPUFileDTO(String name, String key) {
		this.name = name;
		this.key = key;
	}

	public BorderInfoDTO getBorderInfo() {
		return borderInfo;
	}

	public void setBorderInfo(BorderInfoDTO borderInfo) {
		this.borderInfo = borderInfo;
	}

}
