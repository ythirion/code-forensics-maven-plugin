package com.ythirion.codeforensics;

import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Try;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;

@Mojo(name = "run")
public class Runner extends AbstractMojo {
    private static final String UTF_8 = "UTF-8";
    private static final String REPOSITORY_PATH_VARIABLE = "{repositoryPath}";
    private static final String GULPFILE = "gulpfile.js";
    public static final String GIT_LOG_DATES_COMMAND = "git log --reverse --pretty=format:%ad --date=short";
    private final Seq<String> resourceFiles = Vector.of("package.json", GULPFILE);

    private final Seq<Analysis> analysis = Vector.of(
            new Analysis("Hotspot", "hotspot-analysis"),
            new Analysis("Complexity trend", "sloc-trend-analysis", "--timeSplit=eom"),
            new Analysis("Coupling", "sum-of-coupling-analysis", "--timeSplit=eom"),
            new Analysis("System evolution", "sloc-trend-analysis", "--timeSplit=eom"),
            new Analysis("Commit message", "commit-message-analysis", "--minWordCount=1"),
            new Analysis("Developer coupling", "developer-coupling-analysis"),
            new Analysis("Developer effort", "developer-effort-analysis"),
            new Analysis("Knowledge Map", "knowledge-map-analysis")
    );

    @Parameter(defaultValue = "${project.build.directory}/code-forensics", readonly = true)
    private File outputDirectory;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "false")
    private boolean useProjectGulpFile;

    public void execute() {
        copyResources()
                .flatMap(this::retrieveGitStartDate)
                .flatMap(this::npmInstall)
                .flatMap(this::runAnalysis)
                .onFailure(exception -> getLog().error(exception))
                .onSuccess(context -> getLog().info("Analysis results available here : " + context.getOutputDirectory()));
    }

    private Try<Context> copyResources() {
        return Try.of(() -> {
            FileUtils.forceMkdir(outputDirectory);
            for (var file : resourceFiles) {
                copyResource(getClass().getClassLoader().getResource(file), file);
            }

            if (useProjectGulpFile) {
                copyFile(new File(getRootDirectoryPath() + "/" + GULPFILE));
            }
            return new Context(new File(getRootDirectoryPath()), outputDirectory);
        });
    }

    private void copyResource(URL source, String fileName) throws IOException {
        var fileToCreate = new File(outputDirectory + "/" + fileName);
        FileUtils.copyURLToFile(source, fileToCreate);
        updateTemplateFile(fileToCreate);
    }

    private void copyFile(File fileToCopy) throws IOException {
        var fileToCreate = new File(outputDirectory + "/" + GULPFILE);
        FileUtils.copyFile(fileToCopy, fileToCreate);
        updateTemplateFile(fileToCreate);
    }

    private void updateTemplateFile(File file) throws IOException {
        var content = FileUtils.readFileToString(file, UTF_8);
        var newContent = content.replace(REPOSITORY_PATH_VARIABLE, getRootDirectoryPath());

        FileUtils.writeStringToFile(file, newContent, UTF_8);
    }

    private Try<Context> retrieveGitStartDate(Context context) {
        return Try.of(() -> CommandUtils.execute(GIT_LOG_DATES_COMMAND, context.getGitRepositoryDirectory()).split("%n")[0])
                .map(startDate -> context.withDateFrom(LocalDate.parse(startDate)));
    }

    private Try<Context> npmInstall(Context context) {
        return Try.of(() -> CommandUtils.execute("npm install", outputDirectory, this::log))
                .map(context::append);
    }

    private Try<Context> runAnalysis(Context context) {
        return Try.of(() -> {
            for (var analyse : analysis) {
                var command = "gulp " + analyse.getTaskName() + " --targetFile=/ --dateFrom=" + context.getDateFrom() + " --dateTo=" + context.getDateTo() + analyse.getSpecialArgs();
                log("RUN " + analyse.getName() + " analysis");
                context.append(CommandUtils.execute(command, outputDirectory, this::log));
            }
            return context;
        });
    }

    private String getRootDirectoryPath() {
        return project.getBasedir().getAbsolutePath();
    }

    private void log(String text) { getLog().info(text); }
}