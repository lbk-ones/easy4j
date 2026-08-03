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

import java.util.List;

public interface IWhereBuild extends IWhere, StCondition<IWhereBuild,String> {

    IWhereBuild withLogicOperator(LogicOperator operator);

    List<IWhere> getSubBuilders();

    List<Condition> getConditions();

    List<Condition> getUpdateConditions();

    @Override
    default IWhereBuild optionDo(boolean option, VoidFunc func){
        if(option && func!=null){
            func.call();
        }
        return this;
    }

}
