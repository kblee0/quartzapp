package com.home.quartzapp.quartzjobs.common;

import com.home.quartzapp.common.exception.ErrorCodeException;
import com.home.quartzapp.common.util.JsonPathExtractor;
import com.home.quartzapp.quartzjobs.util.JobDataMapWrapper;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Setter
@Slf4j
public class RestClientJob extends QuartzJobBean {
    private String jobName;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        this.setJobName(context.getJobDetail().getKey().toString());

        // StopWatch - start
        StopWatch stopWatch = new StopWatch(context.getFireInstanceId());
        stopWatch.start(jobName);

        // JobDataMap Check
        JobDataMapWrapper jobDataMap = new JobDataMapWrapper(context.getMergedJobDataMap());

        String method = jobDataMap.getString("method").orElseThrow( () -> new ErrorCodeException("QJBE0001", "method"));
        String url = jobDataMap.getString("url").orElseThrow(() -> new ErrorCodeException("QJBE0001", "url"));
        String requestBody = jobDataMap.getString("method").orElse(null);
        Map<String,String> headers = jobDataMap.get("headers", Map.class).orElse(new HashMap<>());
        int connectTimeoutSec = jobDataMap.getInteger("connectTimeoutSec").orElse(5);
        int requestTimeoutSec = jobDataMap.getInteger("requestTimeoutSec").orElse(120);

        log.debug("{} :: RestClientRequest, url={} {}, Timeout={}/{}, body={}", jobName, method, url, connectTimeoutSec, requestTimeoutSec, requestBody);

        // Main process
        ResponseEntity<String> responseEntity;

        try {
            RestClient restClient = RestClient.builder()
                    .requestFactory(getClientHttpRequestFactory(connectTimeoutSec,requestTimeoutSec))
                    .build();

            RestClient.RequestBodySpec requestBodySpec = restClient.method(HttpMethod.valueOf(method)).uri(url);
            headers.keySet().forEach(h -> requestBodySpec.header(h, headers.get(h)));
            if(requestBody != null) requestBodySpec.body(requestBody);

            responseEntity = requestBodySpec
                    .retrieve()
                    .onStatus(HttpStatusCode::isError,
                            (req, res) -> log.error("{} :: RestClientError.onStatus.isError, status={}", jobName, res.getStatusCode().value()))
                    .toEntity(String.class);
        } catch (Exception e) {
            log.error("{} :: RestClientError, url={} {}, body={}", jobName, method, url, requestBody);
            throw new ErrorCodeException("QJBE0002", e);
        }

        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            String errorMessage;
            try {
                errorMessage = jobDataMap.getString("errorMessagePath")
                        .flatMap(path -> JsonPathExtractor.parse(responseEntity.getBody()).get(path))
                        .orElse("RestClient status code is not 2xx :: statusCode=" + responseEntity.getStatusCode());
            } catch(Exception e) {
                throw new ErrorCodeException("QJBE0004", e);
            }
            throw new ErrorCodeException("QJBE0003", new Exception(errorMessage));
        }

        log.debug("{} :: Response Body={}", jobName, responseEntity.getBody());

        // Set Result Map
        context.getMergedJobDataMap().put("result", JobDataMapWrapper.create()
                .put("status", responseEntity.getStatusCode())
                .put("headers", responseEntity.getHeaders())
                .put("body", responseEntity.getBody())
                .getJobDataMap()
        );

        stopWatch.stop();
        log.info("{} :: [JOB_FINISH] {}, statusCode: {}", jobName, stopWatch.shortSummary(), responseEntity.getStatusCode());
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory(int connectTimeoutSec, int requestTimeoutSec) {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(connectTimeoutSec*1000);
        clientHttpRequestFactory.setConnectionRequestTimeout(requestTimeoutSec*1000);
        return clientHttpRequestFactory;
    }
}
