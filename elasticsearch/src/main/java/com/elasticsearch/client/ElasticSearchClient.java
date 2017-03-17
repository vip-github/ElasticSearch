package com.elasticsearch.client;

import java.net.InetSocketAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.client.IndicesAdminClient;
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
	Logger logger = LogManager.getLogger(ElasticSearchClient.class);
	public static void main(String[] args) {
		ElasticSearchClient client = ElasticSearchClient.getInstance();
		client.createIndex("articles");
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

	public void createIndex(String index) {
		IndicesAdminClient indicesAdminClient = client.admin().indices();
		if (!indicesAdminClient.exists(new IndicesExistsRequest(index)).actionGet().isExists()) {
			indicesAdminClient.create(new CreateIndexRequest(index)).actionGet();
			logger.info(String.format("索引创建成功！index:%s", index));
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
