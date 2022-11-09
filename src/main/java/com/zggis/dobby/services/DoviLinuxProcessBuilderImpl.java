package com.zggis.dobby.services;

public class DoviLinuxProcessBuilderImpl implements DoviProcessBuilder {

    @Override
    public ProcessBuilder get(String cmd) {
        return new ProcessBuilder("/bin/bash", "-c", cmd);
    }

}
