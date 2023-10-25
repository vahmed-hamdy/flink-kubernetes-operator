package org.apache.flink.kubernetes.operator.reconciler.deployment;

import org.apache.flink.api.common.JobID;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.kubernetes.operator.api.AbstractFlinkResource;
import org.apache.flink.kubernetes.operator.service.FlinkService;
import org.apache.flink.runtime.rest.messages.EmptyRequestBody;
import org.apache.flink.runtime.rest.messages.JobExceptionsHeaders;
import org.apache.flink.runtime.rest.messages.JobExceptionsInfoWithHistory;
import org.apache.flink.runtime.rest.messages.JobIDPathParameter;
import org.apache.flink.runtime.rest.messages.job.JobExceptionsMessageParameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Trigger Fetcher.* */
public class JobExceptionTriggerFetcher
        implements ProbeTriggerFetcher<AbstractFlinkResource<?, ?>> {
    private static final Logger LOG = LoggerFactory.getLogger(JobExceptionTriggerFetcher.class);

    private final String exceptionPattern;

    public JobExceptionTriggerFetcher(String exceptionPattern) {
        this.exceptionPattern = exceptionPattern;
    }

    @Override
    public boolean evaluateTrigger(
            FlinkService flinkService, Configuration conf, AbstractFlinkResource<?, ?> cr) {
        LOG.info("Fetching job exceptions");
        if (Optional.ofNullable(cr.getStatus().getJobStatus().getJobId()).isEmpty()) {
            return false;
        }
        var jobId = JobID.fromHexString(cr.getStatus().getJobStatus().getJobId());
        final Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put(JobIDPathParameter.KEY, jobId.toString());
        var parameters = new JobExceptionsMessageParameters();
        parameters.getPathParameters().stream()
                .filter(p -> p.getKey().equals(JobIDPathParameter.KEY))
                .forEach(p -> ((JobIDPathParameter) p).resolve(jobId));

        try (var restClient = flinkService.getClusterClient(conf)) {
            var responseBody =
                    restClient
                            .sendRequest(
                                    JobExceptionsHeaders.getInstance(),
                                    parameters,
                                    EmptyRequestBody.getInstance())
                            .get();

            return verifyPattern(responseBody.getExceptionHistory());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean verifyPattern(JobExceptionsInfoWithHistory.JobExceptionHistory history) {
        //        return history.getEntries().stream()
        //                .map(rootExceptionInfo ->
        // rootExceptionInfo.getExceptionName().contains(exceptionPattern))
        //                .reduce(false, (b1, b2) -> b1 || b2);

        var rootExceptionInfoCollections = history.getEntries();
        if (rootExceptionInfoCollections.isEmpty()) {

            LOG.info("EMPTYYYYYYYYYY");
            return true;
        }
        for (var exception : rootExceptionInfoCollections) {
            LOG.info(exception.getExceptionName() + " With toString " + exception.toString());
            if (exception.toString().contains(exceptionPattern)
                    || exception.getExceptionName().contains(exceptionPattern)) {
                return true;
            }
        }

        return false;
    }
}
