package com.zggis.dobby.batch.processors;

import com.zggis.dobby.batch.ConsoleColor;
import com.zggis.dobby.batch.JobUtils;
import com.zggis.dobby.dto.BorderInfoDTO;
import com.zggis.dobby.dto.batch.RPUFileDTO;
import com.zggis.dobby.services.DoviProcessBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.io.IOException;

public class RPUBorderInfoProcessor implements ItemProcessor<RPUFileDTO, RPUFileDTO> {

    private static final Logger logger = LoggerFactory.getLogger(RPUBorderInfoProcessor.class);

    private final String DOVI_TOOL;

    private final DoviProcessBuilder pbservice;

    private final boolean execute;

    public RPUBorderInfoProcessor(DoviProcessBuilder pbservice, String DOVI_TOOL, boolean execute) {
        this.DOVI_TOOL = DOVI_TOOL;
        this.pbservice = pbservice;
        this.execute = execute;
    }

    @Override
    public RPUFileDTO process(RPUFileDTO rpuFile) throws IOException {
        logger.info("Fetching Border info from {}...", JobUtils.getWithoutPath(rpuFile.getName()));
        int frameCount = JobUtils.getFrameCount(rpuFile.getMediaInfo());
        String CMD = DOVI_TOOL + " info -f " + (frameCount / 2) + " -i \"" + rpuFile.getName() + "\"";
        logger.debug(CMD);
        ProcessBuilder pb = pbservice.get(CMD);
        pb.redirectErrorStream(true);
        String[] lines = new String[0];
        BorderInfoDTO result = new BorderInfoDTO();
        if (execute) {
            Process p = pb.start();
            String infoOutput = JobUtils.returnOutput(p);
            lines = infoOutput.split("\n");
        } else {
            logger.info("===EXECUTION SKIPPED===");
        }
        for (String line : lines) {
            try {
                if (line.contains("active_area_top_offset")) {
                    if (line.split(":").length < 2) {
                        logger.error("No value for active_area_top_offset");
                        return null;
                    }
                    String strVal = line.split(":")[1].replaceAll(",", "").trim();
                    result.setTopOffset(Integer.parseInt(strVal));
                } else if (line.contains("active_area_bottom_offset")) {
                    if (line.split(":").length < 2) {
                        logger.error("No value for active_area_bottom_offset");
                        return null;
                    }
                    String strVal = line.split(":")[1].replaceAll(",", "").trim();
                    result.setBottomOffset(Integer.parseInt(strVal));
                } else if (line.contains("active_area_left_offset")) {
                    if (line.split(":").length < 2) {
                        logger.error("No value for active_area_left_offset");
                        return null;
                    }
                    String strVal = line.split(":")[1].replaceAll(",", "").trim();
                    result.setLeftOffset(Integer.parseInt(strVal));
                } else if (line.contains("active_area_right_offset")) {
                    if (line.split(":").length < 2) {
                        logger.error("No value for active_area_right_offset");
                        return null;
                    }
                    String strVal = line.split(":")[1].replaceAll(",", "").trim();
                    result.setRightOffset(Integer.parseInt(strVal));
                }
            } catch (NumberFormatException e) {
                logger.error("Cannot parse offset value");
                return null;
            }
        }
        logger.debug("Border info for {} is Left {}, Right {}, Top {}, Bottom {}", rpuFile.getName(), result.getLeftOffset(), result.getRightOffset(), result.getTopOffset(), result.getBottomOffset());
        rpuFile.setBorderInfo(result);
        logger.info(ConsoleColor.GREEN.value + "Added border info!" + ConsoleColor.NONE.value);
        return rpuFile;
    }

}
