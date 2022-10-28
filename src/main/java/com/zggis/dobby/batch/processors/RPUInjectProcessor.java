package com.zggis.dobby.batch.processors;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.zggis.dobby.batch.FileDTO;
import com.zggis.dobby.batch.HevcFileDTO;
import com.zggis.dobby.batch.JobUtils;
import com.zggis.dobby.batch.VideoInjectionDTO;
import com.zggis.dobby.services.DoviProcessBuilder;

public class RPUInjectProcessor implements ItemProcessor<VideoInjectionDTO, FileDTO> {

	private static final Logger logger = LoggerFactory.getLogger(RPUInjectProcessor.class);

	private String DOVI_TOOL;

	private String outputDir;

	private DoviProcessBuilder pbservice;

	private boolean execute;

	public RPUInjectProcessor(DoviProcessBuilder pbservice, String outputDir, String DOVI_TOOL, boolean execute) {
		this.DOVI_TOOL = DOVI_TOOL;
		this.outputDir = outputDir;
		this.pbservice = pbservice;
		this.execute = execute;
	}

	@Override
	public FileDTO process(VideoInjectionDTO injectDTO) throws IOException {
		logger.info("Injecting {} into {}", injectDTO.getRpuFile().getName(),
				injectDTO.getStandardHevcFile().getName());
		String cmd = DOVI_TOOL + " inject-rpu -i \"" + injectDTO.getStandardHevcFile().getName() + "\" --rpu-in \""
				+ injectDTO.getRpuFile().getName() + "\" -o \"" + outputDir
				+ JobUtils.getWithoutPathAndExtension(injectDTO.getStandardHevcFile().getName()) + "[BL+RPU].hevc\"";
		logger.debug(cmd);
		ProcessBuilder pb = pbservice.get(cmd);
		pb.redirectErrorStream(true);
		if (execute) {
			Process p = pb.start();
			JobUtils.printOutput(p);
		} else {
			logger.info("===EXECUTION SKIPPED===");
		}
		return new HevcFileDTO(outputDir
				+ JobUtils.getWithoutPathAndExtension(injectDTO.getStandardHevcFile().getName()) + "[BL+RPU].hevc",
				injectDTO.getStandardHevcFile().getKey(), true);
	}

}
