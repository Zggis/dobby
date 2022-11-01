package com.zggis.dobby.batch.processors;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.zggis.dobby.batch.JobUtils;
import com.zggis.dobby.dto.batch.BLRPUHevcFileDTO;
import com.zggis.dobby.dto.batch.VideoInjectionDTO;
import com.zggis.dobby.services.DoviProcessBuilder;

public class RPUInjectProcessor implements ItemProcessor<VideoInjectionDTO, BLRPUHevcFileDTO> {

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
	public BLRPUHevcFileDTO process(VideoInjectionDTO injectDTO) throws IOException {
		logger.info("Injecting {} into {}", JobUtils.getWithoutPath(injectDTO.getRpuFile().getName()),
				JobUtils.getWithoutPath(injectDTO.getStandardHevcFile().getName()));
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
		BLRPUHevcFileDTO blrpuHevcFileDTO = new BLRPUHevcFileDTO(
				outputDir + JobUtils.getWithoutPathAndExtension(injectDTO.getStandardHevcFile().getName())
						+ "[BL+RPU].hevc",
				injectDTO.getStandardHevcFile().getKey(), injectDTO.getStandardHevcFile().getMediaInfo(), true);
		blrpuHevcFileDTO.setBorderInfo(injectDTO.getRpuFile().getBorderInfo());
		return blrpuHevcFileDTO;
	}

}
