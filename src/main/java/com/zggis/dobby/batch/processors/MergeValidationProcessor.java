package com.zggis.dobby.batch.processors;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.zggis.dobby.batch.ConsoleColor;
import com.zggis.dobby.batch.JobUtils;
import com.zggis.dobby.dto.batch.BLRPUHevcFileDTO;
import com.zggis.dobby.dto.batch.VideoFileDTO;
import com.zggis.dobby.dto.batch.VideoMergeDTO;

public class MergeValidationProcessor implements ItemProcessor<VideoMergeDTO, VideoMergeDTO> {

    private static final Logger logger = LoggerFactory.getLogger(MergeValidationProcessor.class);

    private final boolean execute;

    private final boolean activeAreaValidation;

    public MergeValidationProcessor(boolean execute, boolean activeAreaValidation) {
        this.execute = execute;
        this.activeAreaValidation = activeAreaValidation;
    }

    @Override
    public VideoMergeDTO process(VideoMergeDTO conversion) throws IOException {
        logger.info("Validating merge of {} and {}", JobUtils.getWithoutPath(conversion.getBlRPUFile().getName()),
                JobUtils.getWithoutPath(conversion.getStandardFile().getName()));
        if (!execute) {
            return conversion;
        }
        if (!validateMergeCompatibility(conversion.getStandardFile(), conversion.getBlRPUFile())) {
            conversion.setBlRPUFile(null);
            conversion.setStandardFile(null);
            return conversion;
        }
        if (activeAreaValidation) {
            int rpuActiveHeight = conversion.getBlRPUFile().getBorderInfo().getBottomOffset()
                    + conversion.getBlRPUFile().getBorderInfo().getTopOffset();
            int matches = 0;
            for (int item : conversion.getStandardFile().getActiveArea().getActiveAreaHeights()) {
                if (item == rpuActiveHeight) {
                    matches++;
                }
            }
            Double heightPercent = ((double) matches
                    / (double) conversion.getStandardFile().getActiveArea().getActiveAreaHeights().size()) * 100;
            if (matches < (conversion.getStandardFile().getActiveArea().getActiveAreaHeights().size() * 0.6)) {
                logger.error(
                        ConsoleColor.RED.value + "Active area height failed to pass matching threshold {}%, aborting merge."
                                + ConsoleColor.NONE.value,
                        heightPercent);
                conversion.setBlRPUFile(null);
                conversion.setStandardFile(null);
                return conversion;
            }
            int rpuActiveWidth = conversion.getBlRPUFile().getBorderInfo().getLeftOffset()
                    + conversion.getBlRPUFile().getBorderInfo().getRightOffset();
            int widthMatches = 0;
            for (int item : conversion.getStandardFile().getActiveArea().getActiveAreaWidths()) {
                if (item == rpuActiveWidth) {
                    widthMatches++;
                }
            }
            Double widthPercent = ((double) widthMatches
                    / (double) conversion.getStandardFile().getActiveArea().getActiveAreaWidths().size()) * 100;
            if (widthMatches < (conversion.getStandardFile().getActiveArea().getActiveAreaWidths().size() * 0.6)) {
                logger.error(
                        ConsoleColor.RED.value + "Active area width failed to pass matching threshold {}%, aborting merge."
                                + ConsoleColor.NONE.value,
                        widthPercent);
                conversion.setBlRPUFile(null);
                conversion.setStandardFile(null);
                return conversion;
            }
            logger.info(ConsoleColor.GREEN.value + "Active areas match [Height:{}% Width:{}%], proceeding with merge..."
                    + ConsoleColor.NONE.value, heightPercent.intValue(), widthPercent.intValue());
        } else {
            logger.warn(ConsoleColor.YELLOW.value + "Active area validation is disabled by your environment variable AAVALIDATE" + ConsoleColor.YELLOW.value);
        }
        return conversion;
    }

    private boolean validateMergeCompatibility(VideoFileDTO standardFile, BLRPUHevcFileDTO dolbyVisionFile) {
        String standardResolution = JobUtils.getResolution(standardFile.getMediaInfo());
        String dolbyVisionResolution = JobUtils.getResolution(dolbyVisionFile.getMediaInfo());
        if (standardResolution == null || !standardResolution.equals(dolbyVisionResolution)) {
            logger.error(ConsoleColor.RED.value + "Resolutions do not match, DV:{}, HDR:{}" + ConsoleColor.NONE.value,
                    dolbyVisionResolution, standardResolution);
            return false;
        }
        if ("3840x2160 DL".equals(standardResolution)) {
            logger.error(ConsoleColor.RED.value + "{} cannot support double layer profile 7" + ConsoleColor.NONE.value,
                    JobUtils.getWithoutPath(standardFile.getName()));
            return false;
        }
        String hdrFormat = JobUtils.getHDRFormat(standardFile.getMediaInfo());
        if (hdrFormat == null) {
            logger.error(ConsoleColor.RED.value + "No HDR format detected for {}" + ConsoleColor.NONE.value,
                    JobUtils.getWithoutPath(standardFile.getName()));
            return false;
        }
        String frameRate = JobUtils.getFrameRate(standardFile.getMediaInfo());
        if (frameRate == null) {
            logger.error(ConsoleColor.RED.value + "Could not determine Frame Rate of {}" + ConsoleColor.NONE.value,
                    JobUtils.getWithoutPath(standardFile.getName()));
            return false;
        }
        logger.info(
                "Resolution:\t{}\t" + ConsoleColor.GREEN.value + "GOOD" + ConsoleColor.NONE.value
                        + "\nHDR Format:\t{}\t" + ConsoleColor.GREEN.value + "GOOD" + ConsoleColor.NONE.value
                        + "\nFrame Rate:\t{}\t\t" + ConsoleColor.GREEN.value + "GOOD" + ConsoleColor.NONE.value,
                standardResolution, hdrFormat, frameRate);
        return true;
    }

}
