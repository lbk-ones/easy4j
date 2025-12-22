<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${parentPackageName}.${mapperPackageName}.${schema}Mapper">
    <resultMap id="BaseResultMap" type="${parentPackageName}.${entityPackageName}.${schema}">
        <#list fieldInfoList as field>
            <#if field.hasPrimaryKey>
        <id column="${field.dbName}" jdbcType="${field.mybatisJdbcType}" property="${field.name}" />
            <#else>
        <result column="${field.dbName}" jdbcType="${field.mybatisJdbcType}" property="${field.name}" />
            </#if>
        </#list>
    </resultMap>
</mapper>
