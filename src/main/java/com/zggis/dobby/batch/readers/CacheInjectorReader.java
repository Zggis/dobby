package com.zggis.dobby.batch.readers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.zggis.dobby.batch.JobUtils;
import com.zggis.dobby.batch.VideoInjectionDTO;

public class CacheInjectorReader implements ItemReader<VideoInjectionDTO>, StepExecutionListener {

	private static final Logger logger = LoggerFactory.getLogger(CacheInjectorReader.class);

	private Stack<VideoInjectionDTO> availableInjections = new Stack<>();

	private static final Pattern EPISODE_NUM_REGEX = Pattern.compile("^.*?s(\\d{2})((?:e\\d{2})+).*");

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
		List<String> rpus = (List<String>) stepExecution.getJobExecution().getExecutionContext().get("DolbyVisionRPU");
		List<String> stds = (List<String>) stepExecution.getJobExecution().getExecutionContext().get("StandardHevc");
		Map<String, String> rpuMap = new HashMap<>();
		for (String rpu : rpus) {
			Matcher m = EPISODE_NUM_REGEX.matcher(JobUtils.getWithoutPathAndExtension(rpu).toLowerCase());
			if (m.find()) {
				String season = m.group(1);
				String episode = m.group(2).substring(1, m.group(2).length()).replace("e", "-");
				rpuMap.put(season + episode, rpu);
			}
		}
		for (String std : stds) {
			Matcher m = EPISODE_NUM_REGEX.matcher(JobUtils.getWithoutPathAndExtension(std).toLowerCase());
			if (m.find()) {
				String season = m.group(1);
				String episode = m.group(2).substring(1, m.group(2).length()).replace("e", "-");
				String rpuFileName = rpuMap.get(season + episode);
				if (rpuFileName != null) {
					VideoInjectionDTO newInjectionDTO = new VideoInjectionDTO(std, rpuFileName);
					availableInjections.push(newInjectionDTO);
				} else {
					logger.warn("Unable to find a RPU episode match for {}", std);
				}
			}
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return ExitStatus.COMPLETED;
	}

}
