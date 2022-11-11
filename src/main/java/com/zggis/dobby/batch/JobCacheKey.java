package com.zggis.dobby.batch;

public enum JobCacheKey {
	RPU("DolbyVisionRPU"), BLRPUHEVC("BLRPUHevc"), MERGE("BLRPUMerge"), BLRPUMKV("BLRPUMKV"), MEDIAFILE("MEDIAFILE"),
	HEVCFILE("HEVCFILE"), NONE("NONE");

	public final String value;

	JobCacheKey(String label) {
		this.value = label;
	}
}
