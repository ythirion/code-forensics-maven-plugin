package com.ythirion.codeforensics;

import lombok.Data;

@Data
public class Analysis {
    private final String name, taskName, specialArgs;

    public Analysis(String name, String taskName, String specialArgs) {
        this.name = name;
        this.taskName = taskName;
        this.specialArgs = specialArgs;
    }

    public Analysis(String name, String taskName) {
        this(name, taskName, "");
    }
}
