package com.rendertweaks.apoli;

/** Added to Apoli's {@code PowerHolderComponentImpl}. */
public interface PowerTypeIndexHolder {

    /** Every power of this component that is an instance of type, active or not, in Apoli's order. */
    Object[] renderTweaks$powersOf(Class<?> type);
}
