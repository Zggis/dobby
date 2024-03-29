package com.zggis.dobby.dto.mediainfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackDTO implements Serializable {
	@JsonProperty("@type")
	public String type;
	@JsonProperty("Duration")
	public String duration;
	@JsonProperty("FrameRate")
	public String frameRate;
	@JsonProperty("FrameCount")
	public String frameCount;
	@JsonProperty("ID")
	public String iD;
	@JsonProperty("HDR_Format")
	public String hDR_Format;
	@JsonProperty("HDR_Format_Profile")
	public String hDR_Format_Profile;
	@JsonProperty("HDR_Format_Level")
	public String hDR_Format_Level;
	@JsonProperty("HDR_Format_Compatibility")
	public String hDR_Format_Compatibility;
	@JsonProperty("Width")
	public String width;
	@JsonProperty("Height")
	public String height;
	@JsonProperty("Sampled_Width")
	public String sampled_Width;
	@JsonProperty("Sampled_Height")
	public String sampled_Height;
	@JsonProperty("PixelAspectRatio")
	public String pixelAspectRatio;
	@JsonProperty("DisplayAspectRatio")
	public String displayAspectRatio;
}
