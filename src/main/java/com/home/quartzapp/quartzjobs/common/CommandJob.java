package com.home.quartzapp.quartzjobs.common;

import com.home.quartzapp.common.exception.ErrorCodeException;
import com.home.quartzapp.quartzjobs.util.JobDataMapWrapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Optional;

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

        // StopWatch - start
        StopWatch stopWatch = new StopWatch(context.getFireInstanceId());
        stopWatch.start(jobName);

        // JobDataMap Check
        JobDataMapWrapper jobDataMap = new JobDataMapWrapper(context.getMergedJobDataMap());

        String cwd = jobDataMap.getString("cwd").orElse(System.getProperty("user.dir"));
        String command = jobDataMap.getString("command").orElseThrow(() -> new ErrorCodeException("QJBE0001", "command"));
        boolean outputToLog = jobDataMap.getBoolean("outputToLog").orElse(true);
        boolean checkExistCode = jobDataMap.getBoolean("checkExistCode").orElse(false);
        String charsetName = jobDataMap.getString("charsetName").orElse(Charset.defaultCharset().displayName());

        log.info("{} :: [JOB_START] cwd: {}, command: {}, outputToLog: {}", jobName, cwd, command, outputToLog);

        int exitCode;
        try {
            exitCode = processBuilder(context, cwd, command, outputToLog, charsetName);
            log.info("{} :: exitCode: {}", jobName, exitCode);
        } catch (IOException | InterruptedException e) {
            throw new ErrorCodeException("QJBE0002", e);
        }
        if(exitCode != 0) {
            log.warn("{} :: The exist code for the \"{}\" command is {}.", jobName, command, exitCode);
            if(checkExistCode) throw new ErrorCodeException("QJBE0004", new Exception("Exit code is " + exitCode));
        }
        stopWatch.stop();
        log.info("{} :: [JOB_FINISH] {}, exitCode: {}", jobName, stopWatch.shortSummary(), exitCode);
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

        isJobInterrupted = true;

        destroyProcessHandleTree(shellProcess.toHandle());

        log.info("[JOB_INTERRUPT] :: JobName: {}", jobName);
        currentThread.interrupt();
    }

    private void destroyProcessHandleTree(ProcessHandle handle) {
        handle.descendants().forEach(this::destroyProcessHandleTree);
        
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