package easy4j.infra.dbaccess.dynamic.dll.op.meta;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "数据库信息")
public class CatalogMetadata {

    @Schema(description = "数据库名称")
    private String tableCat;

}
