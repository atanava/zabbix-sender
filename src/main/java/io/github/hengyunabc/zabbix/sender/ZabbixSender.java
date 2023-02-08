package io.github.hengyunabc.zabbix.sender;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author hengyunabc
 *
 */
public class ZabbixSender {

	public static final int DEFAULT_TIMEOUT = 3 * 1000;

	private static final Logger logger = LoggerFactory.getLogger(ZabbixSender.class.getName());

    private static final Pattern PATTERN = Pattern.compile("[^0-9\\.]+");
    private final static Charset UTF8 = StandardCharsets.UTF_8;

    private String host;
	private int port;
	private int connectTimeout;
	private int socketTimeout;

	private SocketFactory socketFactory;

	public ZabbixSender(String host, int port) {
		this(host, port, SocketFactory.getDefault());
	}

	public ZabbixSender(String host, int port, SocketFactory socketFactory) {
		this(host, port, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, socketFactory);
	}

	public ZabbixSender(String host, int port, int connectTimeout, int socketTimeout) {
		this(host, port, connectTimeout, socketTimeout, SocketFactory.getDefault());
	}

	public ZabbixSender(String host, int port, int connectTimeout, int socketTimeout, SocketFactory socketFactory) {
		this.host = host;
		this.port = port;
		this.connectTimeout = connectTimeout;
		this.socketTimeout = socketTimeout;
		this.socketFactory = socketFactory;
	}

	public SenderResult send(DataObject dataObject) throws IOException {
		return send(dataObject, System.currentTimeMillis() / 1000);
	}

	/**
	 *
	 * @param dataObject
	 * @param clock
	 *            TimeUnit is SECONDS.
	 * @return
	 * @throws IOException
	 */
	public SenderResult send(DataObject dataObject, long clock) throws IOException {
		return send(Collections.singletonList(dataObject), clock);
	}

	public SenderResult send(List<DataObject> dataObjectList) throws IOException {
		return send(dataObjectList, System.currentTimeMillis() / 1000);
	}

	/**
	 *
	 * @param dataObjectList
	 * @param clock
	 *            TimeUnit is SECONDS.
	 * @return
	 * @throws IOException
	 */
	public SenderResult send(List<DataObject> dataObjectList, long clock) throws IOException {
		logger.debug("Trying to send data to Zabbix");
		SenderResult senderResult = new SenderResult();
		Socket socket = null;
		try {
			socket = socketFactory.createSocket();
			socket.setSoTimeout(socketTimeout);
			socket.connect(new InetSocketAddress(host, port), connectTimeout);

			InputStream inputStream = socket.getInputStream();
			OutputStream outputStream = socket.getOutputStream();

			SenderRequest senderRequest = new SenderRequest();
			senderRequest.setData(dataObjectList);
			senderRequest.setClock(clock);

			outputStream.write(senderRequest.toBytes());

			outputStream.flush();

			// normal responseData.length < 100
			byte[] responseData = new byte[512];

			int readCount = 0;

			while (true) {
				int read = inputStream.read(responseData, readCount, responseData.length - readCount);
				if (read <= 0) {
					break;
				}
				readCount += read;
			}

			if (readCount < 13) {
				// seems zabbix server return "[]"?
				senderResult.setbReturnEmptyArray(true);
			}

			// header('ZBXD\1') + len + 0
			// 5 + 4 + 4
			String jsonString = new String(responseData, 13, readCount - 13, UTF8);
			JSONObject json = JSON.parseObject(jsonString);
			String info = json.getString("info");
			// example info: processed: 1; failed: 0; total: 1; seconds spent:
			// 0.000053
			// after split: [, 1, 0, 1, 0.000053]
			String[] split = PATTERN.split(info);

			senderResult.setProcessed(Integer.parseInt(split[1]));
			senderResult.setFailed(Integer.parseInt(split[2]));
			senderResult.setTotal(Integer.parseInt(split[3]));
			senderResult.setSpentSeconds(Float.parseFloat(split[4]));

			logger.debug("Zabbix response: " + senderResult);
		}  finally {
			if (socket != null) {
				if (socket instanceof SSLSocket) {
					((SSLSocket) socket).getSession().invalidate();
				}
				socket.close();
			}
		}

		return senderResult;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public int getSocketTimeout() {
		return socketTimeout;
	}

	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	public SocketFactory getSocketFactory() {
		return socketFactory;
	}

	public void setSocketFactory(SocketFactory socketFactory) {
		this.socketFactory = socketFactory;
	}
}
