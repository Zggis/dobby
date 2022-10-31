package com.zggis.dobby.batch.processors;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.zggis.dobby.batch.dto.HevcFileDTO;
import com.zggis.dobby.batch.dto.TVShowConversionDTO;
import com.zggis.dobby.batch.JobUtils;
import com.zggis.dobby.services.DoviProcessBuilder;

public class MKVToHevcProcessor implements ItemProcessor<TVShowConversionDTO, TVShowConversionDTO> {

	private static final Logger logger = LoggerFactory.getLogger(MKVToHevcProcessor.class);

	private String MKVEXTRACT;

	private String outputDir;

	private DoviProcessBuilder pbservice;

	private boolean execute;

	public MKVToHevcProcessor(DoviProcessBuilder pbservice, String outputDir, String MKVEXTRACT, boolean execute) {
		this.MKVEXTRACT = MKVEXTRACT;
		this.outputDir = outputDir;
		this.pbservice = pbservice;
		this.execute = execute;
	}

	@Override
	public TVShowConversionDTO process(TVShowConversionDTO conversion) throws IOException {
		logger.info("Generating HEVC file from {}...", JobUtils.getWithoutPath(conversion.getStandardFile().getName()));
		String cmd = MKVEXTRACT + " \"" + conversion.getStandardFile().getName() + "\" tracks 0:\"" + outputDir
				+ JobUtils.getWithoutPathAndExtension(conversion.getStandardFile().getName()) + ".hevc\"";
		logger.debug(cmd);
		ProcessBuilder pb = pbservice.get(cmd);
		pb.redirectErrorStream(true);
		if (execute) {
			Process p = pb.start();
			JobUtils.printOutput(p);
		} else {
			logger.info("===EXECUTION SKIPPED===");
		}
		conversion.getResults()
				.add(new HevcFileDTO(outputDir
						+ JobUtils.getWithoutPathAndExtension(conversion.getStandardFile().getName()) + ".hevc",
						conversion.getKey(), false));
		return conversion;
	}

}
