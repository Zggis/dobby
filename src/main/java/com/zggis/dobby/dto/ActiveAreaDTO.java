package com.zggis.dobby.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ActiveAreaDTO implements Serializable {

    private List<Integer> activeAreaHeights = new ArrayList<>();

    private List<Integer> activeAreaWidths = new ArrayList<>();

    public ActiveAreaDTO() {
        super();
    }

    public List<Integer> getActiveAreaHeights() {
        return activeAreaHeights;
    }

    public void setActiveAreaHeights(List<Integer> activeAreaHeights) {
        this.activeAreaHeights = activeAreaHeights;
    }

    public List<Integer> getActiveAreaWidths() {
        return activeAreaWidths;
    }

    public void setActiveAreaWidths(List<Integer> activeAreaWidths) {
        this.activeAreaWidths = activeAreaWidths;
    }

}
