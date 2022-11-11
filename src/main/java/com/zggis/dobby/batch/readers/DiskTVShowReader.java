package com.zggis.dobby.batch.readers;

import com.zggis.dobby.batch.ConsoleColor;
import com.zggis.dobby.dto.batch.VideoFileDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.io.File;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiskTVShowReader implements ItemReader<VideoFileDTO> {

    private static final Logger logger = LoggerFactory.getLogger(DiskTVShowReader.class);

    private static final Pattern EPISODE_NUM_REGEX = Pattern.compile("^.*?s(\\d{2})((?:e\\d{2})+).*");

    Stack<VideoFileDTO> mediaFiles = new Stack<>();

    public DiskTVShowReader(String mediaDir) {
        logger.info("Scanning {} for TV shows...", mediaDir);
        File dir = new File(mediaDir);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if (child.getName().endsWith(".mkv") || child.getName().endsWith(".mp4")) {
                    logger.debug("Checking {}", child.getName());
                    Matcher m = EPISODE_NUM_REGEX.matcher(child.getName().toLowerCase());
                    VideoFileDTO newFile;
                    if (m.find()) {
                        String key = m.group(1) + m.group(2).substring(1).replace("e", "-");
                        newFile = new VideoFileDTO(mediaDir + "/" + child.getName(), key);
                    } else {
                        newFile = new VideoFileDTO(mediaDir + "/" + child.getName());
                    }
                    mediaFiles.push(newFile);
                } else {
                    logger.warn(ConsoleColor.YELLOW.value + "{} is not a MP4 or MKV file, it wil be ignored."
                            + ConsoleColor.NONE.value, child.getName());
                }
            }
        } else {
            logger.error(ConsoleColor.RED.value + "{} Does not exist." + ConsoleColor.NONE.value, mediaDir);
        }
    }

    @Override
    public VideoFileDTO read()
            throws UnexpectedInputException, ParseException, NonTransientResourceException {
        if (!mediaFiles.isEmpty()) {
            return mediaFiles.pop();
        }
        return null;
    }

}
