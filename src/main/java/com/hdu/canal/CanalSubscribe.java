package com.hdu.canal;

import java.net.InetSocketAddress;
import java.util.List;


import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.client.CanalConnector;

import com.alibaba.otter.canal.common.utils.AddressUtils;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.hdu.service.TrainNumberService;
import com.hdu.service.TrainSeatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class CanalSubscribe implements ApplicationListener<ContextRefreshedEvent> {

    @Resource
    private TrainSeatService trainSeatService;
    @Resource
    private TrainNumberService trainNumberService;

//    @Value("${canal.connection.address.host}")
//    private String host;
//    @Value(("${canal.connection.address.port}"))
//    private int port;
//    @Value("${canal.connection.destination}")
//    private String destination;
//    @Value("${canal.connection.username}")
//    private String username;
//    @Value("${canal.connection.password}")
//    private String password;

    private void canalSubscribe() {
        // 创建链接
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress(AddressUtils.getHostIp(),
                11111), "train", "", "");
//        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress(host, port), destination, username, password);
        int batchSize = 1000;
        new Thread(()->{
            try {
                log.info("canal subscribe");
                connector.connect();
                connector.subscribe(".*\\..*");
                connector.rollback();
                while (true) {
                    connector.subscribe(".*\\..*");
                    Message message = connector.getWithoutAck(batchSize); // 获取指定数量的数据
                    long batchId = message.getId();
                    int size = message.getEntries().size();
                    if (batchId == -1 || size == 0) {
                        safeSleep(100);
                        continue;
                    }
                    try {
                        log.info("new message batchId:{}, size:{}",batchId,size);
                        printHandle(message.getEntries());
                        connector.ack(batchId); // 提交确认
                    } catch (Exception e) {
                        log.error("canal data handle exception, batchId:{}",batchId,e);
                        connector.rollback(batchId); // 处理失败, 回滚数据
                    }
                }
            } catch (Exception e){
                log.error("canal subscribe exception",e);
                safeSleep(1000);
                canalSubscribe();
            }
        }).start();
    }

    private  void printHandle(List<CanalEntry.Entry> entrys) throws Exception{
        for (CanalEntry.Entry entry : entrys) {
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                continue;
            }

            CanalEntry.RowChange rowChage = null;
            try {
                rowChage = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("RowChange.parse exception , data:" + entry.toString(),
                        e);
            }

            CanalEntry.EventType eventType = rowChage.getEventType();
            String schemaName = entry.getHeader().getSchemaName();
            String tableName = entry.getHeader().getTableName();
            log.info("name:[{},{}],eventType:{}",schemaName,tableName,eventType);
            for (CanalEntry.RowData rowData : rowChage.getRowDatasList()) {
                if (eventType == CanalEntry.EventType.DELETE) {
                    handleColumn(rowData.getBeforeColumnsList(), eventType, schemaName, tableName);
                }else {
                    handleColumn(rowData.getAfterColumnsList(), eventType, schemaName, tableName);
                }
            }
        }
    }

    private  void handleColumn(List<CanalEntry.Column> columns, CanalEntry.EventType eventType, String schemaName, String tableName) throws Exception {
        if (schemaName.contains("train_ticket_seat")){ // 处理座位变更
            trainSeatService.handle(columns,eventType);
        } else if (tableName.equals("train_number")){ // 处理车次变更
//            trainNumberService.handle(columns,eventType);
        }else {
            log.info("drop table,need care");
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        canalSubscribe();
    }

    private void safeSleep(int mills){
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}