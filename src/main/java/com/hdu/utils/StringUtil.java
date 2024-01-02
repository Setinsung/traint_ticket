package com.hdu.utils;

import com.google.common.base.Splitter;

import java.util.List;
import java.util.stream.Collectors;

public class StringUtil {

    // 将逗号分隔的字符串转换为Long类型的列表
    public static List<Long> splitToListLong(String str) {
        List<String> stringList = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(str);
        return stringList.stream().map(item -> Long.parseLong(item)).collect(Collectors.toList());
    }

    // 将逗号分隔的字符串转换为Integer类型的列表
    public static List<Integer> splitToListInt(String str) {
        List<String> stringList = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(str);
        return stringList.stream().map(item -> Integer.parseInt(item)).collect(Collectors.toList());
    }
}
