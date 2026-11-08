package com.rendertweaks.apoli;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One entity's powers, grouped by the type they were asked for. Built from a snapshot of Apoli's
 * power map and tagged with the map's version, so a holder can tell when it is stale. Arrays keep
 * the map's iteration order, which is the order Apoli's own lists come out in.
 */
public final class PowerTypeIndex {

    private final int version;
    private final Object[] powers;
    private final ConcurrentHashMap<Class<?>, Object[]> byType = new ConcurrentHashMap<>();

    public PowerTypeIndex(int version, Object[] powers) {
        this.version = version;
        this.powers = powers;
    }

    public int version() {
        return version;
    }

    /** Every power that is an instance of type, active or not. */
    public Object[] ofType(Class<?> type) {
        return byType.computeIfAbsent(type, this::filter);
    }

    private Object[] filter(Class<?> type) {
        return Arrays.stream(powers).filter(type::isInstance).toArray();
    }
}
