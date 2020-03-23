package ${basePackage}.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ${basePackage}.entity.${entityName}Entity;
import ${basePackage}.mapper.${entityName}Mapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author nmm
 * @since ${createTime}
 */
@Service
public class ${entityName}Service extends ServiceImpl<${entityName}Mapper, ${entityName}Entity> {
}
