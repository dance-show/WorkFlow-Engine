package net.risesoft.entity.form;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
@Entity
@Table(name = "Y9FORM_OPTIONCLASS")
@Comment("字典类型表")
@NoArgsConstructor
@Data
public class Y9FormOptionClass implements Serializable {
    private static final long serialVersionUID = -5901383621072805572L;

    @Id
    @Column(name = "TYPE", length = 255, nullable = false)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "assigned")
    @Comment("主键，类型名称")
    private String type;

    @Column(name = "NAME", length = 255, nullable = false)
    @Comment("中文名称")
    private String name;

}