package com.zggis.dobby.dto.mediainfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatingLibraryDTO implements Serializable {
    public String name;
    public String version;
    public String url;
}
