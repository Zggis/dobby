package com.zggis.dobby.dto.batch;

import java.io.Serializable;

public class VideoMergeDTO implements Serializable {

    private final VideoFileDTO standardFile;

    private final BLRPUHevcFileDTO blRPUFile;

    private boolean valid = true;

    public VideoMergeDTO(VideoFileDTO standardFile, BLRPUHevcFileDTO blRPUFile) {
        super();
        this.standardFile = standardFile;
        this.blRPUFile = blRPUFile;
    }

    public VideoFileDTO getStandardFile() {
        return standardFile;
    }

    public BLRPUHevcFileDTO getBlRPUFile() {
        return blRPUFile;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
