package com.zggis.dobby.batch.processors;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.zggis.dobby.batch.ConsoleColor;
import com.zggis.dobby.batch.JobUtils;
import com.zggis.dobby.dto.batch.HevcFileDTO;
import com.zggis.dobby.dto.batch.VideoFileDTO;
import com.zggis.dobby.services.DoviProcessBuilder;

public class ConvertToHevcProcessor implements ItemProcessor<VideoFileDTO, HevcFileDTO> {

	private static final Logger logger = LoggerFactory.getLogger(ConvertToHevcProcessor.class);

	private String MP4EXTRACT;

	private String MKVEXTRACT;

	private String outputDir;

	private DoviProcessBuilder pbservice;

	private boolean execute;

	public ConvertToHevcProcessor(DoviProcessBuilder pbservice, String outputDir, String MP4EXTRACT, String MKVEXTRACT,
			boolean execute) {
		this.MP4EXTRACT = MP4EXTRACT;
		this.MKVEXTRACT = MKVEXTRACT;
		this.outputDir = outputDir;
		this.pbservice = pbservice;
		this.execute = execute;
	}

	@Override
	public HevcFileDTO process(VideoFileDTO conversion) throws IOException {
		logger.info("Generating HEVC file from {}...", JobUtils.getWithoutPath(conversion.getName()));
		if (conversion.getName().toLowerCase().endsWith(".mp4")) {
			return processMP4(conversion);
		} else if (conversion.getName().toLowerCase().endsWith(".mkv")) {
			return processMKV(conversion);
		}
		logger.error(ConsoleColor.YELLOW.value + "{} is not an MP4 or MKV file, it will be ignored."
				+ ConsoleColor.NONE.value, conversion.getName());
		return null;
	}

	private HevcFileDTO processMKV(VideoFileDTO file) throws IOException {
		String cmd = MKVEXTRACT + " \"" + file.getName() + "\" tracks 0:\"" + outputDir
				+ JobUtils.getWithoutPathAndExtension(file.getName()) + ".hevc\"";
		logger.debug(cmd);
		ProcessBuilder pb = pbservice.get(cmd);
		boolean dolbyVision = JobUtils.isDolbyVision(file.getMediaInfo());
		pb.redirectErrorStream(true);
		if (execute) {
			Process p = pb.start();
			JobUtils.printOutput(p);
		} else {
			logger.info("===EXECUTION SKIPPED===");
		}
		return new HevcFileDTO(outputDir + JobUtils.getWithoutPathAndExtension(file.getName()) + ".hevc", file.getKey(),
				file.getMediaInfo());
	}

	private HevcFileDTO processMP4(VideoFileDTO file) throws IOException {
		String CMD = MP4EXTRACT + " -raw 1 -out \"" + outputDir + JobUtils.getWithoutPathAndExtension(file.getName())
				+ ".hevc\" \"" + file.getName() + "\"";
		logger.debug(CMD);
		ProcessBuilder pb = pbservice.get(CMD);
		boolean dolbyVision = JobUtils.isDolbyVision(file.getMediaInfo());
		pb.redirectErrorStream(dolbyVision);
		if (execute) {
			Process p = pb.start();
			JobUtils.printOutput(p);
		} else {
			logger.info("===EXECUTION SKIPPED===");
		}
		return new HevcFileDTO(outputDir + JobUtils.getWithoutPathAndExtension(file.getName()) + ".hevc", file.getKey(),
				file.getMediaInfo());
	}
}
