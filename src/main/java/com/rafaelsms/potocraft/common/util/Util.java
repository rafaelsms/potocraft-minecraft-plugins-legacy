package com.rafaelsms.potocraft.common.util;

public final class Util {

    public static <T> T getOrElse(T first, T fallback) {
        if (first == null) {
            return fallback;
        }
        return first;
    }

    private Util() {
    }
}
