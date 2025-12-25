package easy4j.module.mybatisplus.codegen.mybatis;

import easy4j.module.mybatisplus.codegen.AbstractGen;
import easy4j.module.mybatisplus.codegen.ObjectValue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class MapperGen extends AbstractGen {

    private String schema;
    private String description;
    @Override
    public String getFilePath() {
        String parentPackagePath = parsePackage(this.getParentPackageName());
        return joinPath(this.getProjectAbsolutePath(), SRC_MAIN_JAVA
                , parentPackagePath
                , parsePackage(this.getMapperPackageName())
                , this.getEntityName() + "Mapper.java");
    }

    public String gen(boolean isPreview, boolean isServer, ObjectValue objectValue) {
        notNull(this.getEntityName(),"entityName");
        notNull(this.getCnDesc(),"cnDesc");
        String filePath = this.getFilePath();
        this.schema = this.getEntityName();
        this.description = this.getCnDesc();
        return loadTemplate(filePath, "temp", "MapperGen.ftl", this, false);
    }
}
