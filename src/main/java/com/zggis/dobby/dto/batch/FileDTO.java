package com.zggis.dobby.dto.batch;

public abstract class FileDTO implements IFile {

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
