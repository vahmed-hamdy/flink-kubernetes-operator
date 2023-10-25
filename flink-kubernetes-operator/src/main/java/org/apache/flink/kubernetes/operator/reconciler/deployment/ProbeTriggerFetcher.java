package org.apache.flink.kubernetes.operator.reconciler.deployment;

import org.apache.flink.annotation.Experimental;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.kubernetes.operator.api.AbstractFlinkResource;
import org.apache.flink.kubernetes.operator.service.FlinkService;

/** Some Class.* */
@Experimental
public interface ProbeTriggerFetcher<T extends AbstractFlinkResource<?, ?>> {

    boolean evaluateTrigger(FlinkService flinkService, Configuration conf, T cr);
}
