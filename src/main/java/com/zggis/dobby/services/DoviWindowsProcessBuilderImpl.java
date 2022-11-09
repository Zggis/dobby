package com.zggis.dobby.services;

public class DoviWindowsProcessBuilderImpl implements DoviProcessBuilder {

    @Override
    public ProcessBuilder get(String cmd) {
        if (!cmd.endsWith("\"")) {
            cmd += "\"";
        }
        return new ProcessBuilder(cmd);
    }

}
