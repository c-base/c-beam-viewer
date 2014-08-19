package org.c_base.c_beam_viewer.mqtt;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;

import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.openssl.PEMParser;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class SslUtil {
    static SSLSocketFactory getSocketFactory(InputStream certificate) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        // load CA certificate
        PEMParser parser = new PEMParser(new BufferedReader(new InputStreamReader(certificate)));
        X509Certificate cert = new JcaX509CertificateConverter().setProvider("SC").getCertificate((X509CertificateHolder) parser.readObject());
        parser.close();

        // CA certificate is used to authenticate server
        KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
        caKs.load(null, null);
        caKs.setCertificateEntry("ca-certificate", cert);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(caKs);

        // finally, create SSL socket factory
        SSLContext context = SSLContext.getInstance("TLSv1");
        context.init(null, tmf.getTrustManagers(), null);

        return context.getSocketFactory();
    }
}