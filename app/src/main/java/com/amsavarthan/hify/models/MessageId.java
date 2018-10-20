package com.amsavarthan.hify.models;

import android.support.annotation.NonNull;

/**
 * Created by amsavarthan on 22/2/18.
 */

public class MessageId {

    public String msgId;

    public <T extends MessageId> T withId(@NonNull final String id) {
        this.msgId = id;
        return (T) this;
    }

}
