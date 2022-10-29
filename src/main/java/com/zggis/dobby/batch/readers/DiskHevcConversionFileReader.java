package com.zggis.dobby.batch.readers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.zggis.dobby.batch.HevcVideoConversion;
import com.zggis.dobby.batch.VideoFileDTO;

public class DiskHevcConversionFileReader implements ItemReader<HevcVideoConversion> {

	private static final Logger logger = LoggerFactory.getLogger(DiskHevcConversionFileReader.class);

	private static final Pattern EPISODE_NUM_REGEX = Pattern.compile("^.*?s(\\d{2})((?:e\\d{2})+).*");
	
	//private static final Pattern DOLBY_VISION_REGEX = Pattern.compile("^.*?dv.*");
	//Matcher m2 = DOLBY_VISION_REGEX.matcher(child.getName().toLowerCase());

	Stack<HevcVideoConversion> conversions = new Stack<>();

	public DiskHevcConversionFileReader(String mediaDir) {
		Map<String, VideoFileDTO> dvShowMap = new HashMap<>();
		Map<String, VideoFileDTO> showMap = new HashMap<>();
		logger.info("Scanning {} for TV shows...", mediaDir);
		File dir = new File(mediaDir);
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				if (child.getName().endsWith(".mkv") || child.getName().endsWith(".mp4")) {
					logger.debug("Checking {}", child.getName());
					Matcher m = EPISODE_NUM_REGEX.matcher(child.getName().toLowerCase());
					if (m.find()) {
						String key = m.group(1) + m.group(2).substring(1, m.group(2).length()).replace("e", "-");
						VideoFileDTO newFile = new VideoFileDTO(mediaDir + "/" + child.getName(), key);
						if (child.getName().endsWith(".mp4")) {
							dvShowMap.put(key, newFile);
						} else {
							showMap.put(key, newFile);
						}
					}
				}
			}
		} else {
			logger.error("{} Does not exist.", mediaDir);
		}
		for (String key : showMap.keySet()) {
			VideoFileDTO dolbyVisionFile = dvShowMap.get(key);
			if (dolbyVisionFile != null) {
				HevcVideoConversion conversion = new HevcVideoConversion(key, showMap.get(key), dolbyVisionFile);
				conversions.push(conversion);
			} else {
				logger.warn("Cannot find Dolby Vision file for {}", showMap.get(key));
			}
		}
	}

	@Override
	public HevcVideoConversion read()
			throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		if (!conversions.isEmpty()) {
			return conversions.pop();
		}
		return null;
	}

}
