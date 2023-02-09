package com.zggis.dobby.dto;

import java.io.Serializable;

public class BorderInfoDTO implements Serializable {

    private int topOffset = 0;

    private int bottomOffset = 0;

    private int leftOffset = 0;

    private int rightOffset = 0;

    public BorderInfoDTO() {
        super();
    }

    public int getTopOffset() {
        return topOffset;
    }

    public void setTopOffset(int topOffset) {
        this.topOffset = topOffset;
    }

    public int getBottomOffset() {
        return bottomOffset;
    }

    public void setBottomOffset(int bottomOffset) {
        this.bottomOffset = bottomOffset;
    }

    public int getLeftOffset() {
        return leftOffset;
    }

    public void setLeftOffset(int leftOffset) {
        this.leftOffset = leftOffset;
    }

    public int getRightOffset() {
        return rightOffset;
    }

    public void setRightOffset(int rightOffset) {
        this.rightOffset = rightOffset;
    }

}
