package com.zggis.dobby.dto.batch;

import com.zggis.dobby.dto.mediainfo.MediaInfoDTO;

public class HevcFileDTO extends FileDTO {

    private final MediaInfoDTO mediaInfo;

    public HevcFileDTO(String name, String key, MediaInfoDTO mediaInfo) {
        this.name = name;
        this.key = key;
        this.mediaInfo = mediaInfo;
    }

    public MediaInfoDTO getMediaInfo() {
        return mediaInfo;
    }

}
