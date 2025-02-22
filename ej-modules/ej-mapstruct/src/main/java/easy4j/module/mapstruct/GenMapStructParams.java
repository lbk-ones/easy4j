package easy4j.module.mapstruct;
import easy4j.module.base.plugin.gen.JavaBaseGen;
import lombok.*;


@EqualsAndHashCode(callSuper = true)
@Data
public class GenMapStructParams extends JavaBaseGen {

    private String mapperStructInterfaceName = "MapperStruct";

}
