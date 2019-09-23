package com.kalvin.J12306.utils;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * yml工具类
 * Create by Kalvin on 2019/9/23.
 */
public class YmlUtil {

    private final static String filename = "config.yml";

    private static Map ymls = new LinkedHashMap();

    /**
     * string:当前线程需要查询的文件名
     */
    private static ThreadLocal<String> nowFileName = new ThreadLocal<>();

    /**
     * 加载配置文件
     */
    private static void loadYml() {
        InputStream resourceAsStream = YmlUtil.class.getResourceAsStream("/" + filename);
        ymls = new Yaml().loadAs(resourceAsStream, LinkedHashMap.class);
    }

    private static Object getValue(String key) {
        // 首先将key进行拆分
        String[] keys = key.split("[.]");
        for (int i = 0; i < keys.length; i++) {
            Object value = ymls.get(keys[i]);
            if (i < keys.length - 1) {
                ymls = (Map) value;
            } else if (value != null) {
                return value;
            }
        }
        return null;
    }

    public static Object get(String key) {
        loadYml();
        return getValue(key);
    }

}
