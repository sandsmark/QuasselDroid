package com.iskrembilen.quasseldroid.events;

public class NewCertificateEvent {

    public final String certificateString;

    public NewCertificateEvent(String certificateString) {
        this.certificateString = certificateString;
    }
}
