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
import com.zggis.dobby.batch.JobUtils;
import com.zggis.dobby.dto.batch.HevcFileDTO;
import com.zggis.dobby.dto.batch.TVShowConversionDTO;

public class CacheConversionWriter implements ItemWriter<TVShowConversionDTO>, StepExecutionListener {

	private static final Logger logger = LoggerFactory.getLogger(CacheConversionWriter.class);

	private List<HevcFileDTO> files = new ArrayList<>();

	@Override
	public void write(List<? extends TVShowConversionDTO> items) throws Exception {
		for (TVShowConversionDTO conversion : items) {
			files.addAll(conversion.getResults());
			logger.debug("Writing {} : {}", "DV/STDHEVC", conversion.getKey());
		}

	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		List<HevcFileDTO> stdFiles = new ArrayList<>();
		List<HevcFileDTO> dvFiles = new ArrayList<>();
		for (HevcFileDTO fileName : files) {
			if (!JobUtils.doesMediaFileExists(fileName.getName())) {
				return ExitStatus.FAILED;
			}
			if (fileName.isDolbyVision()) {
				dvFiles.add(fileName);
			} else {
				stdFiles.add(fileName);
			}
		}
		stepExecution.getJobExecution().getExecutionContext().put(JobCacheKey.DVHEVC.value, dvFiles);
		stepExecution.getJobExecution().getExecutionContext().put(JobCacheKey.STDHEVC.value, stdFiles);
		return ExitStatus.COMPLETED;
	}

}
