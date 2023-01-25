package io.github.hengyunabc.zabbix.sender.test;

import com.alibaba.fastjson2.JSONObject;
import io.github.hengyunabc.zabbix.sender.*;
import org.junit.*;

import javax.net.SocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

@Ignore
public class ZabbixSenderTlsTest {

    private static String host;
    private static int port;
    private static String webserverHost;
    private static String key;

    private static String keyStore;
    private static String keyStorePassword;
    private static String trustStore;
    private static String trustStorePassword;

    private ZabbixSender zabbixSender;


    @BeforeClass
    public static void init() throws IOException {
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

    @Before
    public void setup() {
        zabbixSender = new ZabbixSender(host, port, new SocketFactorySSL(new DefaultCertificateStorage(keyStore, keyStorePassword)));
    }

    @After
    public void destroy() {
        System.clearProperty("javax.net.ssl.keyStore");
        System.clearProperty("javax.net.ssl.keyStorePassword");
        System.clearProperty("javax.net.ssl.trustStore");
        System.clearProperty("javax.net.ssl.trustStorePassword");
    }

    @Test
    public void test_LLD_rule() throws IOException {
        DataObject dataObject = new DataObject();
        dataObject.setHost(webserverHost);
        dataObject.setKey(key);

        JSONObject data = new JSONObject();
        List<JSONObject> jsonObjects = new LinkedList<>();
        JSONObject object = new JSONObject();
        object.put("hello", "This is object");

        jsonObjects.add(object);
        data.put("data", jsonObjects);

        dataObject.setValue(data.toJSONString());
        dataObject.setClock(System.currentTimeMillis()/1000);

        assertResultIsSuccess(zabbixSender.send(dataObject));
    }

    @Test
    public void sendMultipleObjects() throws IOException {
        List<DataObject> objects = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            DataObject dataObject = new DataObject();
            dataObject.setHost(webserverHost);
            dataObject.setKey(key);
            JSONObject object = new JSONObject();
            object.put("send-multiple-objects-test", String.format("Object %d of %d", i + 1, 3));
            String jsonString = object.toJSONString();
            dataObject.setValue(jsonString);
            dataObject.setClock(System.currentTimeMillis()/1000);
            objects.add(dataObject);
        }
        assertResultIsSuccess(zabbixSender.send(objects));
    }

    /**
     * Pass VM params to CLI to run this test:
         -Djavax.net.ssl.keyStore=<path to keystore>
         -Djavax.net.ssl.keyStorePassword=<keystore password>
         -Djavax.net.ssl.trustStore=<path to trustStore>
         -Djavax.net.ssl.trustStorePassword=<trustStore password>
     * */
    @Ignore
    @Test
    public void send_WithKeyStore_FromVmParams() throws IOException {
        ZabbixSender zabbixSender = new ZabbixSender(host, port, new SocketFactorySSL());

        assertResultIsSuccess(sendData("cert-from-cli-vm-params-test", zabbixSender));
    }

    /**
     * Pass VM params to CLI to run this test:
         -Djavax.net.ssl.keyStore=<path to keystore>
         -Djavax.net.ssl.keyStorePassword=<keystore password>
         -Djavax.net.ssl.trustStore=<path to trustStore>
         -Djavax.net.ssl.trustStorePassword=<trustStore password>
     * */
    @Ignore
    @Test
    public void send_WithKeyStore_FromVmParamsMultipleTimes() throws InterruptedException {
        sendMultipleTimes("cert-from-cli-vm-params-send-multiple-times-test", null);
    }

    @Test
    public void sendWithKeyStore_FromPathOverwriteSysProps() throws IOException {
        overwriteSysProps();
        ZabbixSender zabbixSender = new ZabbixSender(host, port, new SocketFactorySSL());
        assertResultIsSuccess(sendData("cert-from-path-overwrite-sys-props-test", zabbixSender));
    }

    @Test
    public void sendWithKeyStore_FromPathOverwriteSysPropsMultipleTimes() throws InterruptedException {
        overwriteSysProps();
        sendMultipleTimes("cert-from-path-overwrite-sys-props-send-multiple-times-test", null);
    }

    @Test
    public void sendWithKeyStore_FromPath() throws IOException {
        assertResultIsSuccess(sendData("cert-from-path-test", zabbixSender));
    }

    @Test
    public void sendWithKeyStore_FromPathMultipleTimes() throws InterruptedException {
        sendMultipleTimes("cert-from-path-send-multiple-times-test", new DefaultCertificateStorage(keyStore, keyStorePassword));
    }

    private void sendMultipleTimes(final String data, final CertificateStorage cert) throws InterruptedException {
        Thread senderThread = new Thread(() -> {
            do {
                try {
                    SocketFactory socketFactory = cert == null ? new SocketFactorySSL() : new SocketFactorySSL(cert);
                    ZabbixSender zabbixSender = new ZabbixSender(host, port, socketFactory);
                    Thread.sleep(500L);
                    assertResultIsSuccess(sendData(data, zabbixSender));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
            while ( ! Thread.currentThread().isInterrupted());
        });
        senderThread.start();
        Thread.sleep(5000L);
        senderThread.interrupt();
    }

    private SenderResult sendData(String value, ZabbixSender zabbixSender) throws IOException {
        DataObject dataObject = new DataObject();
        dataObject.setHost(webserverHost);
        dataObject.setKey(key);
        dataObject.setClock(System.currentTimeMillis()/1000);
        dataObject.setValue(value);
        return zabbixSender.send(dataObject);
    }

    private void assertResultIsSuccess(SenderResult result) {
        assertTrue("Send fail!", result.success());
        System.out.println("Send success: " + result);
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

    private void overwriteSysProps() {
        System.setProperty("javax.net.ssl.keyStore", keyStore);
        System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
        System.setProperty("javax.net.ssl.trustStore", trustStore);
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
    }

}
