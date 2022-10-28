package com.zggis.dobby.batch.readers;

import java.util.Collection;
import java.util.Stack;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.zggis.dobby.batch.HevcVideoConversion;

public class CacheHevcConversionReader implements ItemReader<HevcVideoConversion>, StepExecutionListener {

	private String fileType;

	private Stack<HevcVideoConversion> availableConversions = new Stack<>();

	public CacheHevcConversionReader(String fileType) {
		this.fileType = fileType;
	}

	@Override
	public HevcVideoConversion read()
			throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		if (!availableConversions.isEmpty()) {
			return availableConversions.pop();
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void beforeStep(StepExecution stepExecution) {
		availableConversions.addAll(
				(Collection<HevcVideoConversion>) stepExecution.getJobExecution().getExecutionContext().get(fileType));
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return ExitStatus.COMPLETED;
	}

}
