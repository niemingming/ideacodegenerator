package ${basePackage}.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import ${basePackage}.entity.${entityName}Entity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author nmm
 * @since ${createTime}
 */
@Mapper
public interface ${entityName}Mapper extends BaseMapper<${entityName}Entity> {
}
