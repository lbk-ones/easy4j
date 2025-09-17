package easy4j.infra.dbaccess.dynamic.dll.op.meta;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "数据库SCHEMA信息")
public class SchemaMetadata {

    @Schema(description = "数据库名称")
    private String tableCat;

    @Schema(description = "schema名称")
    private String schema;

}
