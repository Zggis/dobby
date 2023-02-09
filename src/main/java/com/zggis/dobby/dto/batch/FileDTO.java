package com.zggis.dobby.dto.batch;

import java.io.Serializable;

public abstract class FileDTO implements IFile, Serializable {

    protected String name;

    protected String key;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getKey() {
        return key;
    }

}
