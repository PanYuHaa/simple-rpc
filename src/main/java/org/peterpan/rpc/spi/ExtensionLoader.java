package org.peterpan.rpc.spi;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author PeterPan
 * @date 2023/8/21
 * @description SPI机制
 */
@Slf4j
public class ExtensionLoader {

    // 系统SPI
    private static String SYS_EXTENSION_LOADER_DIR_PREFIX = "META-INF/prpc/";

    // 用户SPI
    private static String DIY_EXTENSION_LOADER_DIR_PREFIX = "META-INF/rpc/";

    private static String[] prefixs = {SYS_EXTENSION_LOADER_DIR_PREFIX, DIY_EXTENSION_LOADER_DIR_PREFIX};

    // bean map 定义信息: key=自定义的key, value=具体bean
    private static Map<String, Class> extensionClassCache = new ConcurrentHashMap<>();
    // 接口名与 bean map 的映射: key=接口名, value=接口名下的 bean map
    private static Map<String, Map<String, Class>> extensionClassCaches = new ConcurrentHashMap<>();

    // 实例化的bean(懒加载)
    private static Map<String, Object> singletonsObject = new ConcurrentHashMap<>();


    private static ExtensionLoader extensionLoader;

    static {
        extensionLoader = new ExtensionLoader();
    }

    public static ExtensionLoader getInstance() {
        return extensionLoader;
    }

    private ExtensionLoader() {

    }

    /**
     * 获取bean
     *
     * @param name(bean)
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public <V> V get(String name) {
        if (!singletonsObject.containsKey(name)) {
            try {
                singletonsObject.put(name, extensionClassCache.get(name).newInstance()); // 根据需要懒加载出来我们要的bean
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return (V) singletonsObject.get(name);
    }

    /**
     * 获取接口下所有的类
     *
     * @param clazz(接口）
     * @return
     */
    public List<Object> gets(Class clazz) {

        final String name = clazz.getName();
        if (!extensionClassCaches.containsKey(name)) {
            try {
                throw new ClassNotFoundException(clazz + "未找到");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        final Map<String, Class> stringClassMap = extensionClassCaches.get(name);
        List<Object> objects = new ArrayList<>();
        if (stringClassMap.size() > 0) {
            stringClassMap.forEach((k, v) -> {
                try {
                    objects.add(singletonsObject.getOrDefault(k, v.newInstance())); // 尝试从 singletonsObject 缓存中获取对象实例。如果缓存中不存在该实例，则通过反射创建一个新的实例。
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        }

        return objects;
    }

    /**
     * 根据spi机制初加载bean的信息放入map
     *
     * @param clazz
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void loadExtension(Class clazz) throws IOException, ClassNotFoundException {
        if (clazz == null) {
            throw new IllegalArgumentException("class 没找到");
        }
        ClassLoader classLoader = this.getClass().getClassLoader();
        Map<String, Class> classMap = new HashMap<>();
        // 从系统SPI以及用户SPI中找bean
        for (String prefix : prefixs) {
            String spiFilePath = prefix + clazz.getName();
            Enumeration<URL> enumeration = classLoader.getResources(spiFilePath);
            while (enumeration.hasMoreElements()) {
                URL url = enumeration.nextElement();
                InputStreamReader inputStreamReader = null;
                inputStreamReader = new InputStreamReader(url.openStream()); // InputStream
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader); // Reader
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    String[] lineArr = line.split("=");
                    String key = lineArr[0];
                    String name = lineArr[1];
                    final Class<?> aClass = Class.forName(name); // name为接口下的bean名
                    extensionClassCache.put(key, aClass);
                    classMap.put(key, aClass);
                    log.info("加载bean key={}, value={}", key, name);
                }
            }
        }
        extensionClassCaches.put(clazz.getName(), classMap); // key为接口名，val为map里面包含的所有的接口实现方案
    }

}
