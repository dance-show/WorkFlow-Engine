package net.risesoft.service.config;

import java.util.List;

import net.risesoft.entity.ItemStartNodeRole;
import net.risesoft.model.platform.Role;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
public interface ItemStartNodeRoleService {

    /**
     * Description:
     *
     * @param itemId
     * @param processDefinitionId
     */
    void copyBind(String itemId, String processDefinitionId);

    /**
     * Description: 删除绑定路由节点角色信息
     *
     * @param itemId
     */
    void deleteBindInfo(String itemId);

    /**
     * Description:
     *
     * @param id
     * @return
     */
    ItemStartNodeRole findById(String id);

    /**
     * Description:
     *
     * @param itemId
     * @param processDefinitionId
     * @param taskDefKey
     * @return
     */
    ItemStartNodeRole findByItemIdAndProcessDefinitionIdAndTaskDefKey(String itemId, String processDefinitionId,
        String taskDefKey);

    /**
     * Description:
     *
     * @param itemId
     * @return
     */
    String getStartTaskDefKey(String itemId);

    /**
     * Description:
     *
     * @param itemId
     * @param processDefinitionId
     * @param taskDefKey
     * @param taskDefName
     */
    void initRole(String itemId, String processDefinitionId, String taskDefKey, String taskDefName);

    /**
     * Description:
     *
     * @param itemId
     * @param processDefinitionId
     * @return
     */
    List<ItemStartNodeRole> listByItemIdAndProcessDefinitionId(String itemId, String processDefinitionId);

    /**
     * Description:
     *
     * @param itemId
     * @param processDefinitionId
     * @param taskDefKey
     * @return
     */
    List<Role> listRoleByItemIdAndProcessDefinitionIdAndTaskDefKey(String itemId, String processDefinitionId,
        String taskDefKey);

    /**
     * Description:
     *
     * @param itemId
     * @param processDefinitionId
     * @param taskDefKey
     * @param roleIds
     */
    void removeRole(String itemId, String processDefinitionId, String taskDefKey, String roleIds);

    /**
     * 保存排序
     *
     * @param idAndTabIndexs
     */
    /**
     * Description:
     *
     * @param idAndTabIndexs
     */
    void saveOrder(String[] idAndTabIndexs);

    /**
     * Description:
     *
     * @param itemId
     * @param processDefinitionId
     * @param taskDefKey
     * @param roleIds
     */
    void saveRole(String itemId, String processDefinitionId, String taskDefKey, String roleIds);
}