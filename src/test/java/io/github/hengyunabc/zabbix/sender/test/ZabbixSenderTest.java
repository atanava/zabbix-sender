package io.github.hengyunabc.zabbix.sender.test;

import io.github.hengyunabc.zabbix.sender.DataObject;
import io.github.hengyunabc.zabbix.sender.SenderResult;
import io.github.hengyunabc.zabbix.sender.ZabbixSender;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.alibaba.fastjson.JSONObject;

@Ignore
public class ZabbixSenderTest {

	static String host;
	static int port;
	static String webserverHost;
	static String key;

	@BeforeClass
	public static void setup() throws IOException {
		Properties props = loadProps();

		host = props.getProperty("zabbix.host");
		port = Integer.parseInt(props.getProperty("zabbix.port"));
		webserverHost = props.getProperty("zabbix.webserver.host");
		key = props.getProperty("zabbix.key");
	}

	@Test
	public void test_LLD_rule() throws IOException {
		ZabbixSender zabbixSender = new ZabbixSender(host, port);

		DataObject dataObject = new DataObject();
		dataObject.setHost(webserverHost);
		dataObject.setKey(key);

		JSONObject data = new JSONObject();
		List<JSONObject> aray = new LinkedList<JSONObject>();
		JSONObject xxx = new JSONObject();
		xxx.put("hello", "hello");

		aray.add(xxx);
		data.put("data", aray);

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
	public void test() throws IOException {
		ZabbixSender zabbixSender = new ZabbixSender(host, port);

		DataObject dataObject = new DataObject();
		dataObject.setHost(webserverHost);
		dataObject.setKey(key);

		dataObject.setValue("10");
		dataObject.setClock(System.currentTimeMillis()/1000);
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
		stream = ZabbixSenderTest.class.getClassLoader().getResourceAsStream(path);
		if (stream == null) {
			throw new RuntimeException("Failed to load properties from " + path);
		}
		Properties props = new Properties();
		props.load(stream);
		return props;
	}

}
