package com.zggis.dobby.batch.processors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.zggis.dobby.batch.JobUtils;
import com.zggis.dobby.dto.ActiveAreaDTO;
import com.zggis.dobby.services.DoviProcessBuilder;

public class MKVActiveAreaProcessor implements ItemProcessor<String, ActiveAreaDTO> {

	private static final Logger logger = LoggerFactory.getLogger(MKVActiveAreaProcessor.class);

	private String FFMPEG;

	private DoviProcessBuilder pbservice;

	private String mediaDir;

	private boolean execute;

	public MKVActiveAreaProcessor(DoviProcessBuilder pbservice, String mediaDir, String FFMPEG, boolean execute) {
		this.FFMPEG = FFMPEG;
		this.pbservice = pbservice;
		this.mediaDir = mediaDir;
		this.execute = execute;
	}

	@Override
	public ActiveAreaDTO process(String standardFilename) throws IOException {
		logger.info("Fetching Border info from {}...", standardFilename);
		double duration = 1500; // Need to figure out how to get actual duration

		List<Integer> activeAreaHeights = new ArrayList<>();
		List<Integer> activeAreaWidths = new ArrayList<>();
		if (!execute) {
			activeAreaHeights.add(-1);
			activeAreaWidths.add(-1);
			logger.info("===EXECUTION SKIPPED===");
		}
		for (double i = 0.2; i <= 0.8 && execute; i += 0.1) {
			String CMD = FFMPEG + " -ss 00:" + (int) ((duration * i) / 60) + ":00 -i \"" + mediaDir + standardFilename
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
						activeAreaHeights.add(Integer.parseInt(strBotVal));
						activeAreaWidths.add(Integer.parseInt(strLeftVal));
						logger.debug("Adding {} as bottom value", strBotVal);
						logger.debug("Adding {} as left value", strLeftVal);
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
		return result;
	}

}
