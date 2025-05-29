package easy4j.module.mapstruct;
import easy4j.module.base.plugin.gen.JavaBaseGen;
import lombok.*;


/**
 * GenMapStructParams
 *
 * @author bokun.li
 * @date 2025-05
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GenMapStructParams extends JavaBaseGen {

    private String mapperStructInterfaceName = "MapperStruct";

}