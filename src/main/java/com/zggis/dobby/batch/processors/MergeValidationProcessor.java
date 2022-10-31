package com.zggis.dobby.batch.processors;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.zggis.dobby.batch.ConsoleColor;
import com.zggis.dobby.batch.JobUtils;
import com.zggis.dobby.dto.batch.VideoMergeDTO;

public class MergeValidationProcessor implements ItemProcessor<VideoMergeDTO, VideoMergeDTO> {

	private static final Logger logger = LoggerFactory.getLogger(MergeValidationProcessor.class);

	private boolean execute;

	public MergeValidationProcessor(boolean execute) {
		this.execute = execute;
	}

	@Override
	public VideoMergeDTO process(VideoMergeDTO conversion) throws IOException {
		logger.info("Validating merge of {} and {}", JobUtils.getWithoutPath(conversion.getBlRPUFile().getName()),
				JobUtils.getWithoutPath(conversion.getStandardFile().getName()));
		if (execute == false) {
			return conversion;
		}
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
		return conversion;
	}

}
