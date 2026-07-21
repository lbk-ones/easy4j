package easy4j.infra.dbaccess.orm.runner;

import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.ResultSet;
import java.sql.Statement;

@Data
@Accessors(chain = true)
public class PsRes {

    ResultSet resultSet;

    Statement statement;

    int effectRows;

    public PsRes(ResultSet resultSet, Statement statement) {
        this.resultSet = resultSet;
        this.statement = statement;
    }

    public PsRes() {
    }
}
