package net.risesoft.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.api.itemadmin.SignDeptInfoApi;
import net.risesoft.model.itemadmin.SignDeptModel;
import net.risesoft.pojo.Y9Result;
import net.risesoft.y9.Y9LoginUserHolder;

/**
 * 会签信息
 *
 * @author zhangchongjie
 * @date 2024/06/05
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/vue/signDept", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class SignDept4GfgController {

    private final SignDeptInfoApi signDeptInfoApi;

    /**
     * 删除会签信息
     *
     * @param id 主键id
     * @return Y9Result<Object>
     */
    @PostMapping(value = "/deleteById")
    public Y9Result<Object> deleteById(@RequestParam String id) {
        return signDeptInfoApi.deleteById(Y9LoginUserHolder.getTenantId(), id);
    }

    /**
     * 获取会签信息
     *
     * @param processInstanceId 流程实例id
     * @param deptType 部门类型
     * @return Y9Result<List<SignDeptModel>>
     */
    @GetMapping(value = "/getSignDeptList")
    public Y9Result<List<SignDeptModel>> getSignDeptList(@RequestParam String processInstanceId,
        @RequestParam String deptType) {
        return signDeptInfoApi.getSignDeptList(Y9LoginUserHolder.getTenantId(), deptType, processInstanceId);
    }

    /**
     * 保存会签部门
     *
     * @param processInstanceId 流程实例id
     * @param deptType 部门类型
     * @param deptIds 部门id
     * @return Y9Result<Object>
     */
    @PostMapping(value = "/saveSignDept")
    public Y9Result<Object> saveSignDept(@RequestParam String processInstanceId, @RequestParam String deptType,
        @RequestParam String deptIds) {
        return signDeptInfoApi.saveSignDept(Y9LoginUserHolder.getTenantId(), Y9LoginUserHolder.getPositionId(), deptIds,
            deptType, processInstanceId);
    }

    /**
     * 保存会签签名
     *
     * @param id 主键id
     * @param userName 用户名
     * @return Y9Result<Object>
     */
    @PostMapping(value = "/saveSignDeptInfo")
    public Y9Result<Object> saveSignDeptInfo(@RequestParam String id, @RequestParam String userName) {
        return signDeptInfoApi.saveSignDeptInfo(Y9LoginUserHolder.getTenantId(), id, userName);
    }

}