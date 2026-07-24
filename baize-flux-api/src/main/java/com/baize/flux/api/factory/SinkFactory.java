package com.baize.flux.api.factory;

import com.baize.flux.api.configuration.ReadonlyConfig;
import com.baize.flux.api.configuration.util.OptionRule;
import com.baize.flux.api.sink.Sink;
import com.baize.flux.api.sink.SinkFactoryContext;
import com.baize.flux.api.sink.SinkWriter;
import com.baize.flux.api.table.type.FluxRow;

/** 创建 Job 级不可变 Sink 的 Connector Factory。 */
public interface SinkFactory extends Factory {
    OptionRule optionRule();
    /** 在 Job 准备阶段仅调用一次。 */
    default Sink createSink(SinkFactoryContext context) {
        final SinkWriter<FluxRow> legacyWriter = createSink(context.getOptions());
        if (legacyWriter == null) {
            return null;
        }
        return new Sink() {
            @Override
            public SinkWriter<FluxRow> createWriter(com.baize.flux.api.sink.SinkWriterContext writerContext) {
                if (writerContext.getParallelism() != 1) {
                    throw new IllegalStateException("Legacy SinkFactory only supports parallelism of 1");
                }
                return legacyWriter;
            }
        };
    }

    /** 旧 Writer Factory 的短期兼容入口，新 Connector 不应实现此方法。 */
    @Deprecated
    default SinkWriter<FluxRow> createSink(ReadonlyConfig config) {
        throw new UnsupportedOperationException("SinkFactory must implement createSink(SinkFactoryContext)");
    }
}
