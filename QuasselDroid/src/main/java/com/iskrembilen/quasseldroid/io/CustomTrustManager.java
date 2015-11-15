/*
    QuasselDroid - Quassel client for Android
    Copyright (C) 2015 Ken Børge Viktil
    Copyright (C) 2015 Magnus Fjell
    Copyright (C) 2015 Martin Sandsmark <martin.sandsmark@kde.org>

    This program is free software: you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation, either version 3 of the License, or (at your option)
    any later version, or under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either version 2.1 of
    the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License and the
    GNU Lesser General Public License along with this program.  If not, see
    <http://www.gnu.org/licenses/>.
 */

package com.iskrembilen.quasseldroid.io;

import android.util.Log;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * A custom trust manager for the SSL socket so we can
 * let the user manually verify certificates.
 *
 * @author sandsmark
 */
class CustomTrustManager implements javax.net.ssl.X509TrustManager {
    /**
     *
     */
    private final CoreConnection coreConnection;
    /*
     * The default X509TrustManager returned by SunX509.  We'll delegate
     * decisions to it, and fall back to the logic in this class if the
     * default X509TrustManager doesn't trust it.
     */
    private X509TrustManager defaultTrustManager;

    CustomTrustManager(CoreConnection coreConnection) throws GeneralSecurityException {
        this.coreConnection = coreConnection;
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        tmf.init((KeyStore)null);

        TrustManager tms[] = tmf.getTrustManagers();

		/*
         * Iterate over the returned trustmanagers, look
		 * for an instance of X509TrustManager.  If found,
		 * use that as our "default" trust manager.
		 */
        for (int i = 0; i < tms.length; i++) {
            if (tms[i] instanceof X509TrustManager) {
                defaultTrustManager = (X509TrustManager) tms[i];
                return;
            }
        }

        throw new GeneralSecurityException("Couldn't initialize certificate management!");
    }

    /*
     * Delegate to the default trust manager.
     */
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        try {
            defaultTrustManager.checkClientTrusted(chain, authType);
        } catch (CertificateException excep) {
            Log.e(CustomTrustManager.class.getName(), "checkClientTrusted failed", excep);
        }
    }

    /*
     * Delegate to the default trust manager.
     */
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException, NewCertificateException {
        try {
            defaultTrustManager.checkServerTrusted(chain, authType);
        } catch (CertificateException excep) {
			/* Here we either check the certificate against the last stored one,
			 * or throw a security exception to let the user know that something is wrong.
			 */
            String hashedCert = hash(chain[0].getEncoded());
            QuasselDbHelper dbHelper = new QuasselDbHelper(coreConnection.applicationContext);
            dbHelper.open();
            String storedCert = dbHelper.getCertificate(coreConnection.getCoreId());
            dbHelper.close();
            if (storedCert != null) {
                if (!storedCert.equals(hashedCert)) {
                    throw new CertificateException(hashedCert);
                }
            } else {
                throw new NewCertificateException(hashedCert);
                //System.out.println("Storing new certificate: " + hashedCert);
                //preferences.edit().putString("certificate", hashedCert).commit();
            }
        }
    }

    /**
     * Java sucks.
     *
     * @param s The bytes to hash
     * @return a hash representing the input bytes.
     */
    private String hash(byte[] s) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("SHA1");
            digest.update(s);
            byte messageDigest[] = digest.digest();
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }


    /*
     * Merely pass this through.
     */
    public X509Certificate[] getAcceptedIssuers() {
        return defaultTrustManager.getAcceptedIssuers();
    }

    static class NewCertificateException extends CertificateException {
        private String hashedCert;

        public NewCertificateException(String hashedCert) {
            this.hashedCert = hashedCert;
        }

        public String hashedCert() {
            return this.hashedCert;
        }
    }
}