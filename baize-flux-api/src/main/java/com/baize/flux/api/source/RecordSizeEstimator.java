package com.baize.flux.api.source;

import com.baize.flux.api.table.type.FluxRow;

/**
 * Stable, deliberately approximate record memory-size estimator.
 *
 * <p>The result is intended for backpressure accounting rather than exact JVM heap measurement.
 */
public interface RecordSizeEstimator<T> {

    long UNKNOWN_RECORD_BYTES = 256L;

    long estimateBytes(T record);

    static long estimate(Object record) {
        if (record == null) {
            return 8L;
        }
        if (record instanceof FluxRow) {
            return ((FluxRow) record).estimatedSizeBytes();
        }
        if (record instanceof byte[]) {
            return 16L + ((byte[]) record).length;
        }
        if (record instanceof CharSequence) {
            return 40L + 2L * ((CharSequence) record).length();
        }
        if (record instanceof Number || record instanceof Boolean || record instanceof Character) {
            return 24L;
        }
        return UNKNOWN_RECORD_BYTES;
    }
}
