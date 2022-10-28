package com.zggis.dobby.batch.readers;

import java.io.File;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.zggis.dobby.dto.VideoFileDTO;

public class StandardDiskFileReader implements ItemReader<String> {

	private static final Logger logger = LoggerFactory.getLogger(StandardDiskFileReader.class);

	private static final Pattern EPISODE_NUM_REGEX = Pattern.compile("^.*?s(\\d{2})((?:e\\d{2})+).*");

	private static final Pattern DOLBY_VISION_REGEX = Pattern.compile("^.*?dv.*");

	Stack<String> standardFileNames = new Stack<>();

	public StandardDiskFileReader(String mediaDir) {
		File dir = new File(mediaDir);
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				if (child.getName().endsWith(".mkv")) {
					logger.debug("Checking {}", child.getName());
					Matcher m = EPISODE_NUM_REGEX.matcher(child.getName().toLowerCase());
					VideoFileDTO newFile = new VideoFileDTO();
					newFile.setFullName(child.getName());
					if (m.find()) {
						newFile.setSeason(m.group(1));
						newFile.setEpisode(m.group(2).substring(1, m.group(2).length()).replace("e", "-"));
					}
					Matcher m2 = DOLBY_VISION_REGEX.matcher(child.getName().toLowerCase());
					if (m2.find()) {
						newFile.setDolbyVision(true);
					}
					if (newFile.getSeason() != null && newFile.getEpisode() != null) {
						if (!newFile.isDolbyVision()) {
							logger.debug("Added file {}", child.getName());
							standardFileNames.push(newFile.getFullName());
						}
					}
				}
			}
		}
	}

	@Override
	public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		if (!standardFileNames.isEmpty()) {
			return standardFileNames.pop();
		}
		return null;
	}

}
