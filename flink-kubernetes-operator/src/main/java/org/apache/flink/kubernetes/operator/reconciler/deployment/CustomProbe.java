package org.apache.flink.kubernetes.operator.reconciler.deployment;

import org.apache.flink.kubernetes.operator.api.AbstractFlinkResource;
import org.apache.flink.kubernetes.operator.controller.FlinkResourceContext;

// TODO: Do not Commit this shit now.
/** Per-deployment Custom Probe instance. */
public interface CustomProbe<RESOURCE extends AbstractFlinkResource<?, ?>> {
    /**
     * Function To probe yalla.*
     *
     * @param ctx ctx.
     * @return boolean.
     */
    boolean probe(FlinkResourceContext<RESOURCE> ctx);

    void reset();
}
