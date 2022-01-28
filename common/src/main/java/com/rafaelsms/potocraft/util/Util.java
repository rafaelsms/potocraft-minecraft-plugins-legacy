package com.rafaelsms.potocraft.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Util {

    // Private constructor
    private Util() {
    }

    /**
     * Get first item or fallback to another one
     *
     * @param first    first item to attempt to be returned
     * @param fallback fallback item
     * @param <T>      type of items
     * @return if first item is null, fallback otherwise first item
     */
    public static <T> @NotNull T getOrElse(@Nullable T first, @NotNull T fallback) {
        if (first == null) {
            return fallback;
        }
        return first;
    }

    /**
     * Get first item catching exceptions or fallback to another one
     *
     * @param first    first item to attempt to be returned
     * @param fallback fallback item
     * @param <T>      type of items
     * @return if first item is null, fallback otherwise first item
     */
    public static <T> @NotNull T getCatchingOrElse(@NotNull Supplier<T> first, @NotNull T fallback) {
        try {
            T returned = first.get();
            if (returned == null) {
                return fallback;
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    /**
     * Convert a collection of items to a list of another type.
     *
     * @param sList      collection to be converter
     * @param rsFunction converting function
     * @param <R>        type of input
     * @param <S>        type of output
     * @return a non-null possibly immutable list of converted items
     */
    public static <R, S> @NotNull List<R> convertList(@Nullable Collection<S> sList,
                                                      @NotNull Function<S, R> rsFunction) {
        if (sList == null) {
            return List.of();
        }
        ArrayList<R> rList = new ArrayList<>(sList.size());
        for (S s : sList) {
            rList.add(convert(s, rsFunction));
        }
        return rList;
    }

    /**
     * Convert a nullable item to another type.
     *
     * @param r          input object
     * @param rsFunction converting function
     * @param <R>        type of input
     * @param <S>        type of output
     * @return null if input is null, converted item otherwise
     */
    public static <R, S> @Nullable S convert(@Nullable R r, @NotNull Function<R, S> rsFunction) {
        if (r == null) {
            return null;
        }
        return rsFunction.apply(r);
    }

    /**
     * Convert a nullable item to another type.
     *
     * @param r          input object
     * @param rsFunction converting function
     * @param <R>        type of input
     * @param <S>        type of output
     * @return converted r object
     */
    public static <R, S> @NotNull S convertNonNull(@NotNull R r, @NotNull Function<R, S> rsFunction) {
        return Objects.requireNonNull(convert(r, rsFunction));
    }

    /**
     * Convert a nullable item to another type providing a fallback.
     *
     * @param r          input object
     * @param rsFunction converting function
     * @param fallback   fallback object if r is null
     * @param <R>        type of input
     * @param <S>        type of output
     * @return converted r item or fallback
     */
    public static <R, S> @NotNull S convertFallback(@Nullable R r,
                                                    @NotNull Function<R, S> rsFunction,
                                                    @NotNull S fallback) {
        if (r == null) {
            return fallback;
        }
        return Objects.requireNonNull(convert(r, rsFunction));
    }
}
