package com.zggis.dobby.services;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.zggis.dobby.batch.ConsoleColor;

@Component
public class MediaServiceImpl {

	private static final Logger logger = LoggerFactory.getLogger(MediaServiceImpl.class);

	@Value("${media.dir}")
	private String mediaDir;

	@Value("${results.dir}")
	private String resultsDir;

	@Value("${temp.dir}")
	private String tempDir;

	@Value("${cleanup:true}")
	private boolean cleanup;

	public String getMediaDirectory() {
		return mediaDir;
	}

	public String getTempDirectory() {
		return tempDir + "/";
	}

	public String getResultsDirectory() {
		return resultsDir + "/";
	}

	public void createTempDirectory() {
		createDirectory(tempDir);
	}

	public void deleteTempDirectory() {
		if (cleanup) {
			logger.info("Cleaning up TEMP directory...");
			deleteDirectory(tempDir);
		} else {
			logger.info("Cleanup is disabled by configuration.");
		}
	}

	public boolean isCleanup() {
		return cleanup;
	}

	private static void createDirectory(String directoryStr) {
		File directory = new File(directoryStr);
		if (!directory.exists()) {
			directory.mkdirs();
		}
	}

	private void deleteDirectory(String directoryName) {
		File directory = new File(directoryName);
		boolean dirEmpty = true;
		if (directory.exists()) {
			File[] directoryListing = directory.listFiles();
			if (directoryListing != null) {
				for (File file : directoryListing) {
					logger.warn(ConsoleColor.YELLOW.value
							+ "This file {} was found in the TEMP directory {}, but Dobby did not put it there. It will not be touched."
							+ ConsoleColor.NONE.value, file.getName(), tempDir);
					dirEmpty = false;
				}
			}
			if (dirEmpty) {
				directory.delete();
			}
		}
	}
}
