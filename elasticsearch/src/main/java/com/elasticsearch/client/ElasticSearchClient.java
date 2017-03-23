package com.elasticsearch.client;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

/**
 * ES连接客户端
 * 
 * @author czq
 * @date 2017年3月16日 下午3:58:23
 */
public class ElasticSearchClient {
	Logger logger = LogManager.getLogger(ElasticSearchClient.class);

	private final static String indexName = "articles";

	public static void main(String[] args) {
		ElasticSearchClient client = ElasticSearchClient.getInstance();
		client.mapping(indexName, "news");
		//client.insertTestData();
		//client.search();
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
			client = new PreBuiltTransportClient(settings).addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress("localhost", 9300)));
		}
	}

	public void createIndex(String... index) {
		if (null == index || index.length == 0) {
			index = new String[] { indexName };
		}
		IndicesAdminClient indicesAdminClient = client.admin().indices();
		for (String name : index) {
			if (!indicesAdminClient.exists(new IndicesExistsRequest(name)).actionGet().isExists()) {
				indicesAdminClient.create(new CreateIndexRequest(name)).actionGet();
				logger.info(String.format("索引创建成功！indexName:%s", name));
			}
		}
	}

	public void mapping(String indexName, String mappingType) {
		try {
			createIndex(indexName);
			String mappingJson = FileUtils.readFileToString(new File("D:/work/git/branches/ElasticSearch/elasticsearch/resource/mapping/news-mapping.json"), "UTF-8");
			PutMappingRequest mapping = Requests.putMappingRequest(indexName).type(mappingType).source(mappingJson);
			client.admin().indices().putMapping(mapping).actionGet();
			logger.info(String.format("%s的mapping创建成功！mappingType:%s", indexName, mappingType));
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.toString());
		} finally {
			close();
		}
	}
	
	public void insertTestData(){
		try {
			String content = "尾单价格：5399元/人 剩余位置：余位有限 出团日期：1月16日、1月23日 出发城市：杭州(自行前往) 行程特色： 特别安排入住2晚或2晚以上温泉酒店, 享受日本最著名的北海道特色温泉! ☺纵览北海道所有精华景点，让您一次玩个够! ☺特别加游世界三大夜景之一的函馆山夜景! ☺搭乘东方航空豪华航班，直飞北海道，不再享转机颠簸之苦! ☺全程美食升级，日本地道定食、各种北海道风味美食无限! ☺【小樽浪漫游】:闲逛罗曼蒂克街道【小樽运河】，是小樽最代表性的景点，走在欧风建筑里，海风微拂，宁静斜 坡港町，浪漫不已。 预定须知： 1、请提前电话确认余位，如需预订需支付100元订金。若确认线路产品无余位，订金如数退还;若预定成功后由于个人原因而无法补足余款的，订金不退。 2、确定购买此旅游产品后两小时内支付全款。 订金与团款汇款账号 户名：浙江中信国际旅行社有限公司宁波分公司 账号：03002535218 开户行：上海银行宁波分行营业部 3、护照有效期需6个月以上，空白页2页以上。 4、咨询热线：18658259715(9：00-23：00，周末询位的网友，工作人员会在周一回复您准确余位) 产品由中信(宁波)国际旅行社采集，杭州某国际旅行社提供。 ";
			String title = "三一";
			String kekwords = "北海道";
			Map<String, String> source = new LinkedHashMap<String, String>();
			source.put("content", content);
			source.put("title", title);
			source.put("keywords", kekwords);
			String id = Hashing.md5().hashString(StringUtils.strip(title), Charsets.UTF_8).toString();
			client.prepareIndex(indexName, "news", id).setSource(source).get();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.toString());
		} finally {
			close();
		}
	}
	
	public void search(){
		try {
			long start = System.currentTimeMillis();
			SearchRequestBuilder builder = client.prepareSearch(indexName).setTypes("news");
			SearchResponse response = builder.setQuery(QueryBuilders.matchPhraseQuery("keywords", "目标管理工作")).get();
			SearchHits hits = response.getHits();
			if(null!=hits && hits.getHits().length>0)
			{
				for (SearchHit hit : hits.hits()) {
					System.out.println("得分："+hit.getScore());
					System.out.println(hit.getSource());
				}
			}
			long end = System.currentTimeMillis();
			System.out.println(String.format("耗时：%s秒", (end-start)/1000));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.toString());
		} finally {
			close();
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
