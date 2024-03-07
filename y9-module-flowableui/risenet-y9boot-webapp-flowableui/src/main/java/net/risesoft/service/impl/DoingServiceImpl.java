package net.risesoft.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.risesoft.api.itemadmin.ChaoSongInfoApi;
import net.risesoft.api.itemadmin.FormDataApi;
import net.risesoft.api.itemadmin.ItemApi;
import net.risesoft.api.itemadmin.OfficeFollowApi;
import net.risesoft.api.itemadmin.ProcessParamApi;
import net.risesoft.api.itemadmin.RemindInstanceApi;
import net.risesoft.api.itemadmin.SpeakInfoApi;
import net.risesoft.api.platform.org.PersonApi;
import net.risesoft.api.processadmin.DoingApi;
import net.risesoft.api.processadmin.IdentityApi;
import net.risesoft.api.processadmin.TaskApi;
import net.risesoft.enums.ItemLeaveTypeEnum;
import net.risesoft.model.platform.Person;
import net.risesoft.model.itemadmin.ItemModel;
import net.risesoft.model.itemadmin.ProcessParamModel;
import net.risesoft.model.itemadmin.RemindInstanceModel;
import net.risesoft.model.processadmin.IdentityLinkModel;
import net.risesoft.model.processadmin.ProcessInstanceModel;
import net.risesoft.model.processadmin.TaskModel;
import net.risesoft.model.user.UserInfo;
import net.risesoft.pojo.Y9Page;
import net.risesoft.service.DoingService;
import net.risesoft.util.SysVariables;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.util.Y9Util;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2023/01/03
 */
@Service(value = "doingService")
@Transactional(readOnly = true)
public class DoingServiceImpl implements DoingService {

    @Autowired
    private DoingApi doingManager;

    @Autowired
    private TaskApi taskManager;

    @Autowired
    private ItemApi itemManager;

    @Autowired
    private PersonApi personApi;

    @Autowired
    private ProcessParamApi processParamManager;

    @Autowired
    private ChaoSongInfoApi chaoSongInfoManager;

    @Autowired
    private FormDataApi formDataManager;

    @Autowired
    private SpeakInfoApi speakInfoManager;

    @Autowired
    private RemindInstanceApi remindInstanceManager;

    @Autowired
    private OfficeFollowApi officeFollowManager;

    @Autowired
    private IdentityApi identityManager;

    /**
     * 当并行的时候，会获取到多个task，为了并行时当前办理人显示多人，而不是显示多条记录，需要分开分别进行处理
     *
     * @return
     */
    private List<String> getAssigneeIdsAndAssigneeNames(List<TaskModel> taskList) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        String taskIds = "", assigneeIds = "";
        StringBuffer assigneeNames = new StringBuffer();
        List<String> list = new ArrayList<String>();
        int i = 0;
        if (taskList.size() > 0) {
            for (TaskModel task : taskList) {
                if (StringUtils.isEmpty(taskIds)) {
                    taskIds = task.getId();
                    String assignee = task.getAssignee();
                    if (StringUtils.isNotBlank(assignee)) {
                        assigneeIds = assignee;
                        Person personTemp = personApi.getPerson(tenantId, assignee).getData();
                        if (personTemp != null) {
                            assigneeNames.append(personTemp.getName());
                            i += 1;
                        }
                    } else {// 处理单实例未签收的当前办理人显示
                        List<IdentityLinkModel> iList = identityManager.getIdentityLinksForTask(tenantId, task.getId());
                        if (!iList.isEmpty()) {
                            int j = 0;
                            for (IdentityLinkModel identityLink : iList) {
                                String assigneeId = identityLink.getUserId();
                                Person ownerUser =
                                    personApi.getPerson(Y9LoginUserHolder.getTenantId(), assigneeId).getData();
                                if (j < 5) {
                                    assigneeNames = Y9Util.genCustomStr(assigneeNames,
                                        ownerUser.getName() + (ownerUser.getDisabled() ? "(已禁用)" : ""), "、");
                                    assigneeIds = Y9Util.genCustomStr(assigneeIds, assigneeId, SysVariables.COMMA);
                                } else {
                                    assigneeNames.append("等，共" + iList.size() + "人");
                                    break;
                                }
                                j++;
                            }
                        }
                    }
                } else {
                    taskIds = Y9Util.genCustomStr(taskIds, task.getId(), SysVariables.COMMA);
                    String assignee = task.getAssignee();
                    if (i < 5) {
                        if (StringUtils.isNotBlank(assignee)) {
                            assigneeIds = Y9Util.genCustomStr(assigneeIds, task.getAssignee(), SysVariables.COMMA);
                            Person personTemp = personApi.getPerson(tenantId, assignee).getData();
                            if (personTemp != null) {
                                assigneeNames = Y9Util.genCustomStr(assigneeNames, personTemp.getName(), "、");
                                i += 1;
                            }
                        }
                    }
                }
            }
            boolean b = taskList.size() > 5;
            if (b) {
                assigneeNames.append("等，共" + taskList.size() + "人");
            }
        } else {

        }
        list.add(taskIds);
        list.add(assigneeIds);
        list.add(assigneeNames.toString());
        return list;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> list(String itemId, String searchTerm, Integer page, Integer rows) {
        Map<String, Object> retMap = new HashMap<String, Object>(16);
        try {
            List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
            String userId = userInfo.getPersonId(), tenantId = Y9LoginUserHolder.getTenantId();
            ItemModel item = itemManager.getByItemId(tenantId, itemId);
            String processDefinitionKey = item.getWorkflowGuid(), itemName = item.getName();
            if (StringUtils.isBlank(searchTerm)) {
                // retMap = doingManager.getListByUserIdAndProcessDefinitionKey(tenantId,
                // userId, processDefinitionKey, page, rows);
                retMap = doingManager.getListByUserIdAndProcessDefinitionKeyOrderBySendTime(tenantId, userId,
                    processDefinitionKey, page, rows);
                List<Map<String, Object>> list = (List<Map<String, Object>>)retMap.get("rows");
                ObjectMapper objectMapper = new ObjectMapper();
                List<Map<String, Object>> hpiModelList =
                    objectMapper.convertValue(list, new TypeReference<List<Map<String, Object>>>() {});
                int serialNumber = (page - 1) * rows;
                Map<String, Object> mapTemp = null;
                ProcessParamModel processParam = null;
                for (Map<String, Object> hpim : hpiModelList) {
                    mapTemp = new HashMap<String, Object>(16);
                    try {
                        String processInstanceId = (String)hpim.get("processInstanceId");
                        String processDefinitionId = (String)hpim.get("processDefinitionId");
                        String taskCreateTime = hpim.get("endTime") != null ? sdf.format(hpim.get("endTime")) : "";
                        List<TaskModel> taskList = taskManager.findByProcessInstanceId(tenantId, processInstanceId);
                        List<String> listTemp = getAssigneeIdsAndAssigneeNames(taskList);
                        String taskIds = listTemp.get(0), assigneeIds = listTemp.get(1),
                            assigneeNames = listTemp.get(2);
                        Boolean isReminder = String.valueOf(taskList.get(0).getPriority()).contains("5");
                        processParam = processParamManager.findByProcessInstanceId(tenantId, processInstanceId);
                        String processSerialNumber = processParam.getProcessSerialNumber();
                        String documentTitle =
                            StringUtils.isBlank(processParam.getTitle()) ? "无标题" : processParam.getTitle();
                        String level = processParam.getCustomLevel();
                        String number = processParam.getCustomNumber();
                        mapTemp.put("itemName", itemName);
                        mapTemp.put("processInstanceId", processInstanceId);
                        mapTemp.put("processDefinitionKey", processDefinitionKey);
                        mapTemp.put(SysVariables.PROCESSSERIALNUMBER, processSerialNumber);
                        mapTemp.put("processDefinitionId", processDefinitionId);
                        mapTemp.put(SysVariables.DOCUMENTTITLE, documentTitle);
                        mapTemp.put("taskDefinitionKey", taskList.get(0).getTaskDefinitionKey());
                        mapTemp.put("taskName", taskList.get(0).getName());
                        mapTemp.put("taskCreateTime", taskCreateTime);
                        mapTemp.put("taskId", taskIds);
                        mapTemp.put("taskAssigneeId", assigneeIds);
                        mapTemp.put("taskAssignee", assigneeNames);
                        mapTemp.put(SysVariables.ITEMID, itemId);
                        mapTemp.put(SysVariables.LEVEL, level);
                        mapTemp.put(SysVariables.NUMBER, number);
                        mapTemp.put("isReminder", isReminder);
                        int chaosongNum =
                            chaoSongInfoManager.countByUserIdAndProcessInstanceId(tenantId, userId, processInstanceId);
                        mapTemp.put("chaosongNum", chaosongNum);
                        mapTemp.put("status", 1);
                        mapTemp.put("taskDueDate", "");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mapTemp.put("serialNumber", serialNumber + 1);
                    serialNumber += 1;
                    items.add(mapTemp);
                }
            } else {
                retMap = doingManager.searchListByUserIdAndProcessDefinitionKey(tenantId, userId, processDefinitionKey,
                    searchTerm, page, rows);
                List<ProcessInstanceModel> hpiModelList = (List<ProcessInstanceModel>)retMap.get("rows");
                int serialNumber = (page - 1) * rows;
                Map<String, Object> mapTemp = null;
                ProcessParamModel processParam = null;
                for (ProcessInstanceModel hpim : hpiModelList) {
                    mapTemp = new HashMap<String, Object>(16);
                    try {
                        String processInstanceId = hpim.getId();
                        String processDefinitionId = hpim.getProcessDefinitionId();
                        List<TaskModel> taskList = taskManager.findByProcessInstanceId(tenantId, processInstanceId);
                        List<String> listTemp = getAssigneeIdsAndAssigneeNames(taskList);
                        String taskIds = listTemp.get(0), assigneeIds = listTemp.get(1),
                            assigneeNames = listTemp.get(2);
                        Boolean isReminder = String.valueOf(taskList.get(0).getPriority()).contains("5");
                        processParam = processParamManager.findByProcessInstanceId(tenantId, processInstanceId);
                        String processSerialNumber = processParam.getProcessSerialNumber();
                        String documentTitle =
                            StringUtils.isBlank(processParam.getTitle()) ? "无标题" : processParam.getTitle();
                        String level = processParam.getCustomLevel();
                        String number = processParam.getCustomNumber();
                        mapTemp.put("itemName", itemName);
                        mapTemp.put("processInstanceId", processInstanceId);
                        mapTemp.put("processDefinitionKey", processDefinitionKey);
                        mapTemp.put(SysVariables.PROCESSSERIALNUMBER, processSerialNumber);
                        mapTemp.put("processDefinitionId", processDefinitionId);
                        mapTemp.put(SysVariables.DOCUMENTTITLE, documentTitle);
                        mapTemp.put("taskDefinitionKey", taskList.get(0).getTaskDefinitionKey());
                        mapTemp.put("taskName", taskList.get(0).getName());
                        mapTemp.put("taskCreateTime", sdf.format(taskList.get(0).getCreateTime()));
                        mapTemp.put("taskId", taskIds);
                        mapTemp.put("taskAssigneeId", assigneeIds);
                        mapTemp.put("taskAssignee", assigneeNames);
                        mapTemp.put(SysVariables.ITEMID, itemId);
                        mapTemp.put(SysVariables.LEVEL, level);
                        mapTemp.put(SysVariables.NUMBER, number);
                        mapTemp.put("isReminder", isReminder);
                        int chaosongNum =
                            chaoSongInfoManager.countByUserIdAndProcessInstanceId(tenantId, userId, processInstanceId);
                        mapTemp.put("chaosongNum", chaosongNum);
                        mapTemp.put("status", 1);
                        mapTemp.put("taskDueDate", "");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mapTemp.put("serialNumber", serialNumber + 1);
                    serialNumber += 1;
                    items.add(mapTemp);
                }
            }
            retMap.put("rows", items);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retMap;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Y9Page<Map<String, Object>> listNew(String itemId, String searchTerm, Integer page, Integer rows) {
        Map<String, Object> retMap = new HashMap<String, Object>(16);
        try {
            List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
            SimpleDateFormat sdfT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
            String userId = userInfo.getPersonId(), tenantId = Y9LoginUserHolder.getTenantId();
            ItemModel item = itemManager.getByItemId(tenantId, itemId);
            String processDefinitionKey = item.getWorkflowGuid(), itemName = item.getName();
            if (StringUtils.isBlank(searchTerm)) {
                retMap = doingManager.getListByUserIdAndProcessDefinitionKeyOrderBySendTime(tenantId, userId,
                    processDefinitionKey, page, rows);
                List<Map<String, Object>> list = (List<Map<String, Object>>)retMap.get("rows");
                ObjectMapper objectMapper = new ObjectMapper();
                List<Map<String, Object>> hpiModelList =
                    objectMapper.convertValue(list, new TypeReference<List<Map<String, Object>>>() {});
                int serialNumber = (page - 1) * rows;
                Map<String, Object> mapTemp = null;
                Map<String, Object> formDataMap = null;
                ProcessParamModel processParam = null;
                ItemLeaveTypeEnum[] arr = ItemLeaveTypeEnum.values();
                for (Map<String, Object> hpim : hpiModelList) {
                    mapTemp = new HashMap<String, Object>(16);
                    try {
                        String processInstanceId = (String)hpim.get("processInstanceId");
                        String processDefinitionId = (String)hpim.get("processDefinitionId");
                        Date endTime = sdfT.parse(hpim.get("endTime").toString());
                        String taskCreateTime = hpim.get("endTime") != null ? sdf.format(endTime) : "";
                        List<TaskModel> taskList = taskManager.findByProcessInstanceId(tenantId, processInstanceId);
                        List<String> listTemp = getAssigneeIdsAndAssigneeNames(taskList);
                        String taskIds = listTemp.get(0), assigneeIds = listTemp.get(1),
                            assigneeNames = listTemp.get(2);
                        Boolean isReminder = String.valueOf(taskList.get(0).getPriority()).contains("5");
                        processParam = processParamManager.findByProcessInstanceId(tenantId, processInstanceId);
                        String processSerialNumber = processParam.getProcessSerialNumber();
                        String documentTitle =
                            StringUtils.isBlank(processParam.getTitle()) ? "无标题" : processParam.getTitle();
                        String level = processParam.getCustomLevel();
                        String number = processParam.getCustomNumber();
                        mapTemp.put("itemName", itemName);
                        mapTemp.put("processDefinitionKey", processDefinitionKey);
                        mapTemp.put(SysVariables.PROCESSSERIALNUMBER, processSerialNumber);
                        mapTemp.put("processDefinitionId", processDefinitionId);
                        mapTemp.put(SysVariables.DOCUMENTTITLE, documentTitle);
                        mapTemp.put("taskDefinitionKey", taskList.get(0).getTaskDefinitionKey());
                        mapTemp.put("taskName", taskList.get(0).getName());
                        mapTemp.put("taskCreateTime", taskCreateTime);
                        mapTemp.put("taskId", taskIds);
                        mapTemp.put("taskAssigneeId", assigneeIds);
                        mapTemp.put("taskAssignee", assigneeNames);
                        mapTemp.put(SysVariables.ITEMID, itemId);
                        mapTemp.put(SysVariables.LEVEL, level);
                        mapTemp.put(SysVariables.NUMBER, number);
                        mapTemp.put("isReminder", isReminder);
                        int chaosongNum =
                            chaoSongInfoManager.countByUserIdAndProcessInstanceId(tenantId, userId, processInstanceId);
                        mapTemp.put("chaosongNum", chaosongNum);
                        mapTemp.put("status", 1);
                        mapTemp.put("taskDueDate", "");
                        formDataMap = formDataManager.getData(tenantId, itemId, processSerialNumber);
                        if (formDataMap.get("leaveType") != null) {
                            String leaveType = (String)formDataMap.get("leaveType");
                            for (ItemLeaveTypeEnum leaveTypeEnum : arr) {
                                if (leaveType.equals(leaveTypeEnum.getValue())) {
                                    formDataMap.put("leaveType", leaveTypeEnum.getName());
                                    break;
                                }
                            }
                        }
                        mapTemp.putAll(formDataMap);
                        mapTemp.put("processInstanceId", processInstanceId);
                        int speakInfoNum = speakInfoManager.getNotReadCount(tenantId, userId, processInstanceId);
                        mapTemp.put("speakInfoNum", speakInfoNum);

                        mapTemp.put("remindSetting", false);
                        RemindInstanceModel remindInstanceModel =
                            remindInstanceManager.getRemindInstance(tenantId, userId, processInstanceId);
                        if (remindInstanceModel != null) {
                            mapTemp.put("remindSetting", true);
                        }

                        int countFollow =
                            officeFollowManager.countByProcessInstanceId(tenantId, userId, processInstanceId);
                        mapTemp.put("follow", countFollow > 0 ? true : false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mapTemp.put("serialNumber", serialNumber + 1);
                    serialNumber += 1;
                    items.add(mapTemp);
                }
            } else {
                retMap = doingManager.searchListByUserIdAndProcessDefinitionKey(tenantId, userId, processDefinitionKey,
                    searchTerm, page, rows);
                List<ProcessInstanceModel> list = (List<ProcessInstanceModel>)retMap.get("rows");
                ObjectMapper objectMapper = new ObjectMapper();
                List<ProcessInstanceModel> hpiModelList =
                    objectMapper.convertValue(list, new TypeReference<List<ProcessInstanceModel>>() {});
                int serialNumber = (page - 1) * rows;
                Map<String, Object> mapTemp = null;
                Map<String, Object> formDataMap = null;
                ProcessParamModel processParam = null;
                for (ProcessInstanceModel hpim : hpiModelList) {
                    mapTemp = new HashMap<String, Object>(16);
                    try {
                        String processInstanceId = hpim.getId();
                        String processDefinitionId = hpim.getProcessDefinitionId();
                        List<TaskModel> taskList = taskManager.findByProcessInstanceId(tenantId, processInstanceId);
                        List<String> listTemp = getAssigneeIdsAndAssigneeNames(taskList);
                        String taskIds = listTemp.get(0), assigneeIds = listTemp.get(1),
                            assigneeNames = listTemp.get(2);
                        Boolean isReminder = String.valueOf(taskList.get(0).getPriority()).contains("5");
                        processParam = processParamManager.findByProcessInstanceId(tenantId, processInstanceId);
                        String processSerialNumber = processParam.getProcessSerialNumber();
                        String documentTitle =
                            StringUtils.isBlank(processParam.getTitle()) ? "无标题" : processParam.getTitle();
                        String level = processParam.getCustomLevel();
                        String number = processParam.getCustomNumber();
                        mapTemp.put("itemName", itemName);
                        mapTemp.put("processInstanceId", processInstanceId);
                        mapTemp.put("processDefinitionKey", processDefinitionKey);
                        mapTemp.put(SysVariables.PROCESSSERIALNUMBER, processSerialNumber);
                        mapTemp.put("processDefinitionId", processDefinitionId);
                        mapTemp.put(SysVariables.DOCUMENTTITLE, documentTitle);
                        mapTemp.put("taskDefinitionKey", taskList.get(0).getTaskDefinitionKey());
                        mapTemp.put("taskName", taskList.get(0).getName());
                        mapTemp.put("taskCreateTime", sdf.format(taskList.get(0).getCreateTime()));
                        mapTemp.put("taskId", taskIds);
                        mapTemp.put("taskAssigneeId", assigneeIds);
                        mapTemp.put("taskAssignee", assigneeNames);
                        mapTemp.put(SysVariables.ITEMID, itemId);
                        mapTemp.put(SysVariables.LEVEL, level);
                        mapTemp.put(SysVariables.NUMBER, number);
                        mapTemp.put("isReminder", isReminder);
                        int chaosongNum =
                            chaoSongInfoManager.countByUserIdAndProcessInstanceId(tenantId, userId, processInstanceId);
                        mapTemp.put("chaosongNum", chaosongNum);
                        mapTemp.put("status", 1);
                        mapTemp.put("taskDueDate", "");
                        formDataMap = formDataManager.getData(tenantId, itemId, processSerialNumber);
                        mapTemp.putAll(formDataMap);

                        int speakInfoNum = speakInfoManager.getNotReadCount(tenantId, userId, processInstanceId);
                        mapTemp.put("speakInfoNum", speakInfoNum);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mapTemp.put("serialNumber", serialNumber + 1);
                    serialNumber += 1;
                    items.add(mapTemp);
                }
            }
            return Y9Page.success(page, Integer.parseInt(retMap.get("totalpages").toString()),
                Integer.parseInt(retMap.get("total").toString()), items, "获取列表成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Y9Page.success(page, 0, 0, new ArrayList<Map<String, Object>>(), "获取列表失败");
    }

}