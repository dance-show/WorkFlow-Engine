package net.risesoft.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.risesoft.api.itemadmin.SignaturePictureApi;
import net.risesoft.api.platform.org.PersonApi;
import net.risesoft.entity.SignaturePicture;
import net.risesoft.model.itemadmin.SignaturePictureModel;
import net.risesoft.model.platform.Person;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.SignaturePictureService;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.util.Y9BeanUtil;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/22
 */
@RestController
@RequestMapping(value = "/services/rest/signaturePicture", produces = MediaType.APPLICATION_JSON_VALUE)
public class SignaturePictureApiImpl implements SignaturePictureApi {

    @Autowired
    private SignaturePictureService signaturePictureService;

    @Autowired
    private PersonApi personManager;

    @Override
    public Y9Result<Object> deleteById(String tenantId, String id) {
        Y9LoginUserHolder.setTenantId(tenantId);
        signaturePictureService.deleteById(id);
        return Y9Result.success();
    }

    @Override
    public Y9Result<SignaturePictureModel> findById(String tenantId, String id) {
        Y9LoginUserHolder.setTenantId(tenantId);
        SignaturePicture sp = signaturePictureService.findById(id);
        SignaturePictureModel spModel = new SignaturePictureModel();
        if (null != sp) {
            Y9BeanUtil.copyProperties(sp, spModel);
        }
        return Y9Result.success(spModel);
    }

    @Override
    @GetMapping(value = "/findByUserId")
    public Y9Result<SignaturePictureModel> findByUserId(String tenantId, String userId) {
        Y9LoginUserHolder.setTenantId(tenantId);

        SignaturePictureModel spModel = new SignaturePictureModel();
        SignaturePicture sp = signaturePictureService.findByUserId(userId);
        if (null != sp) {
            Y9BeanUtil.copyProperties(sp, spModel);
        }
        return Y9Result.success(spModel);
    }

    @Override
    public Y9Result<SignaturePictureModel> saveOrUpdate(String tenantId, String userId, String spJson) {
        Person person = personManager.get(tenantId, userId).getData();
        Y9LoginUserHolder.setTenantId(tenantId);
        Y9LoginUserHolder.setPerson(person);

        ObjectMapper om = new ObjectMapper();
        try {
            JsonNode jn = om.readTree(spJson);
            om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            SignaturePictureModel spModel = new SignaturePictureModel();
            SignaturePicture sp = om.readValue(jn.toString(), SignaturePicture.class);
            SignaturePicture newsp = signaturePictureService.saveOrUpdate(sp);
            if (null != newsp) {
                Y9BeanUtil.copyProperties(newsp, spModel);
            }
            return Y9Result.success(spModel);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Y9Result.failure("保存或更新签名图片失败");
        }
    }
}