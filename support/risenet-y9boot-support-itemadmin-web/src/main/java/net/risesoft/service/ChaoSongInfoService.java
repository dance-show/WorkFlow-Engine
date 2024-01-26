package net.risesoft.service;

import java.util.List;
import java.util.Map;

import net.risesoft.nosql.elastic.entity.ChaoSongInfo;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/22
 */
public interface ChaoSongInfoService {

    /**
     * 改变抄送件意见状态
     *
     * @param id
     * @param type
     */
    void changeChaoSongState(String id, String type);

    /**
     * 设置已阅
     *
     * @param id
     */
    void changeStatus(String id);

    /**
     * 批量已阅
     *
     * @param ids
     */
    void changeStatus(String[] ids);

    /**
     * Description: 根据流程实例id统计除当前人外是否有抄送件
     * 
     * @param userId
     * @param processInstanceId
     * @return
     */
    int countByProcessInstanceId(String userId, String processInstanceId);

    /**
     * Description: 根据流程实例id统计当前人是否有抄送件
     * 
     * @param userId
     * @param processInstanceId
     * @return
     */
    int countByUserIdAndProcessInstanceId(String userId, String processInstanceId);

    /**
     * 收回抄送件
     *
     * @param ids
     */
    void deleteById(String ids);

    /**
     * 批量收回抄送件
     *
     * @param ids
     */
    void deleteByIds(String[] ids);

    /**
     * 根据流程实例id删除抄送件
     *
     * @param processInstanceId
     * @return
     */
    boolean deleteByProcessInstanceId(String processInstanceId);

    /**
     * Description: 查看抄送件详情
     * 
     * @param processInstanceId
     * @param status
     * @param mobile
     * @return
     */
    Map<String, Object> detail(String processInstanceId, Integer status, boolean mobile);

    /**
     * 根据id查找抄送件
     *
     * @param id
     * @return
     */
    ChaoSongInfo findOne(String id);

    /**
     * 获取个人抄送件计数
     *
     * @param userId
     * @return
     */
    int getAllCountByUserId(String userId);

    /**
     * 获取抄送所有件
     *
     * @param userId
     * @param documentTitle
     * @param rows
     * @param page
     * @return
     */
    Map<String, Object> getAllListByUserId(String userId, String documentTitle, int rows, int page);

    /**
     * 获取批阅件计数
     *
     * @param userId
     * @return
     */
    int getDone4OpinionCountByUserId(String userId);

    /**
     * 根据人员唯一标示查找已阅数量
     *
     * @param userId
     * @return
     */
    int getDoneCountByUserId(String userId);

    /**
     * 获取抄送已阅件
     *
     * @param userId
     * @param year
     * @param documentTitle
     * @param rows
     * @param page
     * @return
     */
    Map<String, Object> getDoneListByUserId(String userId, String documentTitle, int rows, int page);

    /**
     * Description: 根据流程实例获取除当前人外的其他抄送件
     * 
     * @param processInstanceId
     * @param userName
     * @param rows
     * @param page
     * @return
     */
    Map<String, Object> getListByProcessInstanceId(String processInstanceId, String userName, int rows, int page);

    /**
     * Description: 根据流程实例获取当前人的抄送件
     * 
     * @param senderId
     * @param processInstanceId
     * @param userName
     * @param rows
     * @param page
     * @return
     */
    Map<String, Object> getListBySenderIdAndProcessInstanceId(String senderId, String processInstanceId,
        String userName, int rows, int page);

    /**
     * 批阅件列表
     *
     * @param userId
     * @param documentTitle
     * @param rows
     * @param page
     * @return
     */
    Map<String, Object> getOpinionChaosongByUserId(String userId, String documentTitle, int rows, int page);

    /**
     * 根据人员唯一标示查找待阅数量
     *
     * @param userId
     * @return
     */
    int getTodoCountByUserId(String userId);

    /**
     * 获取抄送未阅件
     *
     * @param userId
     * @param documentTitle
     * @param rows
     * @param page
     * @return
     */
    Map<String, Object> getTodoListByUserId(String userId, String documentTitle, int rows, int page);

    /**
     * 保存抄送
     *
     * @param chaoSong
     * @return
     */
    ChaoSongInfo save(ChaoSongInfo chaoSong);

    /**
     * 批量保存抄送
     *
     * @param chaoSongList
     * @return
     */
    void save(List<ChaoSongInfo> chaoSongList);

    /**
     * Description: 根据选择的人员保存抄送
     * 
     * @param processInstanceId
     * @param users
     * @param isSendSms
     * @param isShuMing
     * @param smsContent
     * @param smsPersonId
     * @return
     */
    Map<String, Object> save(String processInstanceId, String users, String isSendSms, String isShuMing,
        String smsContent, String smsPersonId);

    /**
     * Description: 个人阅件搜索
     * 
     * @param searchName
     * @param itemId
     * @param userName
     * @param state
     * @param year
     * @param page
     * @param rows
     * @return
     */
    Map<String, Object> searchAllByUserId(String searchName, String itemId, String userName, String state, String year,
        Integer page, Integer rows);

    /**
     * 监控阅件列表
     *
     * @param searchName
     * @param itemId
     * @param senderName
     * @param userName
     * @param state
     * @param year
     * @param page
     * @param rows
     * @return
     */
    Map<String, Object> searchAllList(String searchName, String itemId, String senderName, String userName,
        String state, String year, Integer page, Integer rows);

    /**
     * 更新抄送件标题
     *
     * @param processInstanceId
     * @param documentTitle
     */
    void updateTitle(String processInstanceId, String documentTitle);
}