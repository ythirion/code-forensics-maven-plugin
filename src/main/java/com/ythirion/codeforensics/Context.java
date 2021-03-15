package com.ythirion.codeforensics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;

import java.io.File;
import java.time.LocalDate;

@With
@AllArgsConstructor
@Getter
public class Context {
    private final LocalDate dateTo = LocalDate.now();
    private final StringBuilder log = new StringBuilder();

    private LocalDate dateFrom;
    private final File gitRepositoryDirectory, outputDirectory;

    public Context(File gitRepository, File outputDirectory) {
        this.gitRepositoryDirectory = gitRepository;
        this.outputDirectory = outputDirectory;
    }

    public Context append(String text) {
        log.append(text);
        return this;
    }
}