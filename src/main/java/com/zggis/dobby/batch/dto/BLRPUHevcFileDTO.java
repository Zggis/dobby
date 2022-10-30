package com.zggis.dobby.batch.dto;

import com.zggis.dobby.dto.BorderInfoDTO;

public class BLRPUHevcFileDTO extends HevcFileDTO {

	private BorderInfoDTO borderInfo;

	public BLRPUHevcFileDTO(String name, String key, boolean dolbyVision) {
		super(name, key, dolbyVision);
	}

	public BorderInfoDTO getBorderInfo() {
		return borderInfo;
	}

	public void setBorderInfo(BorderInfoDTO borderInfo) {
		this.borderInfo = borderInfo;
	}

}
