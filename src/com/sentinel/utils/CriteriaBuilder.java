package com.sentinel.utils;

import android.location.Criteria;

public final class CriteriaBuilder {
    private static Criteria mCriteria;

    public CriteriaBuilder() {
        mCriteria = new Criteria();
    }

    public final CriteriaBuilder setAccuracy(int accuracy) {
        mCriteria.setAccuracy(accuracy);
        return this;
    }

    public final CriteriaBuilder setPowerRequirement(int level) {
        mCriteria.setPowerRequirement(level);
        return this;
    }

    public final CriteriaBuilder setAltitudeRequired(boolean altitudeRequired) {
        mCriteria.setAltitudeRequired(altitudeRequired);
        return this;
    }

    public final CriteriaBuilder setBearingRequired(boolean bearingRequired) {
        mCriteria.setBearingRequired(bearingRequired);
        return this;
    }

    public final CriteriaBuilder setSpeedRequired(boolean speedRequired) {
        mCriteria.setSpeedRequired(speedRequired);
        return this;
    }

    public final CriteriaBuilder setCostAllowed(boolean costAllowed) {
        mCriteria.setCostAllowed(costAllowed);
        return this;
    }

    public static final Criteria build() {
        return mCriteria;
    }
}
