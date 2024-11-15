package com.numinagroup.androidtcpclient.models;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class LoginUI extends BaseObservable {


    private String loginText = "";

    public String getLoginText() {
        return loginText;
    }

    @Bindable
    public void setLoginText(String loginText) {
        this.loginText = loginText;
    }


}
