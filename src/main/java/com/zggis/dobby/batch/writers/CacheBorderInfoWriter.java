package com.zggis.dobby.batch.writers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;

import com.zggis.dobby.dto.BorderInfoDTO;

public class CacheBorderInfoWriter implements ItemWriter<BorderInfoDTO>, StepExecutionListener {

	private static final Logger logger = LoggerFactory.getLogger(CacheBorderInfoWriter.class);

	private List<BorderInfoDTO> activeAreas = new ArrayList<>();

	@Override
	public void write(List<? extends BorderInfoDTO> items) throws Exception {
		for (BorderInfoDTO borderInfo : items) {
			activeAreas.add(borderInfo);
			logger.info("Writing Border Info");
		}

	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		for (BorderInfoDTO borderInfo : activeAreas) {
			if (borderInfo.getBottomOffset() == -1 || borderInfo.getLeftOffset() == -1
					|| borderInfo.getTopOffset() == -1 || borderInfo.getRightOffset() == -1) {
				return ExitStatus.FAILED;
			}
		}
		stepExecution.getJobExecution().getExecutionContext().put("BorderInfo", activeAreas);
		return ExitStatus.COMPLETED;
	}

}
