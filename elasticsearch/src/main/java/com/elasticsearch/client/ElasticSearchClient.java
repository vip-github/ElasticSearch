package com.elasticsearch.client;

import java.net.InetSocketAddress;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

/**
 * ES连接客户端
 * 
 * @author czq
 * @date 2017年3月16日 下午3:58:23
 */
public class ElasticSearchClient {

	public static void main(String[] args) {
		ElasticSearchClient.getInstance().getClient();
	}

	private static TransportClient client = null;

	private static ElasticSearchClient instance = null;

	public static ElasticSearchClient getInstance() {
		if (null == instance) {
			instance = new ElasticSearchClient();
		}
		return instance;
	}

	@SuppressWarnings({ "unchecked", "resource" })
	private ElasticSearchClient() {
		if (null == client) {
			Settings settings = Settings.builder().put("cluster.name", "elasticsearch").build();
			client = new PreBuiltTransportClient(settings)
					.addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress("localhost", 9300)));
		}
	}

	public TransportClient getClient() {
		return client;
	}

	public void close() {
		if (null != client) {
			client.close();
		}
	}
}
