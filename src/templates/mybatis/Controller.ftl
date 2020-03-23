package ${basePackage}.controller;

import ${basePackage}.service.${entityName}Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 *  前端控制器
 *
 * @author nmm
 * @since ${createTime}
 */
@RestController
@RequestMapping("/api/v1/${entitypath}")
public class ${entityName}Controller {
    @Autowired
    private ${entityName}Service ${entityVar}Service;
}
