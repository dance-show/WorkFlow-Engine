package net.risesoft.repository.jpa;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.SignDeptInfo;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public interface SignDeptInfoRepository
        extends JpaRepository<SignDeptInfo, String>, JpaSpecificationExecutor<SignDeptInfo> {

    @Modifying
    @Transactional
    void deleteByProcessInstanceIdAndDeptType(String processInstanceId, String deptType);

    @Modifying
    @Transactional
    void deleteByProcessInstanceIdAndDeptTypeAndDeptIdNotIn(String processInstanceId, String deptType,
                                                            List<String> deptIds);

    SignDeptInfo findByProcessInstanceIdAndDeptTypeAndDeptId(String processInstanceId, String deptType, String deptId);

    List<SignDeptInfo> findByProcessInstanceIdAndDeptTypeOrderByOrderIndexAsc(String processInstanceId,
                                                                              String deptType);
}