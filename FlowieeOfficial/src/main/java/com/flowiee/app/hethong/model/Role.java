package com.flowiee.app.hethong.model;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class Role {
    @Data
    public static class Action {
        private boolean isChecked;
        private String keyAction;
        private String valueAction;
    }

    private Map<String, String> module;
    private List<Action> action;
}