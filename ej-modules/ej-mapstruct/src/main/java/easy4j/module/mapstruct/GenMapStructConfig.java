package easy4j.module.mapstruct;

import easy4j.module.base.plugin.gen.BaseConfigCodeGen;
import lombok.Getter;

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
