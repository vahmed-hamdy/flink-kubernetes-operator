package org.apache.flink.kubernetes.operator.reconciler.deployment;

import org.apache.flink.kubernetes.operator.api.AbstractFlinkResource;
import org.apache.flink.kubernetes.operator.api.FlinkDeployment;
import org.apache.flink.kubernetes.operator.controller.FlinkResourceContext;
import org.apache.flink.kubernetes.operator.utils.EventRecorder;

import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Dummy Impl.* */
public class DummyProbeImpl implements CustomProbe<FlinkDeployment> {
    private final String patter = "Some Dummy Exception";
    Logger LOG = LoggerFactory.getLogger(DummyProbeImpl.class);

    private final KubernetesClient kubernetesClient;
    private final EventRecorder eventRecorder;
    private final ProbeTriggerFetcher<AbstractFlinkResource<?, ?>> triggerFetcher;

    public DummyProbeImpl(KubernetesClient kubernetesClient, EventRecorder eventRecorder) {
        this.kubernetesClient = kubernetesClient;
        this.eventRecorder = eventRecorder;
        this.triggerFetcher = new JobExceptionTriggerFetcher(patter);
    }

    @Override
    public boolean probe(FlinkResourceContext<FlinkDeployment> ctx) {
        var conf = ctx.getObserveConfig();
        if (!triggerFetcher.evaluateTrigger(ctx.getFlinkService(), conf, ctx.getResource())) {
            return false;
        }
        LOG.info("Updating Resource");
        FlinkDeployment resource = ctx.getResource();
        if (resource.getSpec()
                        .getFlinkConfiguration()
                        .containsKey("taskmanager.memory.process.size")
                && resource.getSpec()
                        .getFlinkConfiguration()
                        .get("taskmanager.memory.process.size")
                        .equals("1080m")) {
            return true;
        }
        resource.getSpec().getFlinkConfiguration().put("taskmanager.memory.process.size", "1080m");
        kubernetesClient.resource(resource).patch();

        return true;
    }

    @Override
    public void reset() {}
}
