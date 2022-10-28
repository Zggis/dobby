package com.zggis.dobby.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zggis.dobby.batch.processors.ExtractRpuProcessor;
import com.zggis.dobby.batch.processors.MKVActiveAreaProcessor;
import com.zggis.dobby.batch.processors.MKVToHevcProcessor;
import com.zggis.dobby.batch.processors.MP4ToHevcProcessor;
import com.zggis.dobby.batch.processors.MergeToMKVProcessor;
import com.zggis.dobby.batch.processors.RPUBorderInfoProcessor;
import com.zggis.dobby.batch.processors.RPUInjectProcessor;
import com.zggis.dobby.batch.readers.CacheFileReader;
import com.zggis.dobby.batch.readers.CacheInjectorReader;
import com.zggis.dobby.batch.readers.CacheMergeReader;
import com.zggis.dobby.batch.readers.DolbyVisionDiskFileReader;
import com.zggis.dobby.batch.readers.StandardDiskFileReader;
import com.zggis.dobby.batch.writers.CacheActiveAreaWriter;
import com.zggis.dobby.batch.writers.CacheBorderInfoWriter;
import com.zggis.dobby.batch.writers.CacheFileWriter;
import com.zggis.dobby.dto.ActiveAreaDTO;
import com.zggis.dobby.dto.BorderInfoDTO;
import com.zggis.dobby.services.DoviProcessBuilder;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	@Value("${media.dir}")
	private String mediaDir;

	@Value("${dovi.tool.location}")
	private String DOVI_TOOL;

	@Value("${mkvextract.location}")
	private String MKVEXTRACT;

	@Value("${mkvmerge.location}")
	private String MKVMERGE;

	@Value("${mp4extract.location}")
	private String MP4EXTRACT;

	@Value("${mediainfo.location}")
	private String MEDIAINFO;

	@Value("${ffmpeg.location}")
	private String FFMPEG;

	@Autowired
	private DoviProcessBuilder pbservice;

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job mergeVideoFilesJob(MyJobCompletionHandler listener) {
		return jobBuilderFactory.get("mergeVideoFilesJob").incrementer(new RunIdIncrementer()).listener(listener)
				.flow(step1()).next(step2()).next(step3()).next(step4()).next(step5()).next(step6()).next(step7()).end()
				.build();
	}

	// Step 1 - Convert DV MP4 to HEVC
	@Bean
	public Step step1() {
		return stepBuilderFactory.get("step1").<String, String>chunk(10).reader(dolbyVisionReader())
				.processor(mp4ToHevcProcessor()).writer(mp4ToHevcWriter()).build();
	}

	@Bean
	public ItemReader<String> dolbyVisionReader() {
		return new DolbyVisionDiskFileReader(mediaDir);
	}

	@Bean
	public MP4ToHevcProcessor mp4ToHevcProcessor() {
		return new MP4ToHevcProcessor(pbservice, mediaDir + "/", mediaDir + "/temp/", MP4EXTRACT, false);
	}

	@Bean
	public ItemWriter<String> mp4ToHevcWriter() {
		return new CacheFileWriter("DolbyVisionHevc");
	}

	// Step 2 - Extract RPU from DV HEVC
	@Bean
	public Step step2() {
		return stepBuilderFactory.get("step2").<String, String>chunk(10).reader(dolbyVisionHevcReader())
				.processor(extractRpuProcessor()).writer(hevcToRpuWriter()).build();
	}

	@Bean
	public ItemReader<String> dolbyVisionHevcReader() {
		return new CacheFileReader("DolbyVisionHevc");
	}

	@Bean
	public ExtractRpuProcessor extractRpuProcessor() {
		return new ExtractRpuProcessor(pbservice, mediaDir + "/temp/", DOVI_TOOL, false);
	}

	@Bean
	public ItemWriter<String> hevcToRpuWriter() {
		return new CacheFileWriter("DolbyVisionRPU");
	}

	// Step 3 - Get active area from MKV
	@Bean
	public Step step3() {
		return stepBuilderFactory.get("step3").<String, ActiveAreaDTO>chunk(10).reader(standardActiveAreaReader())
				.processor(mkvActiveAreaProcessor()).writer(cacheActiveAreaWriter()).build();
	}

	@Bean
	public ItemReader<String> standardActiveAreaReader() {
		return new StandardDiskFileReader(mediaDir);
	}

	@Bean
	public MKVActiveAreaProcessor mkvActiveAreaProcessor() {
		return new MKVActiveAreaProcessor(pbservice, mediaDir + "/", FFMPEG, false);
	}

	@Bean
	public ItemWriter<ActiveAreaDTO> cacheActiveAreaWriter() {
		return new CacheActiveAreaWriter();
	}

	// Step 4 - Get border info from RPU
	@Bean
	public Step step4() {
		return stepBuilderFactory.get("step4").<String, BorderInfoDTO>chunk(10).reader(standardBorderInfoReader())
				.processor(rpuBorderInfoProcessor()).writer(cacheBorderInfoWriter()).build();
	}

	@Bean
	public ItemReader<String> standardBorderInfoReader() {
		return new CacheFileReader("DolbyVisionRPU");
	}

	@Bean
	public RPUBorderInfoProcessor rpuBorderInfoProcessor() {
		return new RPUBorderInfoProcessor(pbservice, DOVI_TOOL, false);
	}

	@Bean
	public ItemWriter<BorderInfoDTO> cacheBorderInfoWriter() {
		return new CacheBorderInfoWriter();
	}

	// -------------
	// Validate Active Area Step
	// ------------

	// Step 5 - Convert standard MKV to HEVC
	@Bean
	public Step step5() {
		return stepBuilderFactory.get("step5").<String, String>chunk(10).reader(standardActiveAreaReader2())
				.processor(mkvToHevcProcessor()).writer(mkvToHevcWriter()).build();
	}

	@Bean
	public ItemReader<String> standardActiveAreaReader2() {
		return new StandardDiskFileReader(mediaDir);
	}

	@Bean
	public MKVToHevcProcessor mkvToHevcProcessor() {
		return new MKVToHevcProcessor(pbservice, mediaDir + "/", mediaDir + "/temp/", MKVEXTRACT, false);
	}

	@Bean
	public ItemWriter<String> mkvToHevcWriter() {
		return new CacheFileWriter("StandardHevc");
	}

	// Step 6 - Inject RPU into standard HEVC
	@Bean
	public Step step6() {
		return stepBuilderFactory.get("step6").<VideoInjectionDTO, String>chunk(10).reader(cacheInjectorReader())
				.processor(rpuInjectProcessor()).writer(blRPUCacheFileWriter()).build();
	}

	@Bean
	public ItemReader<VideoInjectionDTO> cacheInjectorReader() {
		return new CacheInjectorReader();
	}

	@Bean
	public RPUInjectProcessor rpuInjectProcessor() {
		return new RPUInjectProcessor(pbservice, mediaDir + "/temp/", DOVI_TOOL, false);
	}

	@Bean
	public ItemWriter<String> blRPUCacheFileWriter() {
		return new CacheFileWriter("BLRPUHevc");
	}

	// Step 7 - Convert BL-RPU to MKV
	@Bean
	public Step step7() {
		return stepBuilderFactory.get("step7").<VideoMergeDTO, String>chunk(10).reader(cacheMergeReader())
				.processor(mergeToMkvProcessor()).writer(blRPUMKVCacheFileWriter()).build();
	}

	@Bean
	public ItemReader<VideoMergeDTO> cacheMergeReader() {
		return new CacheMergeReader(mediaDir);
	}

	@Bean
	public MergeToMKVProcessor mergeToMkvProcessor() {
		return new MergeToMKVProcessor(pbservice, mediaDir + "/results/", MKVMERGE, false);
	}

	@Bean
	public ItemWriter<String> blRPUMKVCacheFileWriter() {
		return new CacheFileWriter("BLRPUMKV");
	}
}
