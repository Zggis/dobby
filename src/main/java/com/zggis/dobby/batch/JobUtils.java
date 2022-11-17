package com.zggis.dobby.batch;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.zggis.dobby.dto.mediainfo.MediaInfoDTO;
import com.zggis.dobby.dto.mediainfo.TrackDTO;

public class JobUtils {

    private static final Logger logger = LoggerFactory.getLogger(JobUtils.class);

    private static final Pattern EPISODE_NUM_REGEX = Pattern.compile("^.*?s(\\d{2})((?:e\\d{2})+).*");

    public static void printOutput(Process p) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (InputStreamReader isr = new InputStreamReader(p.getInputStream())) {
            int c;
            while ((c = isr.read()) >= 0) {
                builder.append((char) c);
            }
        }
        try {
            p.waitFor();
            logger.info(ConsoleColor.BLUE.value + "{}" + ConsoleColor.NONE.value,
                    builder.toString().replaceAll("\n", ConsoleColor.NONE.value + "\n" + ConsoleColor.BLUE.value));
        } catch (InterruptedException e) {
            logger.error(ConsoleColor.RED.value + "{}" + ConsoleColor.NONE.value, e.getMessage());
        }
    }

    public static String getWithoutPathAndExtension(String fullFilename) {
        return fullFilename.substring(fullFilename.lastIndexOf('/') + 1, fullFilename.lastIndexOf('.'));
    }

    public static String getWithoutPath(String fullFilename) {
        return fullFilename.substring(fullFilename.lastIndexOf('/') + 1);
    }

    public static boolean doesMediaFileExists(String filename) {
        File file = new File(filename);
        if (file.exists() && !file.isDirectory()) {
            Path path = Paths.get(filename);
            try {
                long bytes = Files.size(path);
                return bytes > 10000;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static String returnOutput(Process p) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (InputStreamReader isr = new InputStreamReader(p.getInputStream())) {
            int c;
            while ((c = isr.read()) >= 0) {
                builder.append((char) c);
            }
        }
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
        return builder.toString();
    }

    public static String getFrameRate(MediaInfoDTO mediaInfo) {
        for (TrackDTO track : mediaInfo.media.track) {
            if ("1".equals(track.iD)) {
                return track.frameRate;
            }
        }
        return null;
    }

    public static String getResolution(MediaInfoDTO mediaInfo) {
        for (TrackDTO track : mediaInfo.media.track) {
            if ("1".equals(track.iD)) {
                String result = track.width + "x" + track.height;
                if ("3840x21601920x1080".equals(result)) {
                    return "3840x2160 DL";
                } else {
                    return result;
                }
            }
        }
        return null;
    }

    public static String getHDRFormat(MediaInfoDTO mediaInfo) {
        for (TrackDTO track : mediaInfo.media.track) {
            if ("1".equals(track.iD)) {
                return track.hDR_Format;
            }
        }
        return null;
    }

    public static boolean isHDR(MediaInfoDTO mediaInfo) {
        for (TrackDTO track : mediaInfo.media.track) {
            if ("1".equals(track.iD) || "Video".equalsIgnoreCase(track.type)) {
                return StringUtils.hasText(track.hDR_Format);
            }
        }
        return false;
    }

    public static boolean isDolbyVision(MediaInfoDTO mediaInfo) {
        for (TrackDTO track : mediaInfo.media.track) {
            if ("1".equals(track.iD) || "Video".equalsIgnoreCase(track.type)) {
                return track.hDR_Format != null && track.hDR_Format.contains("Dolby Vision");
            }
        }
        return false;
    }

    public static boolean isBLRPU(MediaInfoDTO mediaInfo) {
        for (TrackDTO track : mediaInfo.media.track) {
            if ("1".equals(track.iD) || "Video".equalsIgnoreCase(track.type)) {
                return track.hDR_Format != null && track.hDR_Format.contains("Dolby Vision") && track.hDR_Format.contains("SMPTE ST 2086");
            }
        }
        return false;
    }

    public static int getFrameCount(MediaInfoDTO mediaInfo) {
        for (TrackDTO track : mediaInfo.media.track) {
            if ("1".equals(track.iD) || "Video".equalsIgnoreCase(track.type)) {
                if (StringUtils.hasText(track.frameCount)) {
                    return Integer.parseInt(track.frameCount);
                } else {
                    return -1;
                }
            }
        }
        return -1;
    }

    public static double getDuration(MediaInfoDTO mediaInfo) {
        double duration = -1;
        for (TrackDTO track : mediaInfo.media.track) {
            if ("1".equals(track.iD)) {
                duration = Double.parseDouble(track.duration);
            }
        }
        return duration;
    }

    public static Collection<String> getTitleMatches(String name1, String name2) {
        Collection<String> name1Tokens = new HashSet<>();
        Collection<String> name2Tokens = new HashSet<>();
        for (String str : name1.split("\\.")) {
            Matcher m = EPISODE_NUM_REGEX.matcher(str.toLowerCase());
            if (!m.find() && !str.equalsIgnoreCase("2160p") && !str.equalsIgnoreCase("1080p")) {
                name1Tokens.add(str);
            } else {
                break;
            }
        }
        for (String str : name2.split("\\.")) {
            Matcher m = EPISODE_NUM_REGEX.matcher(str.toLowerCase());
            if (!m.find() && !str.equalsIgnoreCase("2160p") && !str.equalsIgnoreCase("1080p")) {
                name2Tokens.add(str);
            } else {
                break;
            }
        }
        name1Tokens.retainAll(name2Tokens);
        return name1Tokens;
    }

}
