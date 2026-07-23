package com.baize.flux.connector.jdbc.core.split;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Splits fixed-width printable ASCII keys in binary lexical order.
 * Database collations are not assumed: callers must use a binary/ASCII compatible key column.
 */
public final class AsciiStringRangeSplitter implements ChunkSplitter<String> {
    @Override
    public List<Chunk<String>> split(String lower, String upper, int count) {
        validate(lower, upper, count);
        BigInteger low = encode(lower);
        BigInteger high = encode(upper);
        if (low.equals(high)) return Collections.singletonList(new Chunk<>(lower, upper, true));
        BigInteger width = high.subtract(low).add(BigInteger.valueOf(count - 1)).divide(BigInteger.valueOf(count));
        List<Chunk<String>> result = new ArrayList<>();
        BigInteger start = low;
        for (int i = 0; i < count && start.compareTo(high) < 0; i++) {
            BigInteger end = i == count - 1 ? high : start.add(width).min(high);
            result.add(new Chunk<>(decode(start, lower.length()), decode(end, lower.length()), end.equals(high)));
            start = end;
        }
        return Collections.unmodifiableList(result);
    }
    private static void validate(String lower, String upper, int count) {
        if (lower == null || upper == null || lower.length() == 0 || lower.length() != upper.length()) throw new IllegalArgumentException("ASCII bounds must be non-empty and have the same length");
        if (count <= 0 || lower.compareTo(upper) > 0) throw new IllegalArgumentException("chunkCount must be positive and bounds ordered");
        for (char c : (lower + upper).toCharArray()) if (c > 127) throw new IllegalArgumentException("only ASCII bounds are supported");
    }
    private static BigInteger encode(String value) { return new BigInteger(1, value.getBytes(java.nio.charset.StandardCharsets.US_ASCII)); }
    private static String decode(BigInteger value, int length) {
        byte[] source = value.toByteArray(); byte[] result = new byte[length];
        int sourceOffset = Math.max(0, source.length - length); int targetOffset = Math.max(0, length - source.length);
        System.arraycopy(source, sourceOffset, result, targetOffset, Math.min(length, source.length));
        return new String(result, java.nio.charset.StandardCharsets.US_ASCII);
    }
}
