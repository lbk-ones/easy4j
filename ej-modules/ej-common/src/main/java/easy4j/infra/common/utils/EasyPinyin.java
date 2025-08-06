package easy4j.infra.common.utils;

import cn.hutool.extra.pinyin.PinyinException;
import cn.hutool.extra.pinyin.PinyinUtil;

/**
 * 拼音码生成工具类（基于hutool）
 */
public class EasyPinyin {
    
    /**
     * 获取中文首字母缩写（大写）
     */
    public static String getPinyinFirstUpper(String chinese) {
        if (chinese == null || chinese.trim().isEmpty()) {
            return "";
        }
        try {
            // 获取首字母缩写并转为大写
            return PinyinUtil.getFirstLetter(chinese,"").toUpperCase();
        } catch (PinyinException e) {
            e.printStackTrace();
            return "";
        }
    }
    public static String getPinyinFirstLower(String chinese) {
        if (chinese == null || chinese.trim().isEmpty()) {
            return "";
        }
        try {
            // 获取首字母缩写并转为大写
            return PinyinUtil.getFirstLetter(chinese,"").toLowerCase();
        } catch (PinyinException e) {
            e.printStackTrace();
            return "";
        }
    }
    
    /**
     * 获取中文全拼（大写）
     */
    public static String getFullPinyinUpper(String chinese) {
        if (chinese == null || chinese.trim().isEmpty()) {
            return "";
        }
        try {
            // 获取全拼并转为大写
            return PinyinUtil.getPinyin(chinese).toUpperCase();
        } catch (PinyinException e) {
            e.printStackTrace();
            return "";
        }
    }
    public static String getFullPinyinLower(String chinese) {
        if (chinese == null || chinese.trim().isEmpty()) {
            return "";
        }

        try {
            // 获取全拼并转为大写
            return PinyinUtil.getPinyin(chinese).toLowerCase();
        } catch (PinyinException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void main(String[] args) {
        String test = "上海市浦东新区";
        System.out.println("首字母缩写: " + getPinyinFirstUpper(test)); // 输出: SHSPDXQ
        System.out.println("首字母缩写: " + getPinyinFirstLower(test)); // 输出: shspdxq
        System.out.println("全拼: " + getFullPinyinUpper(test)); // 输出: SHANG HAI SHI PU DONG XIN QU
        System.out.println("全拼: " + getFullPinyinLower(test)); // 输出: shang hai shi pu dong xin qu
    }
}
