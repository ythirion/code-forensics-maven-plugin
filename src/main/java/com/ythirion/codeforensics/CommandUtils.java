package com.ythirion.codeforensics;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;

public class CommandUtils {
    public static String execute(
            String command,
            File directory,
            Consumer<String> logger) throws IOException {
        logger.accept(command);
        return getResult(
                Runtime.getRuntime().exec(command, null, directory),
                logger
        );
    }

    public static String execute(
            String command,
            File directory) throws IOException {
        return getResult(
                Runtime.getRuntime().exec(command, null, directory),
                text -> {}
        );
    }

    private static String getResult(Process process,
                                    Consumer<String> logger) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder result = new StringBuilder();
        String newLine;

        while ((newLine = reader.readLine()) != null) {
            result.append(newLine + "%n");
            logger.accept(newLine);
        }
        return result.toString();
    }
}