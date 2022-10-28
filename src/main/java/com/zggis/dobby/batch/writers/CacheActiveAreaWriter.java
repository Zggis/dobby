package com.zggis.dobby.batch.writers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;
import org.springframework.util.CollectionUtils;

import com.zggis.dobby.dto.ActiveAreaDTO;

public class CacheActiveAreaWriter implements ItemWriter<ActiveAreaDTO>, StepExecutionListener {

	private static final Logger logger = LoggerFactory.getLogger(CacheActiveAreaWriter.class);

	private List<ActiveAreaDTO> activeAreas = new ArrayList<>();

	@Override
	public void write(List<? extends ActiveAreaDTO> items) throws Exception {
		for (ActiveAreaDTO activeArea : items) {
			activeAreas.add(activeArea);
			logger.info("Writing Active Area");
		}

	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		for (ActiveAreaDTO activeArea : activeAreas) {
			if (CollectionUtils.isEmpty(activeArea.getActiveAreaHeights())
					|| CollectionUtils.isEmpty(activeArea.getActiveAreaWidths())) {
				return ExitStatus.FAILED;
			}
		}
		stepExecution.getJobExecution().getExecutionContext().put("ActiveArea", activeAreas);
		return ExitStatus.COMPLETED;
	}

}
