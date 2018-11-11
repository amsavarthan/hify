package com.amsavarthan.hify.feature_ai.models;

import android.support.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class QuestionID {

    @Exclude
    public String Answered_doc_id;

    public <T extends QuestionID> T withId(@NonNull final String id) {
        this.Answered_doc_id = id;
        return (T) this;
    }


}
