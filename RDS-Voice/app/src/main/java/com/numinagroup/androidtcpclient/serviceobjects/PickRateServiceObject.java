package com.numinagroup.androidtcpclient.serviceobjects;

import android.graphics.Color;

/**
 * This class is used to help us get the information of a pickrate message
 * from the sever. The server sends us all the information in three strings.
 * We use this class to hold that information before we make it an actual
 * PickRate object.
 **/
public class PickRateServiceObject {

    /**
     * Variables used to hold the contents of a PickRate message
     **/
    private final String messageText;
    private final String textColorString;
    private final String backgroundColorString;

    /**
     * Constructor for the PickRateServiceObject class
     **/
    public PickRateServiceObject(String messageText, String textColorString, String backgroundColorString) {
        this.messageText = messageText;
        this.textColorString = textColorString;
        this.backgroundColorString = backgroundColorString;
    }

    /**
     * Getter methods for the message text, text color, and background color
     **/
    public String getMessageText() { return messageText; }
    public String getTextColorString() { return textColorString; }
    public String getBackgroundColorString() { return backgroundColorString; }
}
