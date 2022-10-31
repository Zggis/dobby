package com.zggis.dobby.batch.writers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;

import com.zggis.dobby.batch.JobCacheKey;
import com.zggis.dobby.batch.dto.TVShowConversionDTO;
import com.zggis.dobby.batch.dto.VideoFileDTO;

public class CacheTVShowWriter implements ItemWriter<TVShowConversionDTO>, StepExecutionListener {

	private static final Logger logger = LoggerFactory.getLogger(CacheTVShowWriter.class);

	private List<TVShowConversionDTO> hevcConversions = new ArrayList<>();

	@Override
	public void write(List<? extends TVShowConversionDTO> items) throws Exception {
		for (TVShowConversionDTO conversion : items) {
			hevcConversions.add(conversion);
			logger.debug("Writing Conversion {}", conversion.getKey());
		}
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		stepExecution.getJobExecution().getExecutionContext().put(JobCacheKey.CONVERSION.value, hevcConversions);
		List<VideoFileDTO> stdFiles = new ArrayList<>();
		for (TVShowConversionDTO conversion : hevcConversions) {
			if (conversion.getDolbyVisionFile() == null || conversion.getStandardFile() == null) {
				return ExitStatus.FAILED;
			}
			stdFiles.add(conversion.getStandardFile());
		}
		stepExecution.getJobExecution().getExecutionContext().put(JobCacheKey.STDMKV.value, stdFiles);
		return ExitStatus.COMPLETED;
	}

}
