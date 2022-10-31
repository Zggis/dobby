package com.zggis.dobby.batch;

public enum JobCacheKey {
	STDMKV("STDMKV"), CONVERSION("HevcVideoConversion"), RPU("DolbyVisionRPU"), BLRPUHEVC("BLRPUHevc"),
	MERGE("BLRPUMerge"), BLRPUMKV("BLRPUMKV"), DVHEVC("DVHEVC"), STDHEVC("STDHEVC");

	public final String value;

	private JobCacheKey(String label) {
		this.value = label;
	}
}
