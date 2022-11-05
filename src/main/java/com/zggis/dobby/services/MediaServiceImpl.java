package com.zggis.dobby.services;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.zggis.dobby.batch.ConsoleColor;

public abstract class MediaServiceImpl implements MediaService {

	private static final Logger logger = LoggerFactory.getLogger(MediaServiceImpl.class);

	@Value("${media.dir}")
	private String mediaDir;

	@Value("${results.dir}")
	private String resultsDir;

	@Value("${temp.dir}")
	private String tempDir;

	@Value("${cleanup:true}")
	private boolean cleanup;

	protected abstract void createDirectory(String directory);

	protected boolean doesDirectoryExist(String directory) {
		File dir = new File(directory);
		return dir != null && dir.exists();
	}

	@Override
	public String getMediaDirectory() {
		return mediaDir;
	}

	@Override
	public String getTempDirectory() {
		return tempDir + "/";
	}

	@Override
	public String getResultsDirectory() {
		return resultsDir + "/";
	}

	@Override
	public void createResultsDirectory() {
		createDirectory(resultsDir);
	}

	@Override
	public void createTempDirectory() {
		createDirectory(tempDir);
	}

	@Override
	public void deleteTempDirectory() {
		if (cleanup) {
			logger.info("Cleaning up TEMP directory...");
			deleteDirectory(tempDir);
		} else {
			logger.info("Cleanup is disabled by configuration.");
		}
	}

	@Override
	public boolean isCleanup() {
		return cleanup;
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
