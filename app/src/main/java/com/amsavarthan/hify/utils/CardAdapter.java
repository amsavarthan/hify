package com.amsavarthan.hify.utils;

import android.support.v7.widget.CardView;

/**
 * Created by amsavarthan on 1/3/18.
 */

public interface CardAdapter {

    int MAX_ELEVATION_FACTOR = 8;

    float getBaseElevation();

    CardView getCardViewAt(int position);

    int getCount();

}
