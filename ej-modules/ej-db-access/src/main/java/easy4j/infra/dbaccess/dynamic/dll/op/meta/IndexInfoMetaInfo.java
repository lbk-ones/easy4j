/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.infra.dbaccess.dynamic.dll.op.meta;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 数据库索引信息实体类
 * 该类用于存储数据库表的索引相关信息，包括索引名称、类型、关联列等详细属性
 */
@Data
@Schema(description = "数据库索引信息")
public class IndexInfoMetaInfo {

    /**
     * 表目录（可能为null）
     */
    @Schema(description = "表目录（可能为null）")
    private String tableCat;

    /**
     * 表模式（可能为null）
     */
    @Schema(description = "表模式（可能为null）")
    private String tableSchem;

    /**
     * 表名
     */
    @Schema(description = "表名")
    private String tableName;

    /**
     * 索引值是否可以为非唯一
     * 当TYPE为tableIndexStatistic时为false
     */
    @Schema(description = "索引值是否可以为非唯一。当TYPE为tableIndexStatistic时为false")
    private boolean nonUnique;

    /**
     * 索引目录（可能为null）
     * 当TYPE为tableIndexStatistic时为null
     */
    @Schema(description = "索引目录（可能为null）；当TYPE为tableIndexStatistic时为null")
    private String indexQualifier;

    /**
     * 索引名
     * 当TYPE为tableIndexStatistic时为null
     */
    @Schema(description = "索引名；当TYPE为tableIndexStatistic时为null")
    private String indexName;

    /**
     * 索引类型：
     * tableIndexStatistic - 标识与表的索引描述一起返回的表统计信息
     * tableIndexClustered - 这是一个聚簇索引
     * tableIndexHashed - 这是一个哈希索引
     * tableIndexOther - 这是其他样式的索引
     */
    @Schema(description = "索引类型：" +
            "tableIndexStatistic - 标识与表的索引描述一起返回的表统计信息" +
            "tableIndexClustered - 这是一个聚簇索引" +
            "tableIndexHashed - 这是一个哈希索引" +
            "tableIndexOther - 这是其他样式的索引")
    private short type;

    /**
     * 索引中的列序号
     * 当TYPE为tableIndexStatistic时为0
     */
    @Schema(description = "索引中的列序号；当TYPE为tableIndexStatistic时为0")
    private short ordinalPosition;

    /**
     * 列名
     * 当TYPE为tableIndexStatistic时为null
     */
    @Schema(description = "列名；当TYPE为tableIndexStatistic时为null")
    private String columnName;

    /**
     * 列排序序列
     * "A"表示升序，"D"表示降序，如果不支持排序序列则可能为null
     * 当TYPE为tableIndexStatistic时为null
     */
    @Schema(description = "列排序序列，\"A\"表示升序，\"D\"表示降序，如果不支持排序序列则可能为null；当TYPE为tableIndexStatistic时为null")
    private String ascOrDesc;

    /**
     * 当TYPE为tableIndexStatistic时，这是表中的行数
     * 否则，是索引中的唯一值数量
     */
    @Schema(description = "当TYPE为tableIndexStatistic时，这是表中的行数；否则，是索引中的唯一值数量")
    private long cardinality;

    /**
     * 当TYPE为tableIndexStatistic时，这是表使用的页数
     * 否则是当前索引使用的页数
     */
    @Schema(description = "当TYPE为tableIndexStatistic时，这是表使用的页数，否则是当前索引使用的页数")
    private long pages;

    /**
     * 过滤条件（如果有）
     * 可能为null
     */
    @Schema(description = "过滤条件（如果有）。（可能为null）")
    private String filterCondition;
}
    