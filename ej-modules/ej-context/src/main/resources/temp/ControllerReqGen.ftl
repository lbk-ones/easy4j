package ${parentPackageName}.controller.req;
import ${parentPackageName}.dto.${returnDtoName};
import easy4j.module.mybatisplus.base.PageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.util.List;
/**
 * ${headerDesc}
 * <p/>
 * @author ${author}
 * @since ${.now}
 */
@Data
@Schema(description = "${cnDesc}接口传参")
public class ${domainName}ControllerReq implements Serializable {

    @Schema(description = "${cnDesc}集合",implementation = ${returnDtoName}.class)
    List<${returnDtoName}> ${(returnDtoName?substring(0,1))?lower_case + (returnDtoName?substring(1))}s;

    @Schema(description = "${cnDesc}信息 分页查询信息，分页查询的时候传入",implementation = PageDto.class)
    PageDto pageQuery;

}
