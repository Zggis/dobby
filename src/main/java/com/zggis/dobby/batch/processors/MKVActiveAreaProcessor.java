package com.zggis.dobby.batch.processors;

import com.zggis.dobby.batch.ConsoleColor;
import com.zggis.dobby.batch.JobUtils;
import com.zggis.dobby.dto.ActiveAreaDTO;
import com.zggis.dobby.dto.batch.VideoFileDTO;
import com.zggis.dobby.services.DoviProcessBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MKVActiveAreaProcessor implements ItemProcessor<VideoFileDTO, VideoFileDTO> {

    private static final Logger logger = LoggerFactory.getLogger(MKVActiveAreaProcessor.class);

    private final String FFMPEG;

    private final DoviProcessBuilder pbservice;

    private final boolean execute;

    public MKVActiveAreaProcessor(DoviProcessBuilder pbservice, String FFMPEG, boolean execute) {
        this.FFMPEG = FFMPEG;
        this.pbservice = pbservice;
        this.execute = execute;
    }

    @Override
    public VideoFileDTO process(VideoFileDTO file) throws IOException {
        if (!JobUtils.isDolbyVision(file.getMediaInfo())) {
            logger.info("Fetching active area info from {}...", JobUtils.getWithoutPath(file.getName()));
            double duration = JobUtils.getDuration(file.getMediaInfo());
            List<Integer> activeAreaHeights = new ArrayList<>();
            List<Integer> activeAreaWidths = new ArrayList<>();
            if (!execute) {
                activeAreaHeights.add(-1);
                activeAreaWidths.add(-1);
                logger.info("===EXECUTION SKIPPED===");
            }
            int heightSum = 0;
            int widthSum = 0;
            for (double i = 0.2; i <= 0.8 && execute; i += 0.1) {
                String CMD = FFMPEG + " -ss 00:" + (int) ((duration * i) / 60) + ":00 -i \"" + file.getName()
                        + "\" -vf cropdetect -frames:v 400 -f null -";
                logger.debug(CMD);
                ProcessBuilder pb = pbservice.get(CMD);
                pb.redirectErrorStream(true);
                Process p = pb.start();
                String infoOutput = JobUtils.returnOutput(p);
                String[] lines = infoOutput.split("\n");
                for (String line : lines) {
                    try {
                        if (line.startsWith("[Parsed_cropdetect")) {
                            String[] tokens = line.split(" ");
                            if (tokens.length < 6) {
                                logger.error("No value for Parsed_cropdetect");
                                return null;
                            }
                            String strBotVal = tokens[5].split(":")[1].trim();
                            String strLeftVal = tokens[3].split(":")[1].trim();
                            activeAreaHeights.add(Integer.parseInt(strBotVal) * 2);
                            heightSum += Integer.parseInt(strBotVal) * 2;
                            activeAreaWidths.add(Integer.parseInt(strLeftVal) * 2);
                            widthSum += Integer.parseInt(strLeftVal) * 2;
                            logger.trace("Adding {} as bottom value", strBotVal);
                            logger.trace("Adding {} as left value", strLeftVal);
                        }
                    } catch (NumberFormatException e) {
                        logger.error("Cannot parse offset value");
                        return null;
                    }
                }
            }
            ActiveAreaDTO result = new ActiveAreaDTO();
            result.setActiveAreaHeights(activeAreaHeights);
            result.setActiveAreaWidths(activeAreaWidths);
            if (!CollectionUtils.isEmpty(result.getActiveAreaHeights()) && !CollectionUtils.isEmpty(result.getActiveAreaWidths())) {
                logger.debug("Active Area info for {}, avg Height {}, avg Width {}", file.getName(), heightSum / result.getActiveAreaHeights().size(), widthSum / result.getActiveAreaWidths().size());
            } else {
                logger.error(ConsoleColor.RED.value + "Could not process active area output for {}" + ConsoleColor.NONE.value, file.getName());
            }
            file.setActiveArea(result);
        }
        return file;
    }

}
