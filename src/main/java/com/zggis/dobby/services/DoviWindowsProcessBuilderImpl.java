package com.zggis.dobby.services;

public class DoviWindowsProcessBuilderImpl implements DoviProcessBuilder {

	@Override
	public ProcessBuilder get(String cmd) {
		ProcessBuilder pb = new ProcessBuilder(cmd);
		return pb;
	}

}
