package com.zggis.dobby.batch;

public enum JobCacheKey {
	RPU("DolbyVisionRPU"), BLRPUHEVC("BLRPUHevc"), MERGE("BLRPUMerge"), BLRPUMKV("BLRPUMKV"), MEDIAFILE("MEDIAFILE"),
	HEVCFILE("HEVCFILE");

	public final String value;

	private JobCacheKey(String label) {
		this.value = label;
	}
}
