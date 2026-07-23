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
    private static final char FIRST_PRINTABLE_ASCII = ' ';
    private static final char LAST_PRINTABLE_ASCII = '~';
    private static final BigInteger PRINTABLE_ASCII_RADIX = BigInteger.valueOf(95);

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
        for (char c : (lower + upper).toCharArray()) {
            if (c < FIRST_PRINTABLE_ASCII || c > LAST_PRINTABLE_ASCII) {
                throw new IllegalArgumentException("only printable ASCII bounds are supported");
            }
        }
    }

    private static BigInteger encode(String value) {
        BigInteger result = BigInteger.ZERO;
        for (int index = 0; index < value.length(); index++) {
            result = result.multiply(PRINTABLE_ASCII_RADIX)
                    .add(BigInteger.valueOf(value.charAt(index) - FIRST_PRINTABLE_ASCII));
        }
        return result;
    }

    private static String decode(BigInteger value, int length) {
        char[] result = new char[length];
        BigInteger remaining = value;
        for (int index = length - 1; index >= 0; index--) {
            BigInteger[] quotientAndRemainder = remaining.divideAndRemainder(PRINTABLE_ASCII_RADIX);
            result[index] = (char) (quotientAndRemainder[1].intValue() + FIRST_PRINTABLE_ASCII);
            remaining = quotientAndRemainder[0];
        }
        return new String(result);
    }
}
