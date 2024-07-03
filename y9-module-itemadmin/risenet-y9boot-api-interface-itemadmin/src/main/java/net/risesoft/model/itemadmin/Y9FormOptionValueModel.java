package net.risesoft.model.itemadmin;

import java.io.Serializable;

import lombok.Data;

@Data
public class Y9FormOptionValueModel implements Serializable {
    private static final long serialVersionUID = 4453324429396249500L;

    /** 数据代码 */
    private String code;

    /** 主键名称 */
    private String name;

    /** 字典类型 */
    private String type;

    /** 是否默认选中 */
    private Integer defaultSelected = 0;
}