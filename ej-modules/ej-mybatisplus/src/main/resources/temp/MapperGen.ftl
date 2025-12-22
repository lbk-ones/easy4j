package ${parentPackageName}.${mapperPackageName};
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import ${parentPackageName}.${entityPackageName}.${schema};
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * ${description} Mapper 接口
 * </p>
 *
 * @author ${author}
 * @since ${.now}
 */
@Mapper
public interface ${schema}Mapper extends BaseMapper<${schema}> {

}