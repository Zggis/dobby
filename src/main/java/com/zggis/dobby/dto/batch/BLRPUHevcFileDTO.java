package com.zggis.dobby.dto.batch;

import com.zggis.dobby.dto.BorderInfoDTO;
import com.zggis.dobby.dto.mediainfo.MediaInfoDTO;

public class BLRPUHevcFileDTO extends HevcFileDTO {

    private BorderInfoDTO borderInfo;

    public BLRPUHevcFileDTO(String name, String key, MediaInfoDTO mediaInfo) {
        super(name, key, mediaInfo);
    }

    public BorderInfoDTO getBorderInfo() {
        return borderInfo;
    }

    public void setBorderInfo(BorderInfoDTO borderInfo) {
        this.borderInfo = borderInfo;
    }

}
