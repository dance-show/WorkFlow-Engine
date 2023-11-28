package net.risesoft.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

import net.risesoft.api.itemadmin.ProcessParamApi;
import net.risesoft.api.itemadmin.TaskVariableApi;
import net.risesoft.api.itemadmin.position.Item4PositionApi;
import net.risesoft.api.itemadmin.position.OfficeDoneInfo4PositionApi;
import net.risesoft.api.itemadmin.position.OfficeFollow4PositionApi;
import net.risesoft.api.org.PositionApi;
import net.risesoft.api.processadmin.DoingApi;
import net.risesoft.api.processadmin.IdentityApi;
import net.risesoft.api.processadmin.ProcessDefinitionApi;
import net.risesoft.api.processadmin.ProcessTodoApi;
import net.risesoft.api.processadmin.TaskApi;
import net.risesoft.api.processadmin.VariableApi;
import net.risesoft.model.itemadmin.ItemModel;
import net.risesoft.model.itemadmin.OfficeDoneInfoModel;
import net.risesoft.model.itemadmin.ProcessParamModel;
import net.risesoft.model.itemadmin.TaskVariableModel;
import net.risesoft.model.platform.Position;
import net.risesoft.model.processadmin.IdentityLinkModel;
import net.risesoft.model.processadmin.ProcessInstanceModel;
import net.risesoft.model.processadmin.TaskModel;
import net.risesoft.pojo.Y9Page;
import net.risesoft.service.WorkList4ddyjsService;
import net.risesoft.util.SysVariables;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.util.Y9Util;

@Service(value = "workList4ddyjsService")
@Transactional(readOnly = true)
public class WorkList4ddyjsServiceImpl implements WorkList4ddyjsService {

    @Autowired
    private DoingApi doingManager;

    @Autowired
    private TaskApi taskManager;

    @Autowired
    private Item4PositionApi itemManager;

    @Autowired
    private PositionApi positionManager;

    @Autowired
    private ProcessParamApi processParamManager;

    @Autowired
    private OfficeFollow4PositionApi officeFollowManager;

    @Autowired
    private IdentityApi identityManager;

    @Autowired
    private OfficeDoneInfo4PositionApi officeDoneInfoManager;

    @Autowired
    private ProcessTodoApi todoManager;

    @Autowired
    private VariableApi variableManager;

    @Autowired
    private ProcessDefinitionApi processDefinitionManager;

    @Autowired
    private TaskVariableApi taskVariableManager;

    @Value("${y9.common.flowableBaseUrl}")
    private String flowableBaseUrl;

    @SuppressWarnings({"unchecked"})
    @Override
    public Y9Page<Map<String, Object>> doingList(String itemId, String searchTerm, Integer page, Integer rows) {
        Map<String, Object> retMap = new HashMap<String, Object>(16);
        try {
            List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String positionId = Y9LoginUserHolder.getPositionId(), tenantId = Y9LoginUserHolder.getTenantId();
            ItemModel item = itemManager.getByItemId(tenantId, itemId);
            if (StringUtils.isBlank(searchTerm)) {
                retMap = doingManager.getListByUserIdAndSystemName(tenantId, positionId, item.getSystemName(), page, rows);
                List<ProcessInstanceModel> list = (List<ProcessInstanceModel>)retMap.get("rows");
                ObjectMapper objectMapper = new ObjectMapper();
                List<ProcessInstanceModel> hpiModelList = objectMapper.convertValue(list, new TypeReference<List<ProcessInstanceModel>>() {});
                int serialNumber = (page - 1) * rows;
                Map<String, Object> mapTemp = null;
                ProcessParamModel processParam = null;
                for (ProcessInstanceModel hpim : hpiModelList) {// 以办理时间排序
                    mapTemp = new HashMap<String, Object>(16);
                    try {
                        String processInstanceId = hpim.getId();
                        String processDefinitionId = hpim.getProcessDefinitionId();
                        String taskCreateTime = sdf.format(hpim.getStartTime());
                        List<TaskModel> taskList = taskManager.findByProcessInstanceId(tenantId, processInstanceId);
                        List<String> listTemp = getAssigneeIdsAndAssigneeNames(taskList);
                        String taskIds = listTemp.get(0), assigneeIds = listTemp.get(1), assigneeNames = listTemp.get(2);
                        Boolean isReminder = String.valueOf(taskList.get(0).getPriority()).contains("5");
                        processParam = processParamManager.findByProcessInstanceId(tenantId, processInstanceId);
                        String processSerialNumber = processParam.getProcessSerialNumber();
                        String documentTitle = StringUtils.isBlank(processParam.getTitle()) ? "无标题" : processParam.getTitle();
                        String level = processParam.getCustomLevel();
                        String number = processParam.getCustomNumber();
                        mapTemp.put("itemId", processParam.getItemId());
                        mapTemp.put("itemName", processParam.getItemName());
                        mapTemp.put("processDefinitionKey", hpim.getProcessDefinitionKey());
                        mapTemp.put(SysVariables.PROCESSSERIALNUMBER, processSerialNumber);
                        mapTemp.put("processDefinitionId", processDefinitionId);
                        mapTemp.put("title", documentTitle);
                        mapTemp.put("taskDefinitionKey", taskList.get(0).getTaskDefinitionKey());
                        mapTemp.put("taskName", taskList.get(0).getName());
                        mapTemp.put("taskCreateTime", taskCreateTime);
                        mapTemp.put("taskId", taskIds);
                        mapTemp.put("taskAssigneeId", assigneeIds);
                        mapTemp.put("taskAssignee", assigneeNames);
                        mapTemp.put(SysVariables.LEVEL, level);
                        mapTemp.put(SysVariables.NUMBER, number);
                        mapTemp.put("isReminder", isReminder);
                        mapTemp.put("chaosongNum", 0);
                        mapTemp.put("status", 1);
                        mapTemp.put("taskDueDate", "");
                        mapTemp.put("processInstanceId", processInstanceId);
                        mapTemp.put("speakInfoNum", 0);
                        mapTemp.put("remindSetting", false);
                        int countFollow = officeFollowManager.countByProcessInstanceId(tenantId, positionId, processInstanceId);
                        mapTemp.put("follow", countFollow > 0 ? true : false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mapTemp.put("serialNumber", serialNumber + 1);
                    serialNumber += 1;
                    items.add(mapTemp);
                }
            } else {
                retMap = doingManager.searchListByUserIdAndSystemName(tenantId, positionId, item.getSystemName(), searchTerm, page, rows);
                List<ProcessInstanceModel> list = (List<ProcessInstanceModel>)retMap.get("rows");
                ObjectMapper objectMapper = new ObjectMapper();
                List<ProcessInstanceModel> hpiModelList = objectMapper.convertValue(list, new TypeReference<List<ProcessInstanceModel>>() {});
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
                        String taskIds = listTemp.get(0), assigneeIds = listTemp.get(1), assigneeNames = listTemp.get(2);
                        Boolean isReminder = String.valueOf(taskList.get(0).getPriority()).contains("5");
                        processParam = processParamManager.findByProcessInstanceId(tenantId, processInstanceId);
                        String processSerialNumber = processParam.getProcessSerialNumber();
                        String documentTitle = StringUtils.isBlank(processParam.getTitle()) ? "无标题" : processParam.getTitle();
                        String level = processParam.getCustomLevel();
                        String number = processParam.getCustomNumber();
                        mapTemp.put("itemId", processParam.getItemId());
                        mapTemp.put("itemName", processParam.getItemName());
                        mapTemp.put("processInstanceId", processInstanceId);
                        mapTemp.put("processDefinitionKey", hpim.getProcessDefinitionKey());
                        mapTemp.put(SysVariables.PROCESSSERIALNUMBER, processSerialNumber);
                        mapTemp.put("processDefinitionId", processDefinitionId);
                        mapTemp.put(SysVariables.DOCUMENTTITLE, documentTitle);
                        mapTemp.put("taskDefinitionKey", taskList.get(0).getTaskDefinitionKey());
                        mapTemp.put("taskName", taskList.get(0).getName());
                        mapTemp.put("taskCreateTime", sdf.format(hpim.getStartTime()));
                        mapTemp.put("taskId", taskIds);
                        mapTemp.put("taskAssigneeId", assigneeIds);
                        mapTemp.put("taskAssignee", assigneeNames);
                        mapTemp.put(SysVariables.ITEMID, itemId);
                        mapTemp.put(SysVariables.LEVEL, level);
                        mapTemp.put(SysVariables.NUMBER, number);
                        mapTemp.put("isReminder", isReminder);
                        mapTemp.put("chaosongNum", 0);
                        mapTemp.put("status", 1);
                        mapTemp.put("taskDueDate", "");
                        mapTemp.put("speakInfoNum", 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mapTemp.put("serialNumber", serialNumber + 1);
                    serialNumber += 1;
                    items.add(mapTemp);
                }
            }
            return Y9Page.success(page, Integer.parseInt(retMap.get("totalpages").toString()), Integer.parseInt(retMap.get("total").toString()), items, "获取列表成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Y9Page.success(page, 0, 0, new ArrayList<Map<String, Object>>(), "获取列表失败");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Y9Page<Map<String, Object>> doneList(String itemId, String searchTerm, Integer page, Integer rows) {
        Map<String, Object> retMap = new HashMap<String, Object>(16);
        String userId = Y9LoginUserHolder.getPositionId(), tenantId = Y9LoginUserHolder.getTenantId();
        ItemModel item = itemManager.getByItemId(tenantId, itemId);
        retMap = officeDoneInfoManager.searchByPositionIdAndSystemName(tenantId, userId, searchTerm, item.getSystemName(), "", "", page, rows);
        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        List<OfficeDoneInfoModel> list = (List<OfficeDoneInfoModel>)retMap.get("rows");
        ObjectMapper objectMapper = new ObjectMapper();
        List<OfficeDoneInfoModel> hpiModelList = objectMapper.convertValue(list, new TypeReference<List<OfficeDoneInfoModel>>() {});
        int serialNumber = (page - 1) * rows;
        Map<String, Object> mapTemp = null;
        for (OfficeDoneInfoModel hpim : hpiModelList) {
            mapTemp = new HashMap<String, Object>(16);
            String processInstanceId = hpim.getProcessInstanceId();
            try {
                String processDefinitionId = hpim.getProcessDefinitionId();
                String startTime = hpim.getStartTime().substring(0, 16), endTime = hpim.getEndTime().substring(0, 16);
                String processSerialNumber = hpim.getProcessSerialNumber();
                String documentTitle = StringUtils.isBlank(hpim.getTitle()) ? "无标题" : hpim.getTitle();
                String level = hpim.getUrgency();
                String number = hpim.getDocNumber();
                String completer = StringUtils.isBlank(hpim.getUserComplete()) ? "无" : hpim.getUserComplete();
                mapTemp.put("itemId", hpim.getItemId());
                mapTemp.put("itemName", hpim.getItemName());
                mapTemp.put(SysVariables.PROCESSSERIALNUMBER, processSerialNumber);
                mapTemp.put("title", documentTitle);
                mapTemp.put("processDefinitionId", processDefinitionId);
                mapTemp.put("processDefinitionKey", hpim.getProcessDefinitionId());
                mapTemp.put("startTime", startTime);
                mapTemp.put("endTime", endTime);
                mapTemp.put("taskDefinitionKey", "");
                mapTemp.put("user4Complete", completer);
                mapTemp.put("itemId", itemId);
                mapTemp.put("level", level);
                mapTemp.put("number", number);
                mapTemp.put("chaosongNum", 0);
                mapTemp.put("processInstanceId", processInstanceId);
                int countFollow = officeFollowManager.countByProcessInstanceId(tenantId, userId, processInstanceId);
                mapTemp.put("follow", countFollow > 0 ? true : false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mapTemp.put("serialNumber", serialNumber + 1);
            serialNumber += 1;
            items.add(mapTemp);
        }
        return Y9Page.success(page, Integer.parseInt(retMap.get("totalpages").toString()), Integer.parseInt(retMap.get("total").toString()), items, "获取列表成功");
    }

    /**
     * 当并行的时候，会获取到多个task，为了并行时当前办理人显示多人，而不是显示多条记录，需要分开分别进行处理
     *
     * @return
     */
    private List<String> getAssigneeIdsAndAssigneeNames(List<TaskModel> taskList) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        String taskIds = "", assigneeIds = "", assigneeNames = "";
        List<String> list = new ArrayList<String>();
        int i = 0;
        if (taskList.size() > 0) {
            for (TaskModel task : taskList) {
                if (StringUtils.isEmpty(taskIds)) {
                    taskIds = task.getId();
                    String assignee = task.getAssignee();
                    if (StringUtils.isNotBlank(assignee)) {
                        assigneeIds = assignee;
                        Position personTemp = positionManager.getPosition(tenantId, assignee).getData();
                        if (personTemp != null) {
                            assigneeNames = personTemp.getName();
                            i += 1;
                        }
                    } else {// 处理单实例未签收的当前办理人显示
                        List<IdentityLinkModel> iList = identityManager.getIdentityLinksForTask(tenantId, task.getId());
                        if (!iList.isEmpty()) {
                            int j = 0;
                            for (IdentityLinkModel identityLink : iList) {
                                String assigneeId = identityLink.getUserId();
                                Position ownerUser = positionManager.getPosition(Y9LoginUserHolder.getTenantId(), assigneeId).getData();
                                if (j < 5) {
                                    assigneeNames = Y9Util.genCustomStr(assigneeNames, ownerUser.getName(), "、");
                                    assigneeIds = Y9Util.genCustomStr(assigneeIds, assigneeId, SysVariables.COMMA);
                                } else {
                                    assigneeNames = assigneeNames + "等，共" + iList.size() + "人";
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
                            assigneeIds = Y9Util.genCustomStr(assigneeIds, task.getAssignee(), SysVariables.COMMA);// 并行时，领导选取时存在顺序，因此这里也存在顺序
                            Position personTemp = positionManager.getPosition(tenantId, assignee).getData();
                            if (personTemp != null) {
                                assigneeNames = Y9Util.genCustomStr(assigneeNames, personTemp.getName(), "、");// 并行时，领导选取时存在顺序，因此这里也存在顺序
                                i += 1;
                            }
                        }
                    }
                }
            }
            if (taskList.size() > 5) {
                assigneeNames += "等，共" + taskList.size() + "人";
            }
        } else {
            /*
             * List<HistoricActivityInstance> historicActivityInstanceList =
             * historyService.createHistoricActivityInstanceQuery().
             * processInstanceId(processInstanceId).activityType(SysVariables.
             * CALLACTIVITY).list(); if (historicActivityInstanceList != null &&
             * historicActivityInstanceList.size() > 0) { Map<String, Object> record =
             * setTodoElement(historicActivityInstanceList.get(0), existAssigneeId,
             * existAssigneeName); items.add(record); }
             */
        }
        list.add(taskIds);
        list.add(assigneeIds);
        list.add(assigneeNames);
        return list;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Y9Page<Map<String, Object>> homeDoingList(Integer page, Integer rows) {
        Map<String, Object> retMap = new HashMap<String, Object>(16);
        try {
            List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String positionId = Y9LoginUserHolder.getPositionId(), tenantId = Y9LoginUserHolder.getTenantId();
            retMap = doingManager.getListByUserId(tenantId, positionId, page, rows);
            List<ProcessInstanceModel> list = (List<ProcessInstanceModel>)retMap.get("rows");
            ObjectMapper objectMapper = new ObjectMapper();
            List<ProcessInstanceModel> hpiModelList = objectMapper.convertValue(list, new TypeReference<List<ProcessInstanceModel>>() {});
            int serialNumber = (page - 1) * rows;
            Map<String, Object> mapTemp = null;
            ProcessParamModel processParam = null;
            for (ProcessInstanceModel hpim : hpiModelList) {// 以办理时间排序
                mapTemp = new HashMap<String, Object>(16);
                try {
                    String processInstanceId = hpim.getId();
                    String processDefinitionId = hpim.getProcessDefinitionId();
                    String taskCreateTime = sdf.format(hpim.getStartTime());
                    List<TaskModel> taskList = taskManager.findByProcessInstanceId(tenantId, processInstanceId);
                    List<String> listTemp = getAssigneeIdsAndAssigneeNames(taskList);
                    String taskIds = listTemp.get(0), assigneeIds = listTemp.get(1), assigneeNames = listTemp.get(2);
                    Boolean isReminder = String.valueOf(taskList.get(0).getPriority()).contains("5");
                    processParam = processParamManager.findByProcessInstanceId(tenantId, processInstanceId);
                    String processSerialNumber = processParam.getProcessSerialNumber();
                    String documentTitle = StringUtils.isBlank(processParam.getTitle()) ? "无标题" : processParam.getTitle();
                    String level = processParam.getCustomLevel();
                    String number = processParam.getCustomNumber();
                    mapTemp.put("itemId", processParam.getItemId());
                    mapTemp.put("itemName", processParam.getItemName());
                    mapTemp.put("processDefinitionKey", hpim.getProcessDefinitionKey());
                    mapTemp.put(SysVariables.PROCESSSERIALNUMBER, processSerialNumber);
                    mapTemp.put("processDefinitionId", processDefinitionId);
                    mapTemp.put("title", documentTitle);
                    mapTemp.put("taskDefinitionKey", taskList.get(0).getTaskDefinitionKey());
                    mapTemp.put("taskName", taskList.get(0).getName());
                    mapTemp.put("taskCreateTime", taskCreateTime);
                    mapTemp.put("taskId", taskIds);
                    mapTemp.put("taskAssigneeId", assigneeIds);
                    mapTemp.put("taskAssignee", assigneeNames);
                    mapTemp.put(SysVariables.LEVEL, level);
                    mapTemp.put(SysVariables.NUMBER, number);
                    mapTemp.put("isReminder", isReminder);
                    String url = flowableBaseUrl + "/index/edit?itemId=" + processParam.getItemId() + "&processSerialNumber=" + processSerialNumber + "&itembox=doing&taskId=" + taskIds + "&processInstanceId=" + processInstanceId + "&listType=doing&systemName=" + processParam.getSystemName();
                    mapTemp.put("url", url);
                    mapTemp.put("chaosongNum", 0);
                    mapTemp.put("status", 1);
                    mapTemp.put("taskDueDate", "");
                    mapTemp.put("processInstanceId", processInstanceId);
                    mapTemp.put("speakInfoNum", 0);
                    mapTemp.put("remindSetting", false);
                    mapTemp.put("follow", false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mapTemp.put("serialNumber", serialNumber + 1);
                serialNumber += 1;
                items.add(mapTemp);
            }
            return Y9Page.success(page, Integer.parseInt(retMap.get("totalpages").toString()), Integer.parseInt(retMap.get("total").toString()), items, "获取列表成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Y9Page.success(page, 0, 0, new ArrayList<Map<String, Object>>(), "获取列表失败");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Y9Page<Map<String, Object>> todoList(String itemId, String searchTerm, Integer page, Integer rows) {
        Map<String, Object> retMap = new HashMap<String, Object>(16);
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String tenantId = Y9LoginUserHolder.getTenantId(), positionId = Y9LoginUserHolder.getPositionId();
            ItemModel item = itemManager.getByItemId(tenantId, itemId);
            if (StringUtils.isBlank(searchTerm)) {
                retMap = todoManager.getListByUserIdAndSystemName(tenantId, positionId, item.getSystemName(), page, rows);
            } else {
                retMap = todoManager.searchListByUserIdAndSystemName(tenantId, positionId, item.getSystemName(), searchTerm, page, rows);
            }
            List<TaskModel> list = (List<TaskModel>)retMap.get("rows");
            ObjectMapper objectMapper = new ObjectMapper();
            List<TaskModel> taslList = objectMapper.convertValue(list, new TypeReference<List<TaskModel>>() {});
            List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
            int serialNumber = (page - 1) * rows;
            Map<String, Object> vars = null;
            Collection<String> keys = null;
            Map<String, Object> mapTemp = null;
            ProcessParamModel processParam = null;
            for (TaskModel task : taslList) {
                mapTemp = new HashMap<String, Object>(16);
                String taskId = task.getId();
                String processInstanceId = task.getProcessInstanceId();
                String processDefinitionId = task.getProcessDefinitionId();
                try {
                    Date taskCreateTime = task.getCreateTime();
                    String taskAssignee = task.getAssignee();
                    String description = task.getDescription();
                    String taskDefinitionKey = task.getTaskDefinitionKey();
                    String taskName = task.getName();
                    int priority = task.getPriority();
                    keys = new ArrayList<String>();
                    keys.add(SysVariables.TASKSENDER);
                    vars = variableManager.getVariablesByProcessInstanceId(tenantId, processInstanceId, keys);
                    String taskSender = Strings.nullToEmpty((String)vars.get(SysVariables.TASKSENDER));
                    int isNewTodo = StringUtils.isBlank(task.getFormKey()) ? 1 : Integer.parseInt(task.getFormKey());
                    Boolean isReminder = String.valueOf(priority).contains("8");// 催办的时候任务的优先级+5
                    processParam = processParamManager.findByProcessInstanceId(tenantId, processInstanceId);
                    String processSerialNumber = processParam.getProcessSerialNumber();
                    String level = processParam.getCustomLevel();
                    String number = processParam.getCustomNumber();
                    mapTemp.put("itemId", processParam.getItemId());
                    mapTemp.put("itemName", processParam.getItemName());
                    mapTemp.put("processDefinitionKey", task.getProcessDefinitionId().split(":")[0]);
                    mapTemp.put(SysVariables.PROCESSSERIALNUMBER, processSerialNumber);
                    mapTemp.put("title", processParam.getTitle());
                    mapTemp.put("processDefinitionId", processDefinitionId);
                    mapTemp.put("taskId", taskId);
                    mapTemp.put("description", description);
                    mapTemp.put("taskDefinitionKey", taskDefinitionKey);
                    mapTemp.put("taskName", taskName);
                    mapTemp.put("taskCreateTime", sdf.format(taskCreateTime));
                    mapTemp.put("taskAssignee", taskAssignee);
                    mapTemp.put(SysVariables.TASKSENDER, taskSender);
                    mapTemp.put(SysVariables.ISNEWTODO, isNewTodo);
                    mapTemp.put(SysVariables.ISREMINDER, isReminder);
                    mapTemp.put(SysVariables.NUMBER, number);
                    String multiInstance = processDefinitionManager.getNodeType(tenantId, task.getProcessDefinitionId(), task.getTaskDefinitionKey());
                    mapTemp.put("isZhuBan", "");
                    if (multiInstance.equals(SysVariables.PARALLEL)) {
                        mapTemp.put("isZhuBan", "false");
                        String sponsorGuid = processParam.getSponsorGuid();
                        if (StringUtils.isNotBlank(sponsorGuid)) {
                            if (task.getAssignee().equals(sponsorGuid)) {
                                mapTemp.put("isZhuBan", "true");
                            }
                        }
                        String obj = variableManager.getVariableByProcessInstanceId(tenantId, task.getExecutionId(), SysVariables.NROFACTIVEINSTANCES);
                        Integer nrOfActiveInstances = obj != null ? Integer.valueOf(obj) : 0;
                        if (nrOfActiveInstances == 1) {
                            mapTemp.put("isZhuBan", "true");
                        }
                        if (StringUtils.isNotBlank(task.getOwner()) && !task.getOwner().equals(task.getAssignee())) {
                            mapTemp.put("isZhuBan", "");
                        }
                    }
                    mapTemp.put("isForwarding", false);
                    TaskVariableModel taskVariableModel = taskVariableManager.findByTaskIdAndKeyName(tenantId, taskId, "isForwarding");
                    if (taskVariableModel != null) {// 是否正在发送标识
                        mapTemp.put("isForwarding", taskVariableModel.getText().contains("true") ? true : false);
                    }
                    mapTemp.put(SysVariables.LEVEL, level);
                    mapTemp.put("processInstanceId", processInstanceId);
                    mapTemp.put("speakInfoNum", 0);
                    mapTemp.put("remindSetting", false);

                    int countFollow = officeFollowManager.countByProcessInstanceId(tenantId, positionId, processInstanceId);
                    mapTemp.put("follow", countFollow > 0 ? true : false);

                    String rollBack = variableManager.getVariableLocal(tenantId, taskId, SysVariables.ROLLBACK);
                    if (rollBack != null && Boolean.valueOf(rollBack)) {// 退回件
                        mapTemp.put("rollBack", true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mapTemp.put("serialNumber", serialNumber + 1);
                serialNumber += 1;
                items.add(mapTemp);
            }
            return Y9Page.success(page, Integer.parseInt(retMap.get("totalpages").toString()), Integer.parseInt(retMap.get("total").toString()), items, "获取列表成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Y9Page.success(page, 0, 0, new ArrayList<Map<String, Object>>(), "获取列表失败");
    }

}