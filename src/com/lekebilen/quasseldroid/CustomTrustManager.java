package com.lekebilen.quasseldroid;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

class CustomTrustManager implements javax.net.ssl.X509TrustManager {
     /*
      * The default X509TrustManager returned by SunX509.  We'll delegate
      * decisions to it, and fall back to the logic in this class if the
      * default X509TrustManager doesn't trust it.
      */
     X509TrustManager defaultTrustManager;

     CustomTrustManager() throws GeneralSecurityException {
         // create a "default" JSSE X509TrustManager.

         KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
         //ks.load(new FileInputStream("trustedCerts"),
         //    "passphrase".toCharArray());

         TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
         tmf.init(ks);

         TrustManager tms [] = tmf.getTrustManagers();

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

         /*
          * Find some other way to initialize, or else we have to fail the
          * constructor.
          */
         throw new GeneralSecurityException("Couldn't initialize");
     }

     /*
      * Delegate to the default trust manager.
      */
     public void checkClientTrusted(X509Certificate[] chain, String authType)
                 throws CertificateException {
         try {
             defaultTrustManager.checkClientTrusted(chain, authType);
         } catch (CertificateException excep) {

         }
     }

     /*
      * Delegate to the default trust manager.
      */
     public void checkServerTrusted(X509Certificate[] chain, String authType)
                 throws CertificateException {
         try {
             defaultTrustManager.checkServerTrusted(chain, authType);
         } catch (CertificateException excep) {
        	 if (!CoreConnection.trustCertificate(chain[0].getEncoded())) {
        		 throw new CertificateException();
        	 }
         }
     }

     /*
      * Merely pass this through.
      */
     public X509Certificate[] getAcceptedIssuers() {
         return defaultTrustManager.getAcceptedIssuers();
     }
	
}