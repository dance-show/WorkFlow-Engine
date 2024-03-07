package net.risesoft.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.api.processadmin.ProcessDefinitionApi;
import net.risesoft.api.processadmin.RepositoryApi;
import net.risesoft.entity.CommonButton;
import net.risesoft.entity.ItemButtonBind;
import net.risesoft.entity.ItemButtonRole;
import net.risesoft.entity.SendButton;
import net.risesoft.entity.SpmApproveItem;
import net.risesoft.enums.ItemButtonTypeEnum;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.processadmin.ProcessDefinitionModel;
import net.risesoft.model.user.UserInfo;
import net.risesoft.repository.jpa.ItemButtonBindRepository;
import net.risesoft.service.CommonButtonService;
import net.risesoft.service.ItemButtonBindService;
import net.risesoft.service.ItemButtonRoleService;
import net.risesoft.service.SendButtonService;
import net.risesoft.service.SpmApproveItemService;
import net.risesoft.util.SysVariables;
import net.risesoft.y9.Y9LoginUserHolder;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
@Transactional(value = "rsTenantTransactionManager", readOnly = true)
@Service(value = "itemButtonBindService")
public class ItemButtonBindServiceImpl implements ItemButtonBindService {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private ItemButtonBindRepository buttonItemBindRepository;

    @Autowired
    private CommonButtonService commonButtonService;

    @Autowired
    private SendButtonService sendButtonService;

    @Autowired
    private ItemButtonRoleService itemButtonRoleService;

    @Autowired
    private SpmApproveItemService spmApproveItemService;

    @Autowired
    private RepositoryApi repositoryManager;

    @Autowired
    private ProcessDefinitionApi processDefinitionManager;

    @Override
    @Transactional(readOnly = false)
    public ItemButtonBind bindButton(String itemId, String buttonId, String processDefinitionId, String taskDefKey,
        Integer buttonType) {
        UserInfo person = Y9LoginUserHolder.getUserInfo();
        String userId = person.getPersonId(), userName = person.getName(), tenantId = Y9LoginUserHolder.getTenantId();
        ItemButtonBind bib = new ItemButtonBind();
        bib.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
        bib.setButtonId(buttonId);
        bib.setButtonType(buttonType);
        bib.setItemId(itemId);
        bib.setProcessDefinitionId(processDefinitionId);
        bib.setTaskDefKey(taskDefKey);
        bib.setTenantId(tenantId);
        bib.setCreateTime(sdf.format(new Date()));
        bib.setUpdateTime(sdf.format(new Date()));
        bib.setUserId(userId);
        bib.setUserName(userName);

        Integer index = buttonItemBindRepository.getMaxTabIndex(itemId, processDefinitionId, taskDefKey, buttonType);
        if (index == null) {
            bib.setTabIndex(1);
        } else {
            bib.setTabIndex(index + 1);
        }
        buttonItemBindRepository.save(bib);
        return bib;
    }

    @Override
    @Transactional(readOnly = false)
    public void copyBind(String itemId, String processDefinitionId) {
        UserInfo person = Y9LoginUserHolder.getUserInfo();
        String tenantId = Y9LoginUserHolder.getTenantId(), userId = person.getPersonId(), userName = person.getName();
        SpmApproveItem item = spmApproveItemService.findById(itemId);
        String proDefKey = item.getWorkflowGuid();
        ProcessDefinitionModel latestpd = repositoryManager.getLatestProcessDefinitionByKey(tenantId, proDefKey);
        String latestpdId = latestpd.getId();
        String previouspdId = processDefinitionId;
        if (processDefinitionId.equals(latestpdId)) {
            if (latestpd.getVersion() > 1) {
                ProcessDefinitionModel previouspd =
                    repositoryManager.getPreviousProcessDefinitionById(tenantId, latestpdId);
                previouspdId = previouspd.getId();
            }
        }
        List<Map<String, Object>> nodes = processDefinitionManager.getNodes(tenantId, latestpdId, false);
        for (Map<String, Object> map : nodes) {
            String currentTaskDefKey = (String)map.get("taskDefKey");
            List<ItemButtonBind> bindList = new ArrayList<>();
            if (StringUtils.isBlank(currentTaskDefKey)) {
                bindList = buttonItemBindRepository
                    .findByItemIdAndProcessDefinitionIdAndTaskDefKeyIsNullOrderByTabIndexAsc(itemId, previouspdId);
            } else {
                bindList = buttonItemBindRepository.findByItemIdAndProcessDefinitionIdAndTaskDefKeyOrderByTabIndexAsc(
                    itemId, previouspdId, currentTaskDefKey);
            }
            for (ItemButtonBind bind : bindList) {
                ItemButtonBind oldBind = null;
                if (StringUtils.isBlank(currentTaskDefKey)) {
                    oldBind = buttonItemBindRepository
                        .findByItemIdAndProcessDefinitionIdAndTaskDefKeyIsNullAndButtonIdOrderByTabIndexAsc(itemId,
                            latestpdId, bind.getButtonId());
                } else {
                    oldBind = buttonItemBindRepository
                        .findByItemIdAndProcessDefinitionIdAndTaskDefKeyAndButtonIdOrderByTabIndexAsc(itemId,
                            latestpdId, currentTaskDefKey, bind.getButtonId());
                }
                if (null == oldBind) {
                    String newbindId = Y9IdGenerator.genId(IdType.SNOWFLAKE), oldbindId = bind.getId();
                    /**
                     * 保存按钮的绑定
                     */
                    ItemButtonBind newbind = new ItemButtonBind();
                    newbind.setId(newbindId);
                    newbind.setButtonId(bind.getButtonId());
                    newbind.setButtonType(bind.getButtonType());
                    newbind.setItemId(itemId);
                    newbind.setProcessDefinitionId(latestpdId);
                    newbind.setTaskDefKey(currentTaskDefKey);
                    newbind.setTenantId(tenantId);
                    newbind.setTenantId(tenantId);
                    newbind.setCreateTime(sdf.format(new Date()));
                    newbind.setUpdateTime(sdf.format(new Date()));
                    newbind.setUserId(userId);
                    newbind.setUserName(userName);

                    Integer index = buttonItemBindRepository.getMaxTabIndex(itemId, latestpdId, currentTaskDefKey,
                        bind.getButtonType());
                    if (index == null) {
                        newbind.setTabIndex(1);
                    } else {
                        newbind.setTabIndex(index + 1);
                    }

                    buttonItemBindRepository.save(newbind);
                    /**
                     * 保存按钮的授权
                     */
                    List<ItemButtonRole> roleList = itemButtonRoleService.findByItemButtonId(oldbindId);
                    for (ItemButtonRole role : roleList) {
                        itemButtonRoleService.saveOrUpdate(newbindId, role.getRoleId());
                    }
                }
            }
        }
    }

    @Override
    public List<ItemButtonBind> findList(String itemId, Integer buttonType, String processDefinitionId) {
        String buttonName = "按钮不存在";
        String buttonCustomId = "";
        List<ItemButtonBind> bibList = buttonItemBindRepository
            .findByItemIdAndButtonTypeAndProcessDefinitionIdOrderByTabIndexAsc(itemId, buttonType, processDefinitionId);
        for (ItemButtonBind bib : bibList) {
            if (buttonType == ItemButtonTypeEnum.COMMON.getValue()) {
                CommonButton cb = commonButtonService.findOne(bib.getButtonId());
                if (null != cb) {
                    buttonName = cb.getName();
                    buttonCustomId = cb.getCustomId();
                }
            } else {
                SendButton sb = sendButtonService.findOne(bib.getButtonId());
                if (null != sb) {
                    buttonName = sb.getName();
                    buttonCustomId = sb.getCustomId();
                }
            }
            bib.setButtonName(buttonName);
            bib.setButtonCustomId(buttonCustomId);
        }
        return bibList;
    }

    @Override
    public List<ItemButtonBind> findList(String itemId, Integer buttonType, String processDefinitionId,
        String taskDefKey) {
        String buttonName = "按钮不存在";
        String buttonCustomId = "";
        List<ItemButtonBind> bibList =
            buttonItemBindRepository.findByItemIdAndButtonTypeAndProcessDefinitionIdAndTaskDefKeyOrderByTabIndexAsc(
                itemId, buttonType, processDefinitionId, taskDefKey);
        for (ItemButtonBind bib : bibList) {
            if (buttonType == ItemButtonTypeEnum.COMMON.getValue()) {
                CommonButton cb = commonButtonService.findOne(bib.getButtonId());
                if (null != cb) {
                    buttonName = cb.getName();
                    buttonCustomId = cb.getCustomId();
                }
            } else {
                SendButton sb = sendButtonService.findOne(bib.getButtonId());
                if (null != sb) {
                    buttonName = sb.getName();
                    buttonCustomId = sb.getCustomId();
                }
            }
            bib.setButtonName(buttonName);
            bib.setButtonCustomId(buttonCustomId);
        }
        return bibList;
    }

    @Override
    public List<ItemButtonBind> findListByButtonId(String buttonId) {
        List<ItemButtonBind> bindList =
            buttonItemBindRepository.findByButtonIdOrderByItemIdDescUpdateTimeDesc(buttonId);
        for (ItemButtonBind bind : bindList) {
            List<ItemButtonRole> roleList = itemButtonRoleService.findByItemButtonIdContainRoleName(bind.getId());
            String roleNames = "";
            for (ItemButtonRole role : roleList) {
                if (StringUtils.isEmpty(roleNames)) {
                    roleNames = role.getRoleName();
                } else {
                    roleNames += "、" + role.getRoleName();
                }
            }
            bind.setRoleNames(roleNames);
        }
        return bindList;
    }

    @Override
    public List<ItemButtonBind> findListContainRole(String itemId, Integer buttonType, String processDefinitionId,
        String taskDefineKey) {
        String buttonName = "按钮不存在";
        String buttonCustomId = "";
        List<ItemButtonBind> bindList =
            buttonItemBindRepository.findByItemIdAndButtonTypeAndProcessDefinitionIdAndTaskDefKeyOrderByTabIndexAsc(
                itemId, buttonType, processDefinitionId, taskDefineKey);
        for (ItemButtonBind bind : bindList) {
            if (buttonType == ItemButtonTypeEnum.COMMON.getValue()) {
                CommonButton cb = commonButtonService.findOne(bind.getButtonId());
                if (null != cb) {
                    buttonName = cb.getName();
                    buttonCustomId = cb.getCustomId();
                }
            } else {
                SendButton sb = sendButtonService.findOne(bind.getButtonId());
                if (null != sb) {
                    buttonName = sb.getName();
                    buttonCustomId = sb.getCustomId();
                }
            }
            bind.setButtonName(buttonName);
            bind.setButtonCustomId(buttonCustomId);

            List<ItemButtonRole> roleList = itemButtonRoleService.findByItemButtonIdContainRoleName(bind.getId());
            List<String> roleIds = new ArrayList<>();
            String roleNames = "";
            for (ItemButtonRole role : roleList) {
                // 存绑定关系id，便于删除
                roleIds.add(role.getId());
                if (StringUtils.isEmpty(roleNames)) {
                    roleNames = role.getRoleName();
                } else {
                    roleNames += "、" + role.getRoleName();
                }
            }
            bind.setRoleIds(roleIds);
            bind.setRoleNames(roleNames);
        }
        return bindList;
    }

    @Override
    public List<ItemButtonBind> findListContainRoleId(String itemId, Integer buttonType, String processDefinitionId,
        String taskDefineKey) {
        String buttonName = "按钮不存在";
        String buttonCustomId = "";
        List<ItemButtonBind> bindList =
            buttonItemBindRepository.findByItemIdAndButtonTypeAndProcessDefinitionIdAndTaskDefKeyOrderByTabIndexAsc(
                itemId, buttonType, processDefinitionId, taskDefineKey);
        if (bindList.isEmpty()) {
            bindList = buttonItemBindRepository
                .findByItemIdAndButtonTypeAndProcessDefinitionIdAndTaskDefKeyIsNullOrderByTabIndexAsc(itemId,
                    buttonType, processDefinitionId);
        }
        for (ItemButtonBind bind : bindList) {
            if (buttonType == ItemButtonTypeEnum.COMMON.getValue()) {
                CommonButton cb = commonButtonService.findOne(bind.getButtonId());
                if (null != cb) {
                    buttonName = cb.getName();
                    buttonCustomId = cb.getCustomId();
                }
            } else {
                SendButton sb = sendButtonService.findOne(bind.getButtonId());
                if (null != sb) {
                    buttonName = sb.getName();
                    buttonCustomId = sb.getCustomId();
                }
            }
            bind.setButtonName(buttonName);
            bind.setButtonCustomId(buttonCustomId);

            List<ItemButtonRole> roleList = itemButtonRoleService.findByItemButtonId(bind.getId());
            List<String> roleIds = new ArrayList<>();
            for (ItemButtonRole role : roleList) {
                roleIds.add(role.getRoleId());
            }
            bind.setRoleIds(roleIds);
        }
        return bindList;
    }

    @Override
    public List<ItemButtonBind> findListExtra(String itemId, Integer buttonType, String processDefinitionId,
        String taskDefineKey) {
        String buttonName = "按钮不存在";
        String buttonCustomId = "";
        List<ItemButtonBind> bibList =
            buttonItemBindRepository.findByItemIdAndButtonTypeAndProcessDefinitionIdAndTaskDefKeyOrderByTabIndexAsc(
                itemId, buttonType, processDefinitionId, taskDefineKey);
        if (bibList.isEmpty()) {
            bibList =
                buttonItemBindRepository.findByItemIdAndButtonTypeAndProcessDefinitionIdAndTaskDefKeyOrderByTabIndexAsc(
                    itemId, buttonType, processDefinitionId, "");
        }
        for (ItemButtonBind bib : bibList) {
            if (buttonType == ItemButtonTypeEnum.COMMON.getValue()) {
                CommonButton cb = commonButtonService.findOne(bib.getButtonId());
                if (null != cb) {
                    buttonName = cb.getName();
                    buttonCustomId = cb.getCustomId();
                }
            } else {
                SendButton sb = sendButtonService.findOne(bib.getButtonId());
                if (null != sb) {
                    buttonName = sb.getName();
                    buttonCustomId = sb.getCustomId();
                }
            }
            bib.setButtonName(buttonName);
            bib.setButtonCustomId(buttonCustomId);
        }
        return bibList;
    }

    @Override
    public ItemButtonBind findOne(String id) {
        return buttonItemBindRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = false)
    public void removeButtonItemBinds(String[] buttonItemBindIds) {
        for (String buttonItemBindId : buttonItemBindIds) {
            itemButtonRoleService.deleteByItemButtonId(buttonItemBindId);
            buttonItemBindRepository.deleteById(buttonItemBindId);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public ItemButtonBind save(ItemButtonBind buttonItemBind) {
        UserInfo person = Y9LoginUserHolder.getUserInfo();
        String userId = person.getPersonId(), userName = person.getName(), tenantId = Y9LoginUserHolder.getTenantId();

        String id = buttonItemBind.getId();
        ItemButtonBind oldbib = this.findOne(id);
        if (null != oldbib) {
            oldbib.setTenantId(tenantId);
            oldbib.setUpdateTime(sdf.format(new Date()));
            oldbib.setUserId(userId);
            oldbib.setUserName(userName);

            String buttonName = "按钮不存在";
            String buttonCustomId = "";
            if (ItemButtonTypeEnum.COMMON.getValue() == oldbib.getButtonType()) {
                CommonButton cb = commonButtonService.findOne(oldbib.getButtonId());
                if (null != cb) {
                    buttonName = cb.getName();
                    buttonCustomId = cb.getCustomId();
                }
            } else {
                SendButton sb = sendButtonService.findOne(oldbib.getButtonId());
                if (null != sb) {
                    buttonName = sb.getName();
                    buttonCustomId = sb.getCustomId();
                }
            }
            oldbib.setButtonName(buttonName);
            oldbib.setButtonCustomId(buttonCustomId);

            buttonItemBindRepository.save(oldbib);
            return oldbib;
        } else {
            return buttonItemBindRepository.save(buttonItemBind);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public void saveOrder(String[] idAndTabIndexs) {
        UserInfo person = Y9LoginUserHolder.getUserInfo();
        String userId = person.getPersonId(), userName = person.getName();
        List<ItemButtonBind> oldtibList = new ArrayList<>();
        for (String idAndTabIndex : idAndTabIndexs) {
            String[] arr = idAndTabIndex.split(SysVariables.COLON);
            ItemButtonBind oldbib = this.findOne(arr[0]);
            oldbib.setTabIndex(Integer.valueOf(arr[1]));
            oldbib.setUpdateTime(sdf.format(new Date()));
            oldbib.setUserId(userId);
            oldbib.setUserName(userName);

            oldtibList.add(oldbib);
        }
        buttonItemBindRepository.saveAll(oldtibList);
    }
}