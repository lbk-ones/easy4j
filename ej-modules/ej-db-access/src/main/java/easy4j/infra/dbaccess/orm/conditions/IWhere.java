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
package easy4j.infra.dbaccess.orm.conditions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import easy4j.infra.dbaccess.orm.RuntimeContext;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * 重构 2.1.4条件构造器，抽象顶级父类
 * @see WhereBuild WhereBuild 以字符串形式传参
 * @see UpdateBuild UpdateBuild 以字符串形式传参
 * @see FWhereBuild FWhereBuild 以lambda形式传参
 * @see FUpdateBuild FUpdateBuild 以lambda形式传参
 * @since 2.1.5
 * @author bokun.li
 */
public interface IWhere extends Serializable {


    /**
     * 清楚当前所有条件
     */
    void clear();

    /**
     * 获取where条件构造器,看具体实现类，可能没有
     */
    @JsonIgnore
    Optional<IWhereBuild> getWhere();

    /**
     * 获取更新条件构造器,看具体实现类，可能没有
     */
    @JsonIgnore
    Optional<IUpdateBuild> getUpdate();

    /**
     * 获取传入的选择字段列表
     */
    List<Condition> getSelectFields();

    /**
     * 获取最后面的sql
     */
    String getLast();

    /**
     * 标识是否是子语句
     *
     * @param flag 标志
     */
    void setSubSql(boolean flag);

    /**
     * 设置操作符
     *
     * @param operator 操作符
     */
    IWhere withLogicOperator(LogicOperator operator);

    /**
     * 获取操作符
     */
    LogicOperator getLogicOperator();

    /**
     * 构建查询条件
     *
     * @param whereArgs      参数列表
     * @param runtimeContext 上下文
     * @param skipTail       是否跳过尾部sql解析
     */
    String buildQuery(List<Object> whereArgs, RuntimeContext<?> runtimeContext, boolean skipTail);

    /**
     * 构建更新条件
     *
     * @param argList 参数列表
     * @param context 上下文
     */
    List<String> buildUpdate(List<Object> argList, RuntimeContext<?> context);

}
