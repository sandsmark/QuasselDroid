package com.iskrembilen.quasseldroid.io;

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

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * A custom trust manager for the SSL socket so we can
 * let the user manually verify certificates.
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
	X509TrustManager defaultTrustManager;

	CustomTrustManager(CoreConnection coreConnection) throws GeneralSecurityException {
		this.coreConnection = coreConnection;
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
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
			SharedPreferences preferences = this.coreConnection.service.getSharedPreferences("CertificateStorage", Context.MODE_PRIVATE);
			if (preferences.contains("certificate")) { 
				if (!preferences.getString("certificate", "lol").equals(hashedCert)) {
					throw new CertificateException();
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
	 * @param s The bytes to hash
	 * @return a hash representing the input bytes.
	 */
	private String hash(byte[] s) {
		try {
			MessageDigest digest = java.security.MessageDigest.getInstance("SHA1");
			digest.update(s);
			byte messageDigest[] = digest.digest();
			StringBuffer hexString = new StringBuffer();
			for (int i=0; i<messageDigest.length; i++)
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