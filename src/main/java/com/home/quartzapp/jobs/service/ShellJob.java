package com.home.quartzapp.jobs.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShellJob extends QuartzJobBean {
    @Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        int exitCode;
        
        String command = "dir";
        
        log.info("============================================");
        try {
            exitCode = processBuilder(null, command, false);
            log.info("Sehll Command Exit Code: {}", exitCode);
            } catch (IOException e) {
            log.error("IOException: [{}] command failed. {}", command, e);
        } catch (InterruptedException e) {
            log.error("InterruptedException: [{}] command failed. {}", command, e);
        }
        log.info("============================================");
    }

    private int processBuilder(String workingDir, String command, boolean stdoutToLog) throws IOException, InterruptedException {
        boolean isWindows;
        File cwd;


        if(workingDir != null) {
            cwd = new File(workingDir);
        }
        else {
            cwd = new File(System.getProperty("user.home"));
        }

        isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(cwd);
        if (isWindows) {
            builder.command("cmd.exe", "/c", command);
        } else {
            builder.command("sh", "-c", command);
        }

        log.info("Working Dir: {}", cwd.getAbsolutePath());
        log.info("Command    : {}", command);

        Process process = builder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            if(stdoutToLog) {
                log.info(line);
            }
        }

        int exitCode = process.waitFor();

        return exitCode;
    }
}
