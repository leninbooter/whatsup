package com.whatsup.whatsup;

import android.graphics.drawable.Drawable;

/**
 * Created by alenin on 05/07/2014.
 */
public class WwhGvItem {
    Drawable image;

    public WwhGvItem() {}

    public WwhGvItem( Drawable image ) {
        super();
        this.image = image;
    }

    public void setImage( Drawable image ) {
        this.image = image;
    }

    public Drawable getImage() {
        return this.image;
    }
}
