package com.peersafe.chainbet.utils.sensitiveWord;

import android.content.Context;
import android.content.res.AssetManager;

import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.utils.log.OLLogger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @version 1.0
 * @Description: 初始化敏感词库，将敏感词加入到HashMap中，构建DFA算法模型
 * @Project：test
 * @Author : chenming
 * @Date ： 2014年4月20日 下午2:27:06
 */
public class SensitiveWordInit {
    private String TAG = SensitiveWordInit.class.getSimpleName();
    //字符编码
    private String ENCODING = "UTF-8";
    @SuppressWarnings("rawtypes")
    public HashMap sensitiveWordMap;

    public SensitiveWordInit() {
        super();
    }

    /**
     * @author chenming
     * @date 2014年4月20日 下午2:28:32
     * @version 1.0
     */
    @SuppressWarnings("rawtypes")
    public Map initKeyWord() {
        try {
            //读取敏感词库
            Set<String> keyWordSet = readSensitiveWordFile();
            //将敏感词库加入到HashMap中
            addSensitiveWordToHashMap(keyWordSet);
            //spring获取application，然后application.setAttribute("sensitiveWordMap",sensitiveWordMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sensitiveWordMap;
    }

    /**
     * 读取敏感词库，将敏感词放入HashSet中，构建一个DFA算法模型：
     * 中 = {
     *      isEnd = 0
     *      国 = {<br>
     *      	 isEnd = 1
     *           人 = {isEnd = 0
     *                民 = {isEnd = 1}
     *                }
     *           男  = {
     *           	   isEnd = 0
     *           		人 = {
     *           			 isEnd = 1
     *           			}
     *           	}
     *           }
     *      }
     *  五 = {
     *      isEnd = 0
     *      星 = {
     *      	isEnd = 0
     *      	红 = {
     *              isEnd = 0
     *              旗 = {
     *                   isEnd = 1
     *                  }
     *              }
     *      	}
     *      }
     *
     * @param keyWordSet 敏感词库
     * @author chenming
     * @date 2014年4月20日 下午3:04:20
     * @version 1.0
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void addSensitiveWordToHashMap(Set<String> keyWordSet) {
        //初始化敏感词容器，减少扩容操作
        sensitiveWordMap = new HashMap(keyWordSet.size());
        String key = null;
        Map nowMap = null;
        Map<String, String> newWorMap = null;
        //迭代keyWordSet
        Iterator<String> iterator = keyWordSet.iterator();
        while (iterator.hasNext()) {
            //关键字
            key = iterator.next();
            nowMap = sensitiveWordMap;
            for (int i = 0; i < key.length(); i++) {
                //转换成char型
                char keyChar = key.charAt(i);
                //获取
                Object wordMap = nowMap.get(keyChar);

                //如果存在该key，直接赋值
                if (wordMap != null) {
                    nowMap = (Map) wordMap;
                } else {
                    //不存在则构建一个map，同时将isEnd设置为0，因为他不是最后一个
                    newWorMap = new HashMap<String, String>();
                    //不是最后一个
                    newWorMap.put("isEnd", "0");
                    nowMap.put(keyChar, newWorMap);
                    nowMap = newWorMap;
                }

                //最后一个
                if (i == key.length() - 1) {
                    nowMap.put("isEnd", "1");
                }
            }
        }
    }

    /**
     * 读取敏感词库中的内容，将内容添加到set集合中
     *
     * @return
     * @throws Exception
     * @author chenming
     * @date 2014年4月20日 下午2:31:18
     * @version 1.0
     */
    @SuppressWarnings("resource")
    private Set<String> readSensitiveWordFile() throws Exception
    {
        Set<String> set = null;

        Context context = OneLotteryApplication.getAppContext();
        AssetManager am = context.getResources().getAssets();
        //读取文件
        String[] files = am.list("SensitiveWord");
        if (files.length > 0) {
            set = new HashSet<String>();

            for (int i = 0; i < files.length; i++) {
                String file = files[i];

                OLLogger.i(TAG, "敏感词库文件" + (i + 1) + ": " + file);

                InputStream stream = am.open("SensitiveWord/" + file);
                InputStreamReader read = new InputStreamReader(stream, ENCODING);
                try {
                    BufferedReader bufferedReader = new BufferedReader(read);
                    String txt = null;
                    int count = 0;
                    //读取文件，将文件内容放入到set中
                    while ((txt = bufferedReader.readLine()) != null) {
                        set.add(txt);
                        if (count < 10) {

                            OLLogger.i(TAG, "敏感词" + count++ + ": " + txt);

                        }
                    }
                    OLLogger.i(TAG, "敏感词个数：" + set.size());
                } catch (Exception e) {
                    throw e;
                } finally {
                    //关闭文件流
                    read.close();
                }
            }

        } else {
            //不存在抛出异常信息
            OLLogger.i(TAG, "敏感词库文件不存在");
        }

        return set;
    }
}
