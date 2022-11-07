package com.zggis.dobby.batch.readers;

import java.util.*;

import com.zggis.dobby.dto.batch.*;
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
import org.springframework.util.StringUtils;

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
        for (HevcFileDTO hevc : hevcs) {
            if (!JobUtils.isDolbyVision(hevc.getMediaInfo())) {
                int bestMatchGrade = 0;
                RPUFileDTO bestMatchRpu = null;
                int hevcFrameCount = JobUtils.getFrameCount(hevc.getMediaInfo());
                for (RPUFileDTO rpu : rpus) {
                    int currentGrade = 0;
                    int rpuFrameCount = JobUtils.getFrameCount(rpu.getMediaInfo());
                    if (Math.abs(hevcFrameCount - rpuFrameCount) < 5) {
                        logger.debug("Matched frame count {} between {} and {}", rpuFrameCount, rpu.getName(), hevc.getName());
                        currentGrade += 50;
                    }
                    if (StringUtils.hasText(hevc.getKey()) && StringUtils.hasText(rpu.getKey()) && hevc.getKey().equals(rpu.getKey())) {
                        logger.debug("Matched TV show episode {} between {} and {}", rpu.getKey(), rpu.getName(), hevc.getName());
                        currentGrade += 20;
                    }
                    String name1 = JobUtils.getWithoutPathAndExtension(rpu.getName());
                    String name2 = JobUtils.getWithoutPathAndExtension(hevc.getName());
                    Collection<String> titleMatches = JobUtils.getTitleMatches(name1, name2);
                    if (!titleMatches.isEmpty()) {
                        logger.debug("Matched media name {} between {} and {}", titleMatches.toArray(), rpu.getName(), hevc.getName());
                        currentGrade += 20;
                    }
                    if (currentGrade > bestMatchGrade) {
                        bestMatchGrade = currentGrade;
                        bestMatchRpu = rpu;
                    }
                }
                if (bestMatchGrade > 0) {
                    VideoInjectionDTO newInjectionDTO = new VideoInjectionDTO(hevc, bestMatchRpu);
                    rpus.remove(bestMatchRpu);
                    availableInjections.push(newInjectionDTO);
                } else {
                    logger.warn(ConsoleColor.YELLOW.value
                            + "Unable to find a RPU match for {}, check the logs above. This may be caused by a missing file, or a file that failed validation and was skipped as a result."
                            + ConsoleColor.YELLOW.value, hevc.getName());
                }
            }
        }
        for (RPUFileDTO rpu : rpus) {
            logger.warn(ConsoleColor.YELLOW.value
                    + "Unable to find a HDR HEVC match for {}, check the logs above. This may be caused by a missing file, or a file that failed validation and was skipped as a result."
                    + ConsoleColor.YELLOW.value, rpu.getName());
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return ExitStatus.COMPLETED;
    }

}
