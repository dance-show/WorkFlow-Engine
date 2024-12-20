package net.risesoft.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.controller.vo.NodeTreeVO;
import net.risesoft.entity.DynamicRole;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.DynamicRoleService;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/vue/dynamicRole", produces = MediaType.APPLICATION_JSON_VALUE)
public class DynamicRoleRestController {

    private final DynamicRoleService dynamicRoleService;

    /**
     * 获取动态角色列表
     *
     * @return Y9Result<List<DynamicRole>>
     */
    @GetMapping(value = "/dynamicRoleList")
    public Y9Result<List<DynamicRole>> dynamicRoleList() {
        List<DynamicRole> drList = dynamicRoleService.listAll();
        return Y9Result.success(drList, "获取成功");
    }

    /**
     * 获取动态角色
     *
     * @param id 角色id
     * @return Y9Result<DynamicRole>
     */
    @GetMapping(value = "/getDynamicRole")
    public Y9Result<DynamicRole> getDynamicRole(@RequestParam String id) {
        DynamicRole dynamicRole = dynamicRoleService.getById(id);
        return Y9Result.success(dynamicRole, "获取成功");
    }

    /**
     * 获取动态角色树
     *
     * @return Y9Result<List<NodeTreeVO>>
     */
    @GetMapping(value = "/list")
    public Y9Result<List<NodeTreeVO>> list() {
        List<NodeTreeVO> listMap = new ArrayList<>();
        List<DynamicRole> dynamicRoleList = dynamicRoleService.listAll();
        for (DynamicRole dynamicRole : dynamicRoleList) {
            NodeTreeVO map = new NodeTreeVO();
            map.setId(dynamicRole.getId());
            map.setName(dynamicRole.getName());
            map.setIsParent(false);
            map.setOrgType("dynamicRole");
            listMap.add(map);
        }
        return Y9Result.success(listMap, "获取成功");
    }

    /**
     * 删除
     *
     * @param dynamicRoleIds 角色ids
     * @return Y9Result<String>
     */
    @PostMapping(value = "/remove")
    public Y9Result<String> remove(@RequestParam String[] dynamicRoleIds) {
        dynamicRoleService.removeDynamicRoles(dynamicRoleIds);
        return Y9Result.successMsg("删除成功");
    }

    /**
     * 保存动态角色
     *
     * @param dynamicRole 动态角色
     * @return Y9Result<String>
     */
    @PostMapping(value = "/saveOrUpdate")
    public Y9Result<String> saveOrUpdate(DynamicRole dynamicRole) {
        dynamicRoleService.saveOrUpdate(dynamicRole);
        return Y9Result.successMsg("保存成功");
    }
}
