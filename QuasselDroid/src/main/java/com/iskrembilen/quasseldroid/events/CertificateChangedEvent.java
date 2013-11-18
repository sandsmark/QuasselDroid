package com.iskrembilen.quasseldroid.events;

public class CertificateChangedEvent {

    public final String certificateHash;

    public CertificateChangedEvent(String certificateHash) {
        this.certificateHash = certificateHash;
    }

}
