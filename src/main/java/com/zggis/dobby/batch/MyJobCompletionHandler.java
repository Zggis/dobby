package com.zggis.dobby.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MyJobCompletionHandler extends JobExecutionListenerSupport {

	private static final Logger log = LoggerFactory.getLogger(MyJobCompletionHandler.class);

	@Value("${media.dir}")
	private String mediaDir;

	@Autowired
	public MyJobCompletionHandler() {
		super();
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
			// JobUtils.deleteDirectory(mediaDir + "/temp");
			log.info("Job Complete!");
		}
	}

	@Override
	public void beforeJob(JobExecution jobExecution) {
		JobUtils.createDirectory(mediaDir + "/temp");
	}

}
