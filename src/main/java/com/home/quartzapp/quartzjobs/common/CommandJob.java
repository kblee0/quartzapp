package com.home.quartzapp.quartzjobs.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

import lombok.Getter;
import lombok.Setter;
import org.quartz.*;
import org.springframework.scheduling.quartz.QuartzJobBean;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Getter
@Setter
@DisallowConcurrentExecution
@Component
@Slf4j
public class CommandJob extends QuartzJobBean implements InterruptableJob {
    private volatile boolean isJobInterrupted = false;
    private Process shellProcess = null;
    private String jobName = null;

    @Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        this.setJobName(context.getJobDetail().getKey().toString());

        JobDataMap jobDataMap = context.getMergedJobDataMap();

        log.info("{} :: [JOB_START] cwd: {}, command: {}, outputToLog: {}",
                jobName, jobDataMap.get("cwd"), jobDataMap.get("command"), jobDataMap.get("outputToLog"));

        String cwd = Optional.ofNullable(jobDataMap.getString("cwd")).orElse(System.getProperty("user.dir"));
        String command = Optional.ofNullable(jobDataMap.getString("command")).orElseThrow( () ->
                new IllegalArgumentException(String.format("%s :: The \"command\" parameter is required.", this.getJobName())));
        boolean outputToLog = jobDataMap.getBooleanValue("outputToLog");
        String charsetName = jobDataMap.getString("charsetName");

        int exitCode;
        try {
            exitCode = processBuilder(context, cwd, command, outputToLog, charsetName);
            log.info("{} :: exitCode: {}", jobName, exitCode);
        } catch (IOException e) {
            throw new JobExecutionException(String.format("%s :: [%s] command failed with IOException.", jobName, command), e.getCause(),false);
        } catch (InterruptedException e) {
            throw new JobExecutionException(String.format("%s :: [%s] command failed with InterruptedException: %s", jobName, command ,e.getMessage()),false);
        }
        log.info("{} :: [JOB_FINISH] exitCode: {}", jobName, exitCode);
    }

    private int processBuilder(JobExecutionContext context, String workingDir, String command, boolean outputToLog, String charsetName) throws IOException, InterruptedException {
        JobDetail jobDetail = context.getJobDetail();
        boolean isWindows;
        File cwd = new File(workingDir);

        isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(new File(workingDir));
        if (isWindows) {
            builder.command("cmd.exe", "/c", command);
        } else {
            builder.command("sh", "-c", command);
        }

        log.info("{} :: cwd: {}, command: {}", jobDetail.getKey(), cwd.getAbsolutePath(), command);

        shellProcess = builder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(shellProcess.getInputStream(), Optional.ofNullable(charsetName).orElse("UTF-8")));

        String line;
        while ((line = reader.readLine()) != null) {
            if(outputToLog) {
                log.info(line);
            }
        }

        return shellProcess.waitFor();
    }

    @Override
    public void interrupt() {
        Thread currentThread = Thread.currentThread();

        isJobInterrupted = true; //interrupt 되었다고 flag를 둔다

        destoryProcessHandleTree(shellProcess.toHandle());

        log.info("[JOB_INTERRUPT] :: JobName: {}", jobName);
        currentThread.interrupt(); //쓰레드가 일시 정지 상태이면 바로 깨워서 실행시킨다
    }

    private void destoryProcessHandleTree(ProcessHandle handle) {
        handle.descendants().forEach(this::destoryProcessHandleTree);
        
        String commandLine = handle.info().command().orElse("(null)");

        if(handle.info().arguments().isPresent()) {
            commandLine += " " + String.join(" ", handle.info().arguments().get());
        }
        log.info("{} :: destroy process. pid: {}, command: {}",
            jobName,
            handle.pid(), 
            commandLine);
        handle.destroy();
    }
}