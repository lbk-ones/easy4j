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
package easy4j.infra.context.api.seed;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.RandomUtil;

/**
 * DefaultEasy4jSeed
 * 测试的时候报错 先给个默认的占占位
 *
 * @author bokun.li
 */
public class DefaultEasy4jSeed implements Easy4jSeed {

    public final static Snowflake snowflake = new Snowflake(RandomUtil.randomInt(0, 31), 2L);

    @Override
    public String nextIdStr() {
        return snowflake.nextIdStr();
    }

    @Override
    public long nextIdLong() {
        return snowflake.nextId();
    }
}
