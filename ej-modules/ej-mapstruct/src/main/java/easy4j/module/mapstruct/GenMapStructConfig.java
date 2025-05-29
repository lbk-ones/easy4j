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
package easy4j.module.mapstruct;

import easy4j.module.base.plugin.gen.BaseConfigCodeGen;
import lombok.Getter;

/**
 * GenMapStructConfig
 *
 * @author bokun.li
 * @date 2025-05
 */
@Getter
public class GenMapStructConfig extends BaseConfigCodeGen {

    private GenMapStructConfig(Builder builder) {
        super(builder);
        this.mapperStructInterfaceName = builder.mapperStructInterfaceName;
        this.currentMapperStructInterfaceClass = builder.currentMapperStructInterfaceClass;
    }

    private final String mapperStructInterfaceName;

    private final Class<?> currentMapperStructInterfaceClass;


    public static class Builder extends BaseConfigCodeGen.Builder<Builder>{


        private String mapperStructInterfaceName = "MapperStruct";

        private Class<?> currentMapperStructInterfaceClass;

        public Builder setMapperStructInterfaceName(String mapperStructInterfaceName) {
            this.mapperStructInterfaceName = mapperStructInterfaceName;
            return this;
        }

        public Builder setCurrentMapperStructInterfaceClass(Class<?> currentMapperStructInterfaceClass) {
            this.currentMapperStructInterfaceClass = currentMapperStructInterfaceClass;
            return this;
        }

        public GenMapStructConfig build(){
            return new GenMapStructConfig(this);
        }
        @Override
        public Builder self() {
            return this;
        }
    }

}
