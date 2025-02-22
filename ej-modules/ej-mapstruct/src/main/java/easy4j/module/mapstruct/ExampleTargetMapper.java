package easy4j.module.mapstruct;

import easy4j.module.base.annotations.Desc;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;



@Desc("")
// @Mapper(uses = { TransferMapper.class})
public interface ExampleTargetMapper {
    ExampleTargetMapper INSTANCE = Mappers.getMapper(ExampleTargetMapper.class);

    // 定义日期格式
    String DATE_FORMAT = "yyyy-MM-dd";

    // 自定义 String 转 Date 的方法
    default Date stringToDate(String dateString) {
        if (dateString == null) {
            return null;
        }
        try {
            return new SimpleDateFormat(DATE_FORMAT).parse(dateString);
        } catch (ParseException e) {
            throw new IllegalArgumentException("日期格式错误，期望格式: " + DATE_FORMAT, e);
        }
    }

    // 自定义 Date 转 String 的方法
    default String dateToString(Date date) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }

    // 定义映射方法
    @Mapping(source = "dateString", target = "date", qualifiedByName = "")
    <T,R> T sourceToTarget(R source);

    @Mapping(source = "date", target = "dateString", qualifiedByName = "")
    <T,R> T targetToSource(R target);
}