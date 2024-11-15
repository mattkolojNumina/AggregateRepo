package com.numinagroup.androidtcpclient.serviceobjects;

import android.graphics.Color;

/**
 * This class represents a pickrate message that has been sent by the server.
 * A pickrate message has a few pieces of content. It has a message text, a text
 * color and a background color.
 **/
public class PickRate {

    /**
     * Variables used to hold the contents of a PickRate message
     **/
    private final String messageText;
    private final int textColor;
    private final int backgroundColor;

    /**
     * Constructor for the PickRate class
     **/
    public PickRate(String messageText, String textColor, String backgroundColor) {
        this.messageText = messageText;
        this.textColor = extractColorFromString(textColor);
        this.backgroundColor = extractColorFromString(backgroundColor);
    }

    /**
     * Method to translate the hexcode input string from the server into an int that
     * the textview can use to change the color of the background or text color. We do
     * that by splitting the string into the four hex values for argb, and then we call
     * Color.argb() which gives us an int representation of the argb values we input.
     *
     * Example string from server: FFF06F53
     * a:FF (FF represents full opacity)
     * r:F0
     * g:6F
     * b:53
     *
     * @param inputString Hexcode color code from the server
     **/
    public int extractColorFromString(String inputString) {
        int a = Integer.parseInt(inputString.substring(0,2), 16);
        int r = Integer.parseInt(inputString.substring(2,4), 16);
        int g = Integer.parseInt(inputString.substring(4,6), 16);
        int b = Integer.parseInt(inputString.substring(6), 16);

        return new Color().argb(a,r,g,b);
    }

    /**
     * Getter methods for the message text, text color, and background color
     **/
    public String getMessageText() { return messageText; }
    public int getTextColor() { return textColor; }
    public int getBackgroundColor() { return backgroundColor; }
}
