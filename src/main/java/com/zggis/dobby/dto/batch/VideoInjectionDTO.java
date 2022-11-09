package com.zggis.dobby.dto.batch;

public class VideoInjectionDTO {

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
