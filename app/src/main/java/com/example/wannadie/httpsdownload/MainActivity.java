package com.example.wannadie.httpsdownload;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class MainActivity extends AppCompatActivity {

    private TextView mContentTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContentTextView = (TextView) findViewById(R.id.tv_content);
        new Thread(){
            @Override
            public void run() {
                super.run();
                URL url = null;
                try {

                    // Load CAs from an InputStream
// (could be from a resource or ByteArrayInputStream or ...)
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
// From https://www.washington.edu/itconnect/security/ca/load-der.crt
                    AssetManager assetManager =getAssets();
                    InputStream open = assetManager.open("down360safecom.crt");
                    InputStream caInput = new BufferedInputStream(open);
                    Certificate ca;
                    try {
                        ca = cf.generateCertificate(caInput);
                        System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
                    } finally {
                        caInput.close();
                    }

// Create a KeyStore containing our trusted CAs
                    String keyStoreType = KeyStore.getDefaultType();
                    KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                    keyStore.load(null, null);
                    keyStore.setCertificateEntry("ca", ca);

// Create a TrustManager that trusts the CAs in our KeyStore
                    String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                    TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                    tmf.init(keyStore);

// Create an SSLContext that uses our TrustManager
                    SSLContext context = SSLContext.getInstance("TLS");
                    context.init(null, tmf.getTrustManagers(), null);

// Tell the URLConnection to use a SocketFactory from our SSLContext
//                    https://down.360safe.com/instbeta.exe
                    url = new URL(" https://down.360safe.com/instbeta.exe");
                    HttpsURLConnection urlConnection =
                            (HttpsURLConnection)url.openConnection();
//                    urlConnection.setSSLSocketFactory(context.getSocketFactory());
                    InputStream in = urlConnection.getInputStream();
//                    copyInputStreamToOutputStream(in);
                    readInputStream(in);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }


    public static byte[] readInputStream(InputStream input) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[1024];
            int len = 0;
            while((len = input.read(buffer)) != -1) {
                output.write(buffer, 0, len);
            }
        } catch (IOException e) {

            e.printStackTrace();
        }
        Log.e("AAAA", "AAAAAAAAAAAAAAAAAAAAAAAA");

        return output.toByteArray();
    }


    public  String copyInputStreamToOutputStream(InputStream is) {
        /*
          * To convert the InputStream to String we use the BufferedReader.readLine()
          * method. We iterate until the BufferedReader return null which means
          * there's no more data to read. Each line will appended to a StringBuilder
          * and returned as String.
          */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        final StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mContentTextView.setText(sb);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }
}
