package com.zggis.dobby.services;

public class DoviLinuxProcessBuilderImpl implements DoviProcessBuilder {

	@Override
	public ProcessBuilder get(String cmd) {
		ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", cmd);
		return pb;
	}

}
