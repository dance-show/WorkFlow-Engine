package net.risesoft.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.risesoft.entity.Y9PreFormItemBind;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
public interface Y9PreFormItemBindRepository extends JpaRepository<Y9PreFormItemBind, String>, JpaSpecificationExecutor<Y9PreFormItemBind> {

    public Y9PreFormItemBind findByItemId(String itemId);

}