package easy4j.module.mybatisplus.codegen.mybatis;

import easy4j.module.mybatisplus.codegen.AbstractGen;
import easy4j.module.mybatisplus.codegen.ObjectValue;
import easy4j.module.mybatisplus.codegen.servlet.PreviewRes;
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
        String res = loadTemplate(filePath, "temp", "MapperGen.ftl", this, isPreview);
        PreviewRes previewRes = new PreviewRes();
        PreviewRes.PInfo pInfo = new PreviewRes.PInfo("Mapper");
        String fName = this.getEntityName() + "Mapper.java";
        pInfo.add(fName,res);
        previewRes.add(pInfo);
        objectValue.setObject(previewRes);
        return res;
    }
}
