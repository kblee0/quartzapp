package com.home.quartzapp.jobs.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DisallowConcurrentExecution
public class ShellJob extends QuartzJobBean implements InterruptableJob {
    private Thread currentThread = null;
    private Process shellProcess = null;
    private JobDetail jobDetail = null;
    
    @Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        String cwd;
        String command;
        boolean outputToLog;
        int exitCode;
        
        currentThread = Thread.currentThread();
        jobDetail = context.getJobDetail();
        cwd = jobDetail.getJobDataMap().getString("cwd");
        command = jobDetail.getJobDataMap().getString("command");
        outputToLog = jobDetail.getJobDataMap().getBoolean("outputToLog");

        if(command == null) {
            throw new IllegalArgumentException(
                String.format("%s :: The \"command\" parameter is required.", jobDetail.getKey()));
        }
        try {
            exitCode = processBuilder(cwd, command, outputToLog);
            log.info("{} :: exitCode: {}", jobDetail.getKey(), exitCode);
        } catch (IOException e) {
            throw new JobExecutionException(
                String.format("%s :: [%s] command failed with IOException.", jobDetail.getKey(), command),
                e,
                false);
        } catch (InterruptedException e) {
            throw new JobExecutionException(
                String.format("%s :: [%s] command failed with InterruptedException.", jobDetail.getKey(), command),
                e,
                false);
        }
        if(exitCode != 0) {
            throw new JobExecutionException(
                String.format("%s :: The exit code of dir command is non-zero. (code=%d)", jobDetail.getKey(), exitCode),
                false);
        }
    }

    private int processBuilder(String workingDir, String command, boolean outputToLog) throws IOException, InterruptedException {
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

        log.info("{} :: cwd: {}, comand: {}", jobDetail.getKey(), cwd.getAbsolutePath(), command);

        shellProcess = builder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(shellProcess.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            if(outputToLog) {
                log.info(line);
            }
        }

        int exitCode = shellProcess.waitFor();

        return exitCode;
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        log.info("{} :: Job interrupt", jobDetail.getKey());
        
            destoryProcessHandleTree(shellProcess.toHandle());
            currentThread.interrupt();
    }

    private void destoryProcessHandleTree(ProcessHandle handle) {
        handle.descendants().forEach((child) -> destoryProcessHandleTree(child));
        
        String commandLine = handle.info().command().orElse("(null)");

        if(handle.info().arguments().isPresent()) {
            commandLine += " " + String.join(" ", handle.info().arguments().get());
        }
        log.info("{} :: destroy process. pid: {}, command: {}",
            jobDetail.getKey(),
            handle.pid(), 
            commandLine);
        handle.destroy();
    }
}
