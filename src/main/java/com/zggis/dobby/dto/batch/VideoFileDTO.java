package com.zggis.dobby.dto.batch;

import com.zggis.dobby.dto.ActiveAreaDTO;
import com.zggis.dobby.dto.mediainfo.MediaInfoDTO;

import java.io.Serializable;

public class VideoFileDTO extends FileDTO {

    private ActiveAreaDTO activeArea;

    private MediaInfoDTO mediaInfo;

    public VideoFileDTO(String name, String key) {
        this.name = name;
        this.key = key;
    }

    public VideoFileDTO(String name) {
        this.name = name;
    }

    public ActiveAreaDTO getActiveArea() {
        return activeArea;
    }

    public void setActiveArea(ActiveAreaDTO activeArea) {
        this.activeArea = activeArea;
    }

    public MediaInfoDTO getMediaInfo() {
        return mediaInfo;
    }

    public void setMediaInfo(MediaInfoDTO mediaInfo) {
        this.mediaInfo = mediaInfo;
    }

}
