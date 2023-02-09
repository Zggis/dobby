package com.zggis.dobby.dto.batch;

import java.io.Serializable;

public class VideoInjectionDTO implements Serializable {

    private final HevcFileDTO standardHevcFile;

    private final RPUFileDTO rpuFile;

    public VideoInjectionDTO(HevcFileDTO standardHevcFile, RPUFileDTO rpuFile) {
        super();
        this.standardHevcFile = standardHevcFile;
        this.rpuFile = rpuFile;
    }

    public HevcFileDTO getStandardHevcFile() {
        return standardHevcFile;
    }

    public RPUFileDTO getRpuFile() {
        return rpuFile;
    }

}
