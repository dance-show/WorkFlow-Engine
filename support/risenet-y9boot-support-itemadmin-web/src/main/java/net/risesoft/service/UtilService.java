package net.risesoft.service;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/22
 */
public interface UtilService {

    /**
     * Description:
     * 
     * @param taskId
     * @param targetTaskDefineKey
     */
    void freeJump(String taskId, String targetTaskDefineKey);
}