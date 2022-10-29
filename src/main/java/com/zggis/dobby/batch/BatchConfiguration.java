package com.zggis.dobby.batch;

import java.util.Arrays;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zggis.dobby.batch.processors.ExtractRpuProcessor;
import com.zggis.dobby.batch.processors.MKVActiveAreaProcessor;
import com.zggis.dobby.batch.processors.MKVToHevcProcessor;
import com.zggis.dobby.batch.processors.MP4ToHevcProcessor;
import com.zggis.dobby.batch.processors.MediaInfoProcessor;
import com.zggis.dobby.batch.processors.MergeToMKVProcessor;
import com.zggis.dobby.batch.processors.RPUBorderInfoProcessor;
import com.zggis.dobby.batch.processors.RPUInjectProcessor;
import com.zggis.dobby.batch.readers.CacheInjectorReader;
import com.zggis.dobby.batch.readers.CacheMergeReader;
import com.zggis.dobby.batch.readers.CacheReader;
import com.zggis.dobby.batch.readers.DiskHevcConversionFileReader;
import com.zggis.dobby.batch.writers.CacheConversionWriter;
import com.zggis.dobby.batch.writers.CacheFileWriter;
import com.zggis.dobby.batch.writers.CacheHevcConversionWriter;
import com.zggis.dobby.services.DoviProcessBuilder;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	private static final int CHUNK = 1;

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
				.flow(step0()).next(step0_5()).next(step1()).next(step2()).next(step2_5()).next(step3()).next(step4())
				.end().build();
	}

	// Step 0 - Fetch Media Info
	@Bean
	public Step step0() {
		return stepBuilderFactory.get("step0").<HevcVideoConversion, HevcVideoConversion>chunk(CHUNK)
				.reader(mediaInfoDiskFileReader()).processor(mediaInfoProcessor()).writer(hevcConversionWriter())
				.build();
	}

	@Bean
	public ItemReader<HevcVideoConversion> mediaInfoDiskFileReader() {
		return new DiskHevcConversionFileReader(mediaDir);
	}

	@Bean
	public MediaInfoProcessor mediaInfoProcessor() {
		return new MediaInfoProcessor(pbservice, mediaDir + "/temp/", MEDIAINFO, true);
	}

	@Bean
	public ItemWriter<HevcVideoConversion> hevcConversionWriter() {
		return new CacheHevcConversionWriter();
	}

	// step 0.5 - Populate active area
	@Bean
	public Step step0_5() {
		return stepBuilderFactory.get("step0_5").<VideoFileDTO, VideoFileDTO>chunk(CHUNK).reader(stdMkvReader())
				.processor(mkvActiveAreaProcessor()).writer(stdMkvWriter()).build();
	}

	@Bean
	public ItemReader<VideoFileDTO> stdMkvReader() {
		return new CacheReader<VideoFileDTO>("STDMKV");
	}

	@Bean
	public MKVActiveAreaProcessor mkvActiveAreaProcessor() {
		return new MKVActiveAreaProcessor(pbservice, FFMPEG, false);
	}

	@Bean
	public ItemWriter<VideoFileDTO> stdMkvWriter() {
		return new CacheFileWriter<VideoFileDTO>("STDMKV");
	}

	// Step 1 - Convert DV MP4 to HEVC
	@Bean
	public Step step1() {
		return stepBuilderFactory.get("step1").<HevcVideoConversion, HevcVideoConversion>chunk(CHUNK)
				.reader(hevcConversionDiskFileReader()).processor(compositeItemProcessor()).writer(conversionWriter())
				.build();
	}

	@Bean
	public ItemProcessor<HevcVideoConversion, HevcVideoConversion> compositeItemProcessor() {
		CompositeItemProcessor<HevcVideoConversion, HevcVideoConversion> compositeItemProcessor = new CompositeItemProcessor<>();
		compositeItemProcessor.setDelegates(Arrays.asList(mp4ToHevcProcessor(), mkvToHevcProcessor()));
		return compositeItemProcessor;
	}

	@Bean
	public ItemReader<HevcVideoConversion> hevcConversionDiskFileReader() {
		return new CacheReader<HevcVideoConversion>("HevcVideoConversion");
	}

	@Bean
	public MP4ToHevcProcessor mp4ToHevcProcessor() {
		return new MP4ToHevcProcessor(pbservice, mediaDir + "/temp/", MP4EXTRACT, false);
	}

	@Bean
	public MKVToHevcProcessor mkvToHevcProcessor() {
		return new MKVToHevcProcessor(pbservice, mediaDir + "/temp/", MKVEXTRACT, false);
	}

	@Bean
	public ItemWriter<HevcVideoConversion> conversionWriter() {
		return new CacheConversionWriter();
	}

	// Step 2 - Extract RPU from DV HEVC

	@Bean
	public Step step2() {
		return stepBuilderFactory.get("step2").<HevcFileDTO, RPUFileDTO>chunk(CHUNK).reader(hevcReader())
				.processor(extractRpuProcessor()).writer(hevcToRpuWriter()).build();
	}

	@Bean
	public ItemReader<HevcFileDTO> hevcReader() {
		return new CacheReader<HevcFileDTO>("DVHEVC");
	}

	@Bean
	public ExtractRpuProcessor extractRpuProcessor() {
		return new ExtractRpuProcessor(pbservice, mediaDir + "/temp/", DOVI_TOOL, false);
	}

	@Bean
	public ItemWriter<RPUFileDTO> hevcToRpuWriter() {
		return new CacheFileWriter<RPUFileDTO>("DolbyVisionRPU");
	}

	// Step 2.5 - Populate Border info
	@Bean
	public Step step2_5() {
		return stepBuilderFactory.get("step2_5").<RPUFileDTO, RPUFileDTO>chunk(CHUNK).reader(rpuReader())
				.processor(rpuBorderInfoProcessor()).writer(rpuWriter()).build();
	}

	@Bean
	public ItemReader<RPUFileDTO> rpuReader() {
		return new CacheReader<RPUFileDTO>("DolbyVisionRPU");
	}

	@Bean
	public RPUBorderInfoProcessor rpuBorderInfoProcessor() {
		return new RPUBorderInfoProcessor(pbservice, DOVI_TOOL, true);
	}

	@Bean
	public ItemWriter<RPUFileDTO> rpuWriter() {
		return new CacheFileWriter<RPUFileDTO>("DolbyVisionRPU");
	}

	/*
	 * // Step 3 - Get active area from MKV
	 * 
	 * @Bean public Step step3() { return stepBuilderFactory.get("step3").<String,
	 * ActiveAreaDTO>chunk(10).reader(standardActiveAreaReader())
	 * .processor(mkvActiveAreaProcessor()).writer(cacheActiveAreaWriter()).build();
	 * }
	 * 
	 * @Bean public ItemReader<String> standardActiveAreaReader() { return new
	 * StandardDiskFileReader(mediaDir); }
	 * 
	 * @Bean public MKVActiveAreaProcessor mkvActiveAreaProcessor() { return new
	 * MKVActiveAreaProcessor(pbservice, mediaDir + "/", FFMPEG, false); }
	 * 
	 * @Bean public ItemWriter<ActiveAreaDTO> cacheActiveAreaWriter() { return new
	 * CacheActiveAreaWriter(); }
	 * 
	 * // Step 4 - Get border info from RPU
	 * 
	 * @Bean public Step step4() { return stepBuilderFactory.get("step4").<String,
	 * BorderInfoDTO>chunk(10).reader(standardBorderInfoReader())
	 * .processor(rpuBorderInfoProcessor()).writer(cacheBorderInfoWriter()).build();
	 * }
	 * 
	 * @Bean public ItemReader<String> standardBorderInfoReader() { return new
	 * CacheFileReader("DolbyVisionRPU"); }
	 * 
	 * @Bean public RPUBorderInfoProcessor rpuBorderInfoProcessor() { return new
	 * RPUBorderInfoProcessor(pbservice, DOVI_TOOL, false); }
	 * 
	 * @Bean public ItemWriter<BorderInfoDTO> cacheBorderInfoWriter() { return new
	 * CacheBorderInfoWriter(); }
	 * 
	 * // ------------- // Validate Active Area Step // ------------
	 * 
	 * // Step 5 - Convert standard MKV to HEVC
	 * 
	 * @Bean public Step step5() { return stepBuilderFactory.get("step5").<String,
	 * String>chunk(10).reader(standardActiveAreaReader2())
	 * .processor(mkvToHevcProcessor()).writer(mkvToHevcWriter()).build(); }
	 * 
	 * @Bean public ItemReader<String> standardActiveAreaReader2() { return new
	 * StandardDiskFileReader(mediaDir); }
	 * 
	 * @Bean public MKVToHevcProcessor mkvToHevcProcessor() { return new
	 * MKVToHevcProcessor(pbservice, mediaDir + "/", mediaDir + "/temp/",
	 * MKVEXTRACT, false); }
	 * 
	 * @Bean public ItemWriter<String> mkvToHevcWriter() { return new
	 * CacheFileWriter("StandardHevc"); }
	 */
	// Step 3 - Inject RPU into standard HEVC

	@Bean
	public Step step3() {
		return stepBuilderFactory.get("step3").<VideoInjectionDTO, HevcFileDTO>chunk(CHUNK)
				.reader(cacheInjectorReader()).processor(rpuInjectProcessor()).writer(blRPUCacheFileWriter()).build();
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
	public ItemWriter<HevcFileDTO> blRPUCacheFileWriter() {
		return new CacheFileWriter<HevcFileDTO>("BLRPUHevc");
	}

	// Step 7 - Convert BL-RPU to MKV

	@Bean
	public Step step4() {
		return stepBuilderFactory.get("step4").<VideoMergeDTO, VideoFileDTO>chunk(CHUNK).reader(cacheMergeReader())
				.processor(mergeToMkvProcessor()).writer(blRPUMKVCacheFileWriter()).build();
	}

	@Bean
	public ItemReader<VideoMergeDTO> cacheMergeReader() {
		return new CacheMergeReader();
	}

	@Bean
	public MergeToMKVProcessor mergeToMkvProcessor() {
		return new MergeToMKVProcessor(pbservice, mediaDir + "/results/", MKVMERGE, false);
	}

	@Bean
	public ItemWriter<VideoFileDTO> blRPUMKVCacheFileWriter() {
		return new CacheFileWriter<VideoFileDTO>("BLRPUMKV");
	}

}
