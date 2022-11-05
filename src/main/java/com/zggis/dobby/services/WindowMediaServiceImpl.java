package com.zggis.dobby.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindowMediaServiceImpl extends MediaServiceImpl implements MediaService {

	private static final Logger logger = LoggerFactory.getLogger(WindowMediaServiceImpl.class);

	protected void createDirectory(String directoryStr) {
		if (!doesDirectoryExist(directoryStr)) {
			try {
				Files.createDirectory(Paths.get(directoryStr));
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
}
