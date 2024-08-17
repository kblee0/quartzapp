package com.home.quartzapp.quartzjobs.common;

import com.home.quartzapp.common.util.ExceptionUtil;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
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
import java.util.Optional;

@Setter
@Slf4j
public class RestClientJob extends QuartzJobBean {
    private String jobName = null;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        this.setJobName(context.getJobDetail().getKey().toString());

        StopWatch stopWatch = new StopWatch(context.getFireInstanceId());
        stopWatch.start(jobName);

        JobDataMap jobDataMap = context.getMergedJobDataMap();

        log.info("{} :: [JOB_START] url: {} {}", jobName, jobDataMap.get("method"), jobDataMap.get("url"));

        String method = null;
        String url = null;
        String requestBody = null;
        ResponseEntity<String> responseEntity;

        try {
            method = jobDataMap.getString("method");
            url = jobDataMap.getString("url");
            HashMap<String,String> headers = Optional.ofNullable((HashMap<String,String>)jobDataMap.get("headers")).orElse(new HashMap<>());
            requestBody = jobDataMap.getString("body");

            int connectTimeoutSec = jobDataMap.containsKey("connectTimeoutSec") ? jobDataMap.getInt("connectTimeoutSec") : 5;
            int requestTimeoutSec = jobDataMap.containsKey("requestTimeoutSec") ? jobDataMap.getInt("requestTimeoutSec") : 120;

            log.debug("{} :: RestClientRequest, url={} {}, Timeout={}/{}, body={}", jobName, method, url, connectTimeoutSec, requestTimeoutSec, requestBody);

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
        } catch (Throwable e) {
            log.error("{} :: RestClientError, url={} {}, body={}", jobName, method, url, requestBody);
            throw new JobExecutionException("RestClient request failed.", e, false);
        }

        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            String errorMessage = getErrorMessageByJsonPath(responseEntity.getBody(), jobDataMap.get("errorMessagePath").toString()).orElse(
                    String.format("RestClient status code is not 2xx :: statusCode=%d", responseEntity.getStatusCode().value())
            );
            throw new JobExecutionException(errorMessage, false);
        }
        log.debug("{} :: Response Body={}", jobName, responseEntity.getBody());

        JobDataMap resultDataMap = new JobDataMap();
        resultDataMap.put("status", responseEntity.getStatusCode());
        resultDataMap.put("headers", responseEntity.getHeaders());
        resultDataMap.put("body", responseEntity.getBody());

        context.getMergedJobDataMap().put("result", resultDataMap);

        stopWatch.stop();
        log.info("{} :: [JOB_FINISH] {}, statusCode: {}", jobName, stopWatch.shortSummary(), responseEntity.getStatusCode());
    }

    Optional<String> getErrorMessageByJsonPath(String body, String jsonPath) {
        if(body == null || jsonPath == null) return Optional.empty();

        try {
            ReadContext ctx = JsonPath.parse(body);
            Object obj = ctx.read(jsonPath);
            return Optional.ofNullable(obj.toString());
        } catch (Exception e) {
            log.error("getErrorMessageByJsonPath :: message: {}, exception: {}", e.getMessage(), ExceptionUtil.getStackTrace(e));
            return Optional.empty();
        }
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory(int connectTimeoutSec, int requestTimeoutSec) {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(connectTimeoutSec*1000);
        clientHttpRequestFactory.setConnectionRequestTimeout(requestTimeoutSec*1000);
        return clientHttpRequestFactory;
    }
}
