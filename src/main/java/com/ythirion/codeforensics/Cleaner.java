package com.ythirion.codeforensics;

import io.vavr.control.Try;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

@Mojo(name = "clean")
public class Cleaner extends AbstractMojo {
    @Parameter(defaultValue = "${project.build.directory}/code-forensics", readonly = true)
    private File outputDirectory;

    public void execute() {
        Try.run(() -> FileUtils.deleteDirectory(outputDirectory))
                .onFailure(exception -> getLog().error(exception));
    }
}