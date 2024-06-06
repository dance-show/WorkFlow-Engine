package net.risesoft.controller;

import lombok.RequiredArgsConstructor;
import net.risesoft.entity.ItemInterfaceBind;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.ItemInterfaceBindService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 * @author zhangchongjie
 * @date 2024/05/24
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/vue/itemInterfaceBind")
public class ItemInterfaceBindController {

    private final ItemInterfaceBindService itemInterfaceBindService;

    /**
     * 获取绑定列表
     *
     * @param itemId 事项id
     * @return
     */
    @RequestMapping(value = "/getBindList", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<List<ItemInterfaceBind>> getBindList(@RequestParam(required = true) String itemId) {
        List<ItemInterfaceBind> list = itemInterfaceBindService.findByItemId(itemId);
        return Y9Result.success(list, "获取成功");
    }

    /**
     * 移除绑定
     *
     * @param id 绑定id
     * @return
     */
    @RequestMapping(value = "/removeBind", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> removeBind(@RequestParam String id) {
        itemInterfaceBindService.removeBind(id);
        return Y9Result.successMsg("删除成功");
    }

    /**
     * 保存绑定
     *
     * @param interfaceIds 接口id
     * @param itemId 事项id
     * @return
     */
    @RequestMapping(value = "/saveBind", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> saveBind(@RequestParam(required = true) String[] interfaceIds, @RequestParam(required = true) String itemId) {
        itemInterfaceBindService.saveBind(itemId, interfaceIds);
        return Y9Result.successMsg("保存成功");
    }
}