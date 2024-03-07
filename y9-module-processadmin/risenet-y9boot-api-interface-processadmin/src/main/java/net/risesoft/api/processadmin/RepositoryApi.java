package net.risesoft.api.processadmin;

import java.util.List;
import java.util.Map;

import net.risesoft.model.processadmin.ProcessDefinitionModel;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/19
 */
public interface RepositoryApi {

    /**
     * 删除部署的流程
     *
     * @param tenantId 租户id
     * @param deploymentId 部署id
     * @return Map&lt;String, Object&gt;
     */
    Map<String, Object> delete(String tenantId, String deploymentId);

    /**
     * 根据流程定义key获取最新部署的流程定义
     *
     * @param tenantId 租户id
     * @param processDefinitionKey 流程定义key
     * @return ProcessDefinitionModel
     */
    ProcessDefinitionModel getLatestProcessDefinitionByKey(String tenantId, String processDefinitionKey);

    /**
     * 获取所有流程定义最新版本的集合
     *
     * @param tenantId 租户id
     * @return List&lt;ProcessDefinitionModel&gt;
     */
    List<ProcessDefinitionModel> getLatestProcessDefinitionList(String tenantId);

    /**
     * 根据流程定义Id获取上一个版本的流程定义，如果当前版本是1，则返回自己
     *
     * @param tenantId 租户id
     * @param processDefinitionId 流程定义Id
     * @return ProcessDefinitionModel
     */
    ProcessDefinitionModel getPreviousProcessDefinitionById(String tenantId, String processDefinitionId);

    /**
     * 根据流程定义Id获取流程定义
     *
     * @param tenantId 租户id
     * @param processDefinitionId 流程定义Id
     * @return ProcessDefinitionModel
     */
    ProcessDefinitionModel getProcessDefinitionById(String tenantId, String processDefinitionId);

    /**
     * 根据流程定义key获取最新部署的流程定义
     *
     * @param tenantId 租户id
     * @param processDefinitionKey 流程定义key
     * @return List&lt;ProcessDefinitionModel&gt;
     */
    List<ProcessDefinitionModel> getProcessDefinitionListByKey(String tenantId, String processDefinitionKey);

    /**
     * 激活/挂起流程的状态
     *
     * @param tenantId 租户id
     * @param state 状态
     * @param processDefinitionId 流程定义Id
     * @return Map&lt;String, Object&gt;
     */
    Map<String, Object> switchSuspendOrActive(String tenantId, String state, String processDefinitionId);

}