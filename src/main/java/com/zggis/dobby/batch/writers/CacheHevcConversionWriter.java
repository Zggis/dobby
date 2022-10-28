package com.zggis.dobby.batch.writers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;

import com.zggis.dobby.batch.HevcVideoConversion;

public class CacheHevcConversionWriter implements ItemWriter<HevcVideoConversion>, StepExecutionListener {

	private static final Logger logger = LoggerFactory.getLogger(CacheHevcConversionWriter.class);

	private List<HevcVideoConversion> hevcConversions = new ArrayList<>();

	@Override
	public void write(List<? extends HevcVideoConversion> items) throws Exception {
		for (HevcVideoConversion conversion : items) {
			hevcConversions.add(conversion);
			logger.info("Writing Conversion {}", conversion.getKey());
		}

	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		stepExecution.getJobExecution().getExecutionContext().put("HevcVideoConversion", hevcConversions);
		return ExitStatus.COMPLETED;
	}

}
