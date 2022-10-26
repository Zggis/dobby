package com.zggis.dobby.services;

public class DoviWindowsProcessBuilderImpl implements DoviProcessBuilder {

	@Override
	public ProcessBuilder get(String cmd) {
		if (!cmd.endsWith("\"")) {
			cmd += "\"";
		}
		ProcessBuilder pb = new ProcessBuilder(cmd);
		return pb;
	}

}
