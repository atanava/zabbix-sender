package io.github.hengyunabc.zabbix.sender.test;

import com.alibaba.fastjson.JSONObject;
import io.github.hengyunabc.zabbix.sender.*;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class ZabbixSenderTlsTest {

    static String host;
    static int port;
    static String webserverHost;
    static String key;

    static String keyStore;
    static String keyStorePassword;
    static String trustStore;
    static String trustStorePassword;


    @BeforeClass
    public static void setup() throws IOException {
        Properties props = loadProps();

        host = props.getProperty("zabbix.host");
        port = Integer.parseInt(props.getProperty("zabbix.port"));
        webserverHost = props.getProperty("zabbix.webserver.host");
        key = props.getProperty("zabbix.key");

        keyStore = props.getProperty("cert.keyStore");
        keyStorePassword = props.getProperty("cert.keyStorePassword");
        trustStore = props.getProperty("cert.trustStore");
        trustStorePassword = props.getProperty("cert.trustStorePassword");
    }

    @Test
    public void test_LLD_rule() throws IOException, GeneralSecurityException {
        Socket socket = SocketFactory.createSSLSocket(new CertificateStorage(keyStore, keyStorePassword));
        ZabbixSender zabbixSender = new ZabbixSender(host, port, socket);

        DataObject dataObject = new DataObject();
        dataObject.setHost(webserverHost);
        dataObject.setKey(key);

        JSONObject data = new JSONObject();
        List<JSONObject> jsonObjects = new LinkedList<JSONObject>();
        JSONObject object = new JSONObject();
        object.put("hello", "This is object");

        jsonObjects.add(object);
        data.put("data", jsonObjects);

        dataObject.setValue(data.toJSONString());
        dataObject.setClock(System.currentTimeMillis()/1000);
        SenderResult result = zabbixSender.send(dataObject);

        System.out.println("result:" + result);
        if (result.success()) {
            System.out.println("send success.");
        } else {
            System.err.println("send fail!");
        }
    }

    @Test
    public void sendMultipleItems() throws IOException, GeneralSecurityException {
        Socket socket = SocketFactory.createSSLSocket(new CertificateStorage(keyStore, keyStorePassword));
        ZabbixSender zabbixSender = new ZabbixSender(host, port, socket);

        List<DataObject> objects = new ArrayList<DataObject>();
        for (int i = 0; i < 3; i++) {
            DataObject dataObject = new DataObject();
            dataObject.setHost(webserverHost);
            dataObject.setKey(key);
            JSONObject object = new JSONObject();
            object.put("send-multiple-objects-test", String.format("Object %d of %d", i + 1, 3));
            String jsonString = object.toJSONString();
            System.out.println("trying to send: " + jsonString);
            dataObject.setValue(jsonString);
            dataObject.setClock(System.currentTimeMillis()/1000);
            objects.add(dataObject);
        }
        SenderResult result = zabbixSender.send(objects);

        System.out.println("result:" + result);
        if (result.success()) {
            System.out.println("send success.");
        } else {
            System.err.println("send fail!");
        }

    }

    /**
     * Set VM params in CLI to run this test:
     * -Djavax.net.ssl.keyStore=<path to keystore>
     * -Djavax.net.ssl.keyStorePassword=<keystore password>
     * -Djavax.net.ssl.trustStore=<path to trustStore>
     * -Djavax.net.ssl.trustStorePassword=<trustStore password>
     * */
    @Ignore
    @Test
    public void send_WithKeyStore_FromVmParams() throws IOException, GeneralSecurityException {
        Socket socket = SocketFactory.createSSLSocket(null);
        ZabbixSender zabbixSender = new ZabbixSender(host, port, socket);

        DataObject dataObject = new DataObject();
        dataObject.setHost(webserverHost);
        dataObject.setKey(key);
        dataObject.setClock(System.currentTimeMillis()/1000);
        dataObject.setValue("cert-from-cli-vm-params-test");
        SenderResult result = zabbixSender.send(dataObject);

        System.out.println("result:" + result);
        if (result.success()) {
            System.out.println("send success.");
        } else {
            System.err.println("send fail!");
        }
    }

    @Test
    public void sendWithKeyStore_FromPath() throws IOException, GeneralSecurityException {
        Socket socket = SocketFactory.createSSLSocket(new CertificateStorage(keyStore, keyStorePassword));
        ZabbixSender zabbixSender = new ZabbixSender(host, port, socket);

        DataObject dataObject = new DataObject();
        dataObject.setHost(webserverHost);
        dataObject.setKey(key);
        dataObject.setClock(System.currentTimeMillis()/1000);
        dataObject.setValue("cert-from-path-test");
        SenderResult result = zabbixSender.send(dataObject);

        System.out.println("result:" + result);
        if (result.success()) {
            System.out.println("send success.");
        } else {
            System.err.println("send fail!");
        }
    }

    @Test
    public void sendWithKeyStore_FromPathOverwriteSysProps() throws IOException, GeneralSecurityException {
        Socket socket = SocketFactory.createSSLSocket(new CertificateStorage(keyStore, keyStorePassword, trustStore, trustStorePassword));
        ZabbixSender zabbixSender = new ZabbixSender(host, port, socket);

        DataObject dataObject = new DataObject();
        dataObject.setHost(webserverHost);
        dataObject.setKey(key);
        dataObject.setClock(System.currentTimeMillis()/1000);
        dataObject.setValue("cert-from-path-overwrite-sys-props-test");
        SenderResult result = zabbixSender.send(dataObject);

        System.out.println("result:" + result);
        if (result.success()) {
            System.out.println("send success.");
        } else {
            System.err.println("send fail!");
        }
    }

    private static Properties loadProps() throws IOException {
        InputStream stream;
        String path = "test.properties";
        stream = ZabbixSenderTlsTest.class.getClassLoader().getResourceAsStream(path);
        if (stream == null) {
            throw new RuntimeException("Failed to load properties from " + path);
        }
        Properties props = new Properties();
        props.load(stream);
        return props;
    }


}
