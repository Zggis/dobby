package com.zggis.dobby.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinuxMediaServiceImpl extends MediaServiceImpl implements MediaService {

	private static final Logger logger = LoggerFactory.getLogger(LinuxMediaServiceImpl.class);

	protected void createDirectory(String directoryStr) {
		try {
			Files.createDirectory(Paths.get(directoryStr));
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

	}
}
