package com.zggis.dobby.dto.mediainfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackDTO {
	@JsonProperty("@type")
	public String type;
	@JsonProperty("UniqueID")
	public String uniqueID;
	@JsonProperty("VideoCount")
	public String videoCount;
	@JsonProperty("AudioCount")
	public String audioCount;
	@JsonProperty("TextCount")
	public String textCount;
	@JsonProperty("FileExtension")
	public String fileExtension;
	@JsonProperty("Format")
	public String format;
	@JsonProperty("Format_Version")
	public String format_Version;
	@JsonProperty("FileSize")
	public String fileSize;
	@JsonProperty("Duration")
	public String duration;
	@JsonProperty("OverallBitRate")
	public String overallBitRate;
	@JsonProperty("FrameRate")
	public String frameRate;
	@JsonProperty("FrameCount")
	public String frameCount;
	@JsonProperty("StreamSize")
	public String streamSize;
	@JsonProperty("IsStreamable")
	public String isStreamable;
	@JsonProperty("Encoded_Date")
	public String encoded_Date;
	@JsonProperty("File_Created_Date")
	public String file_Created_Date;
	@JsonProperty("File_Created_Date_Local")
	public String file_Created_Date_Local;
	@JsonProperty("File_Modified_Date")
	public String file_Modified_Date;
	@JsonProperty("File_Modified_Date_Local")
	public String file_Modified_Date_Local;
	@JsonProperty("Encoded_Application")
	public String encoded_Application;
	@JsonProperty("Encoded_Library")
	public String encoded_Library;
	@JsonProperty("StreamOrder")
	public String streamOrder;
	@JsonProperty("ID")
	public String iD;
	@JsonProperty("Format_Profile")
	public String format_Profile;
	@JsonProperty("Format_Level")
	public String format_Level;
	@JsonProperty("Format_Tier")
	public String format_Tier;
	@JsonProperty("HDR_Format")
	public String hDR_Format;
	@JsonProperty("HDR_Format_Compatibility")
	public String hDR_Format_Compatibility;
	@JsonProperty("CodecID")
	public String codecID;
	@JsonProperty("BitRate")
	public String bitRate;
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
	@JsonProperty("FrameRate_Mode")
	public String frameRate_Mode;
	@JsonProperty("ColorSpace")
	public String colorSpace;
	@JsonProperty("ChromaSubsampling")
	public String chromaSubsampling;
	@JsonProperty("ChromaSubsampling_Position")
	public String chromaSubsampling_Position;
	@JsonProperty("BitDepth")
	public String bitDepth;
	@JsonProperty("Delay")
	public String delay;
	@JsonProperty("Default")
	public String defaultTrack;
	@JsonProperty("Forced")
	public String forced;
	public String colour_description_present;
	public String colour_description_present_Source;
	public String colour_range;
	public String colour_range_Source;
	public String colour_primaries;
	public String colour_primaries_Source;
	public String transfer_characteristics;
	public String transfer_characteristics_Source;
	public String matrix_coefficients;
	public String matrix_coefficients_Source;
	@JsonProperty("MasteringDisplay_ColorPrimaries")
	public String masteringDisplay_ColorPrimaries;
	@JsonProperty("MasteringDisplay_ColorPrimaries_Source")
	public String masteringDisplay_ColorPrimaries_Source;
	@JsonProperty("MasteringDisplay_Luminance")
	public String masteringDisplay_Luminance;
	@JsonProperty("MasteringDisplay_Luminance_Source")
	public String masteringDisplay_Luminance_Source;
	@JsonProperty("MaxCLL")
	public String maxCLL;
	@JsonProperty("MaxCLL_Source")
	public String maxCLL_Source;
	@JsonProperty("MaxFALL")
	public String maxFALL;
	@JsonProperty("MaxFALL_Source")
	public String maxFALL_Source;
	@JsonProperty("Format_Commercial_IfAny")
	public String format_Commercial_IfAny;
	@JsonProperty("Format_Settings_Endianness")
	public String format_Settings_Endianness;
	@JsonProperty("Format_AdditionalFeatures")
	public String format_AdditionalFeatures;
	@JsonProperty("BitRate_Mode")
	public String bitRate_Mode;
	@JsonProperty("Channels")
	public String channels;
	@JsonProperty("ChannelPositions")
	public String channelPositions;
	@JsonProperty("ChannelLayout")
	public String channelLayout;
	@JsonProperty("SamplesPerFrame")
	public String samplesPerFrame;
	@JsonProperty("SamplingRate")
	public String samplingRate;
	@JsonProperty("SamplingCount")
	public String samplingCount;
	@JsonProperty("Compression_Mode")
	public String compression_Mode;
	@JsonProperty("Delay_Source")
	public String delay_Source;
	@JsonProperty("StreamSize_Proportion")
	public String streamSize_Proportion;
	@JsonProperty("Title")
	public String title;
	@JsonProperty("Language")
	public String language;
	@JsonProperty("ServiceKind")
	public String serviceKind;
	public ExtraDTO extra;
	@JsonProperty("@typeorder")
	public String typeorder;
	@JsonProperty("ElementCount")
	public String elementCount;
}
