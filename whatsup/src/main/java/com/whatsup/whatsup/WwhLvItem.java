package com.whatsup.whatsup;

/**
 * Created by alenin on 05/07/2014.
 */
public class WwhLvItem {
    private String event_title;
    private NonScrollableGridView nsgv;

    public WwhLvItem() { };

    public String getEvent_title() {
        return this.event_title;
    }

    public NonScrollableGridView getNsgv() {
        return this.nsgv;
    }

    public void setEvent_title(String event_title) {
        this.event_title = event_title;
    }

    public void setNsgv( NonScrollableGridView nsgv ) {
        this.nsgv = nsgv;
    }
}
