package org.c_base.c_beam_viewer.mqtt;

import java.io.*;
import java.security.*;
import java.security.cert.*;

import javax.net.ssl.*;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.*;
import org.bouncycastle.openssl.*;

public class SslUtil {
    static SSLSocketFactory getSocketFactory(InputStream certificate) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        // load CA certificate
        PEMParser parser = new PEMParser(new BufferedReader(new InputStreamReader(certificate)));
        X509Certificate cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate((X509CertificateHolder) parser.readObject());
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