package com.zggis.dobby.batch.readers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.zggis.dobby.batch.ConsoleColor;
import com.zggis.dobby.batch.JobCacheKey;
import com.zggis.dobby.batch.JobUtils;
import com.zggis.dobby.dto.batch.HevcFileDTO;
import com.zggis.dobby.dto.batch.RPUFileDTO;
import com.zggis.dobby.dto.batch.VideoInjectionDTO;

public class CacheInjectorReader implements ItemReader<VideoInjectionDTO>, StepExecutionListener {

	private static final Logger logger = LoggerFactory.getLogger(CacheInjectorReader.class);

	private Stack<VideoInjectionDTO> availableInjections = new Stack<>();

	@Override
	public VideoInjectionDTO read()
			throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		if (!availableInjections.isEmpty()) {
			return availableInjections.pop();
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void beforeStep(StepExecution stepExecution) {
		List<RPUFileDTO> rpus = (List<RPUFileDTO>) stepExecution.getJobExecution().getExecutionContext()
				.get(JobCacheKey.RPU.value);
		List<HevcFileDTO> hevcs = (List<HevcFileDTO>) stepExecution.getJobExecution().getExecutionContext()
				.get(JobCacheKey.HEVCFILE.value);
		Map<String, RPUFileDTO> rpuMap = new HashMap<>();
		for (RPUFileDTO rpu : rpus) {
			rpuMap.put(rpu.getKey(), rpu);
		}
		for (HevcFileDTO hevc : hevcs) {
			if (!hevc.isDolbyVision()) {
				RPUFileDTO rpuFile = rpuMap.get(hevc.getKey());
				if (rpuFile != null) {
					VideoInjectionDTO newInjectionDTO = new VideoInjectionDTO(hevc, rpuFile);
					availableInjections.push(newInjectionDTO);
				} else {
					logger.warn(ConsoleColor.YELLOW.value + "Unable to find a RPU episode match for {}"
							+ ConsoleColor.YELLOW.value, JobUtils.getWithoutPath(hevc.getName()));
				}
			}
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return ExitStatus.COMPLETED;
	}

}
