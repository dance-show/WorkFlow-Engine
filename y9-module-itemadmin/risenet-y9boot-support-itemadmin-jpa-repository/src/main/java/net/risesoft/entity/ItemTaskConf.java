package net.risesoft.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
@NoArgsConstructor
@Data
@Entity
@Table(name = "FF_ITEM_TASKCONF")
@Comment("事项绑定流程的任务配置表")
public class ItemTaskConf implements Serializable {

    private static final long serialVersionUID = -4007011147966571051L;

    @Id
    @Comment("主键")
    @Column(name = "ID", length = 38, nullable = false)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "assigned")
    private String id;

    @Comment("租户Id")
    @Column(name = "TENANTID", length = 50, nullable = false)
    private String tenantId;

    @Comment("事项Id")
    @Column(name = "ITEMID", length = 50, nullable = false)
    private String itemId;

    @Comment("流程定义Id")
    @Column(name = "PROCESSDEFINITIONID", length = 100, nullable = false)
    private String processDefinitionId;

    @Comment("任务key")
    @Column(name = "TASKDEFKEY", length = 100)
    private String taskDefKey;

    @Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
    @Comment("区分主协办")
    @Column(name = "SPONSOR", nullable = false)
    @ColumnDefault("0")
    private Boolean sponsor = false;

    @Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
    @Comment("抢占式签收")
    @Column(name = "SIGNTASK", nullable = false)
    @ColumnDefault("0")
    private Boolean signTask = false;

    @Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
    @Comment("必签意见")
    @Column(name = "SIGNOPINION")
    @ColumnDefault("0")
    private Boolean signOpinion = false;
}