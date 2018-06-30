package com.amsavarthan.hify.models;

import android.support.annotation.NonNull;

/**
 * Created by amsavarthan on 22/2/18.
 */

public class UserId {

    public String userId;

    public <T extends UserId> T withId(@NonNull final String id){
        this.userId=id;
        return (T)this;
    }

}
