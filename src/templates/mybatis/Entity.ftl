package ${basePackage}.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
<#if hasDate??>
import java.time.*;
</#if>

/**
 * ${tablename}
 * @author nmm
 * @since ${createTime}
 */
@Data
@TableName("${tablename}")
public class ${entityName}Entity {

    <#list fields as field>
    <#if (field.comment)??>
    /**
    * ${field.comment}
    */
    </#if>
    <#if field.type == "int">
    private Long ${field.name};
    <#elseif field.type == "number">
    private Double ${field.name};
    <#elseif field.type == "string">
    private String ${field.name};
    <#elseif field.type == "date">
    private LocalDate ${field.name};
    <#elseif field.type == "time">
    private LocalDateTime ${field.name};
    <#elseif field.type == "boolean">
    private Boolean ${field.name};
    <#else >
    private Object ${field.name};
    </#if >
    </#list>

}
