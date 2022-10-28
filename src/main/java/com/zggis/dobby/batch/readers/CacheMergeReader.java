package com.zggis.dobby.batch.readers;

import java.io.File;
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

import com.zggis.dobby.batch.FileDTO;
import com.zggis.dobby.batch.VideoFileDTO;
import com.zggis.dobby.batch.VideoMergeDTO;

public class CacheMergeReader implements ItemReader<VideoMergeDTO>, StepExecutionListener {

	private static final Logger logger = LoggerFactory.getLogger(CacheMergeReader.class);

	private Stack<VideoMergeDTO> availableMerges = new Stack<>();

	private static final Pattern EPISODE_NUM_REGEX = Pattern.compile("^.*?s(\\d{2})((?:e\\d{2})+).*");

	private String mediaDir;

	public CacheMergeReader(String mediaDir) {
		this.mediaDir = mediaDir;
	}

	@Override
	public VideoMergeDTO read()
			throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		if (!availableMerges.isEmpty()) {
			return availableMerges.pop();
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void beforeStep(StepExecution stepExecution) {
		List<FileDTO> blrpus = (List<FileDTO>) stepExecution.getJobExecution().getExecutionContext().get("BLRPUHevc");
		Map<String, FileDTO> blrpuMap = new HashMap<>();
		for (FileDTO rpu : blrpus) {
			blrpuMap.put(rpu.getKey(), rpu);
		}
		File dir = new File(mediaDir);
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				if (child.getName().endsWith(".mkv")) {
					logger.debug("Checking {}", child.getName());
					Matcher m = EPISODE_NUM_REGEX.matcher(child.getName().toLowerCase());
					if (m.find()) {
						String season = m.group(1);
						String episode = m.group(2).substring(1, m.group(2).length()).replace("e", "-");
						FileDTO blrpuFileName = blrpuMap.get(season + episode);
						if (blrpuFileName != null) {
							VideoMergeDTO newInjectionDTO = new VideoMergeDTO(
									new VideoFileDTO(mediaDir + "/" + child.getName(), season + episode),
									blrpuFileName);
							availableMerges.push(newInjectionDTO);
						} else {
							logger.warn("Unable to find a BLRPU episode match for {}", child.getName());
						}
					}
				}
			}
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return ExitStatus.COMPLETED;
	}

}
