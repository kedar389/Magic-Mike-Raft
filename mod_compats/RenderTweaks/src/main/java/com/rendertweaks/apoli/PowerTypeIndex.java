package com.rendertweaks.apoli;

import java.util.Arrays;

/**
 * One entity's powers, grouped by the type they were asked for. Built from a snapshot of Apoli's
 * power map and tagged with the map's version, so a holder can tell when it is stale. Arrays keep
 * the map's iteration order, which is the order Apoli's own lists come out in.
 *
 * <p>Only a handful of power types are ever asked for, so they live in two small parallel arrays
 * searched by identity, replaced as a whole when a type is added. Readers on other threads see
 * either the old or the new pair, both correct.
 */
public final class PowerTypeIndex {

    private static final Object[] NONE = new Object[0];

    private final int version;
    private final Object[] powers;
    private volatile Entries entries = new Entries(new Class<?>[0], new Object[0][]);

    public PowerTypeIndex(int version, Object[] powers) {
        this.version = version;
        this.powers = powers;
    }

    public int version() {
        return version;
    }

    /** Every power that is an instance of type, active or not. */
    public Object[] ofType(Class<?> type) {
        Entries current = entries;
        Class<?>[] types = current.types;
        for (int i = 0; i < types.length; i++) {
            if (types[i] == type) {
                return current.powers[i];
            }
        }
        return add(type);
    }

    private synchronized Object[] add(Class<?> type) {
        Entries current = entries;
        for (int i = 0; i < current.types.length; i++) {
            if (current.types[i] == type) {
                return current.powers[i];
            }
        }
        Object[] matching = filter(type);
        int size = current.types.length;
        Class<?>[] types = Arrays.copyOf(current.types, size + 1);
        Object[][] byType = Arrays.copyOf(current.powers, size + 1);
        types[size] = type;
        byType[size] = matching;
        entries = new Entries(types, byType);
        return matching;
    }

    private Object[] filter(Class<?> type) {
        int count = 0;
        for (Object power : powers) {
            if (type.isInstance(power)) {
                count++;
            }
        }
        if (count == 0) {
            return NONE;
        }
        Object[] matching = new Object[count];
        int i = 0;
        for (Object power : powers) {
            if (type.isInstance(power)) {
                matching[i++] = power;
            }
        }
        return matching;
    }

    private record Entries(Class<?>[] types, Object[][] powers) {}
}
