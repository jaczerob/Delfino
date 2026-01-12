package dev.jaczerob.delfino.common.utilities;

import java.util.function.Supplier;

public class NullSafety {
    public static <T> T getOrNull(final Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (final Exception exc) {
            return null;
        }
    }
}
