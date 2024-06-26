package net.risesoft.service;

import java.util.List;
import java.util.Map;

import net.risesoft.model.itemadmin.FieldPermModel;
import net.risesoft.model.itemadmin.FormFieldDefineModel;
import net.risesoft.model.itemadmin.Y9FormFieldModel;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/22
 */
public interface FormDataService {

    /**
     * 删除子表数据
     *
     * @param formId
     * @param tableId
     * @param guid
     * @return
     */
    Map<String, Object> delChildTableRow(String formId, String tableId, String guid);

    /**
     * 获取表单所有字段权限
     *
     * @param formId
     * @param taskDefKey
     * @param processDefinitionId
     * @return
     */
    List<FieldPermModel> getAllFieldPerm(String formId, String taskDefKey, String processDefinitionId);

    /**
     * Description: 获取子表数据
     *
     * @param formId
     * @param tableId
     * @param processSerialNumber
     * @return
     * @throws Exception
     */
    List<Map<String, Object>> getChildTableData(String formId, String tableId, String processSerialNumber)
        throws Exception;

    /**
     * 根据事项id和流程序列号获取数据
     *
     * @param tenantId
     * @param itemId
     * @param processSerialNumber
     * @return
     */
    Map<String, Object> getData(String tenantId, String itemId, String processSerialNumber);

    /**
     * 获取字段权限
     *
     * @param formId
     * @param fieldName
     * @param taskDefKey
     * @param processDefinitionId
     * @return
     */
    FieldPermModel getFieldPerm(String formId, String fieldName, String taskDefKey, String processDefinitionId);

    /**
     * 根据表单id获取绑定字段信息
     *
     * @param itemId
     * @return
     */
    List<Y9FormFieldModel> getFormField(String itemId);

    /**
     * 根据表单id获取绑定字段信息
     *
     * @param formId
     * @return
     */
    List<FormFieldDefineModel> getFormFieldDefine(String formId);

    /**
     * 获取表单json数据
     *
     * @param formId
     * @return
     */
    String getFormJson(String formId);

    /**
     * 根据表单id获取表单数据
     *
     * @param formId
     * @param processSerialNumber
     * @return
     */
    Map<String, Object> getFromData(String formId, String processSerialNumber);

    /**
     * Description: 保存子表数据
     *
     * @param formId
     * @param tableId
     * @param processSerialNumber
     * @param jsonData
     * @throws Exception
     */
    void saveChildTableData(String formId, String tableId, String processSerialNumber, String jsonData)
        throws Exception;

    /**
     * 保存表单数据
     *
     * @param formdata
     * @param formId
     * @param actionType
     * @throws Exception
     */
    void saveFormData(String formdata, String formId) throws Exception;
}