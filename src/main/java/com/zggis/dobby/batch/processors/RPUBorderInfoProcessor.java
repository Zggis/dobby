package com.zggis.dobby.batch.processors;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.zggis.dobby.batch.JobUtils;
import com.zggis.dobby.batch.RPUFileDTO;
import com.zggis.dobby.dto.BorderInfoDTO;
import com.zggis.dobby.services.DoviProcessBuilder;

public class RPUBorderInfoProcessor implements ItemProcessor<RPUFileDTO, RPUFileDTO> {

	private static final Logger logger = LoggerFactory.getLogger(RPUBorderInfoProcessor.class);

	private String DOVI_TOOL;

	private DoviProcessBuilder pbservice;

	private boolean execute;

	public RPUBorderInfoProcessor(DoviProcessBuilder pbservice, String DOVI_TOOL, boolean execute) {
		this.DOVI_TOOL = DOVI_TOOL;
		this.pbservice = pbservice;
		this.execute = execute;
	}

	@Override
	public RPUFileDTO process(RPUFileDTO rpuFile) throws IOException {
		logger.info("Fetching Border info from {}...", rpuFile.getName());
		String CMD = DOVI_TOOL + " info -f 123 -i \"" + rpuFile.getName() + "\"";
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
			result.setBottomOffset(0);
			result.setTopOffset(0);
			result.setLeftOffset(0);
			result.setRightOffset(0);
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
		rpuFile.setBorderInfo(result);
		return rpuFile;
	}

}
