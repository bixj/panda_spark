package com.pandatv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Tuple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author: likaiqing
 * @create: 2018-11-02 17:06
 **/
public class Test {
    @org.junit.Test
    public void test1() throws IOException {
        Jedis jedis = new Jedis("localhost", 6379);
        String str = jedis.get("panda:zhaomu:ancdtl:27072326");
        ObjectMapper mapper = new ObjectMapper();
        HashMap hashMap = mapper.readValue(str, HashMap.class);
        System.out.println(hashMap);

        jedis.close();
    }

    @org.junit.Test
    public void test2() throws IOException {
        String str = "{\"name\":\"ruc_profile_change\",\"data\":\"{\\\"rid\\\":154764538,\\\"nickname\\\":\\\"\\\\u7530\\\\u679c\\\\u679c\\\\u7684\\\\u7238\\\\u7238\\\"}\",\"host\":\"ruc7v.main.bjtb.pdtv.it\",\"key\":\"\",\"time\":\"2018-10-30 00:01:21\",\"requestid\":\"1540828881829-56913101-26916-f2dea295864b0486\"}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(str);
        String data = jsonNode.get("data").asText();
        String rid = mapper.readTree(data).get("rid").asText();
        System.out.println(rid);
    }

    @org.junit.Test
    public void test3() throws IOException, InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(100);
        List<Object> list = new ArrayList<>();
        for (int j = 0; j < 100; j++) {
            list.add(service.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    Jedis jedis = new Jedis("localhost", 6379);
                    Pipeline pipelined = jedis.pipelined();
                    for (int i = 0; i < 10000; i++) {
                        pipelined.zincrby("zincrby_test", 1, "test6");
                    }
                    pipelined.sync();
                    try {
                        pipelined.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    jedis.close();
                    return true;
                }
            }));
        }
        service.shutdown();
        boolean b = service.awaitTermination(20, TimeUnit.SECONDS);
        if (b) {
            System.out.println(b);
        }
        System.out.println(list.size());
        Jedis jedis = new Jedis("localhost", 6379);
        Set<Tuple> tuples = jedis.zrangeWithScores("zincrby_test", 0, 0);
        for (Tuple tuple : tuples) {
            System.out.println(tuple.getElement() + ":" + tuple.getScore());
        }
        jedis.close();

    }
}
