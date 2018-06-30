package com.amsavarthan.hify.models;

import android.support.annotation.NonNull;

/**
 * Created by amsavarthan on 22/2/18.
 */

public class AcceptedValue {

    public boolean accepted;

    public <T extends AcceptedValue> T withAccepted(@NonNull final boolean id) {
        this.accepted = id;
        return (T) this;
    }

}
