package com.ythirion.codeforensics;

import io.vavr.control.Try;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

@Mojo(name = "launch-webserver")
public class WebServerRunner extends AbstractMojo {
    @Parameter(defaultValue = "${project.build.directory}/code-forensics", readonly = true)
    private File outputDirectory;

    public void execute() {
        Try.run(() -> CommandUtils.execute("gulp webserver", outputDirectory, text -> getLog().info(text)))
                .onFailure(exception -> getLog().error(exception));
    }
}