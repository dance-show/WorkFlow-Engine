package net.risesoft.repository.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.ItemLinkRole;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public interface ItemLinkRoleRepository extends JpaRepository<ItemLinkRole, String>, JpaSpecificationExecutor<ItemLinkRole> {

    @Modifying
    @Transactional(readOnly = false)
    void deleteByItemLinkId(String id);

    List<ItemLinkRole> findByItemLinkId(String itemLinkId);

    ItemLinkRole findByItemLinkIdAndRoleId(String itemLinkId, String roleId);
}