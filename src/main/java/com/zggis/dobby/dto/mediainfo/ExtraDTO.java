package com.zggis.dobby.dto.mediainfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtraDTO {
	@JsonProperty("ComplexityIndex")
	public String complexityIndex;
	@JsonProperty("NumberOfDynamicObjects")
	public String numberOfDynamicObjects;
	@JsonProperty("BedChannelCount")
	public String bedChannelCount;
	@JsonProperty("BedChannelConfiguration")
	public String bedChannelConfiguration;
	public String bsid;
	public String dialnorm;
	public String compr;
	public String acmod;
	public String lfeon;
	public String dialnorm_Average;
	public String dialnorm_Minimum;
	public String compr_Average;
	public String compr_Minimum;
	public String compr_Maximum;
	public String compr_Count;
}
