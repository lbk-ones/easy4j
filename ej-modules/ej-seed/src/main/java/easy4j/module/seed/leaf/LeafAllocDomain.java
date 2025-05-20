package easy4j.module.seed.leaf;

import easy4j.module.base.plugin.dbaccess.annotations.JdbcColumn;
import easy4j.module.base.plugin.dbaccess.annotations.JdbcTable;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@JdbcTable(name = "leaf_alloc")
public class LeafAllocDomain implements Serializable {

    @JdbcColumn(name = "biz_tag", isPrimaryKey = true)
    private String BIZ_TAG;

    @JdbcColumn(name = "max_id")
    private Long MAX_ID;

    @JdbcColumn(name = "step")
    private Long STEP;
    @JdbcColumn(name = "description")
    private String DESCRIPTION;

    @JdbcColumn(name = "update_time")
    private Date UPDATE_TIME;
}
