package com.hdu.es;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.message.BasicHeader;
import org.apache.zookeeper.MultiResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.*;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class EsClient implements ApplicationListener<ContextRefreshedEvent> {

    //    private final static int CONNECT_TIMEOUT = 100;
//    private final static int SOCKET_TIMEOUT = 60*1000;
//    private final static int REQUEST_TIMEOUT = SOCKET_TIMEOUT;
//    private BasicHeader[] basicHeaders;
    private RestHighLevelClient restHighLevelClient;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            initClient();
        } catch (Exception e) {
            log.error("es cliten init exception", e);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        }
    }

    private void initClient() {
        log.info("es client init start");
//        basicHeaders = new BasicHeader[]{
//                new BasicHeader("Accept","application/json; charset=UTF-8")
//        };
//        RestClientBuilder builder = RestClient.builder(new HttpHost("172.20.10.3",9200,"http"));
//        builder.setDefaultHeaders(basicHeaders)
//                .setRequestConfigCallback((config)->{
//                    config.setConnectTimeout(CONNECT_TIMEOUT);
//                    config.setSocketTimeout(SOCKET_TIMEOUT);
//                    config.setConnectionRequestTimeout(REQUEST_TIMEOUT);
//                    return config;
//                });
        // Create the low-level client
        RestClient httpClient = RestClient.builder(
                new HttpHost("192.168.48.132", 9200)
        ).build();

        // Create the HLRC
        RestHighLevelClient hlrc = new RestHighLevelClientBuilder(httpClient)
                .setApiCompatibilityMode(true) // 开启兼容模式
                .build();
        restHighLevelClient = hlrc;
        log.info("es client init end");
    }

    public IndexResponse index(IndexRequest indexRequest) throws Exception {
        try {
            return restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("es.index exception, indexRequest:{}", indexRequest, e);
            throw e;
        }
    }

    public UpdateResponse update(UpdateRequest updateRequest) throws Exception {
        try {
            return restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("es.update exception, updateRequest:{}", updateRequest, e);
            throw e;
        }
    }

    public GetResponse get(GetRequest getRequest) throws Exception {
        try {
            return restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("es.get exception, getRequest:{}", getRequest, e);
            throw e;
        }
    }

    public MultiGetResponse multiGet(MultiGetRequest multiGetRequest) throws Exception {
        try {
            return restHighLevelClient.multiGet(multiGetRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("es.multiGet exception, multiGetRequest:{}", multiGetRequest, e);
            throw e;
        }
    }

    public BulkResponse bulk(BulkRequest bulkRequest) throws Exception {
        try {
            return restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("es.bulk exception, bulkRequest:{}", bulkRequest, e);
            throw e;
        }
    }
}
