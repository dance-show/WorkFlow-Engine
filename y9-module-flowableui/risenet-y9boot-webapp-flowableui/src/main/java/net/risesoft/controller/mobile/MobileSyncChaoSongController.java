package net.risesoft.controller.mobile;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import net.risesoft.api.itemadmin.ProcessParamApi;
import net.risesoft.model.itemadmin.ProcessParamModel;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.json.Y9JsonUtil;
import net.risesoft.y9.util.Y9Util;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2023/01/03
 */
@Deprecated
@RestController
@RequestMapping("/mobile/syncChaosong")
@Slf4j
public class MobileSyncChaoSongController {

    @Resource(name = "jdbcTemplate4Tenant")
    private JdbcTemplate jdbcTemplate;

    @Resource(name = "jdbcTemplate4Public")
    private JdbcTemplate jdbcTemplate4Public;

    @Autowired
    private ProcessParamApi processParamManager;

    /**
     *
     * @param request
     * @param response
     */
    @ResponseBody
    @RequestMapping(value = "/deleteChaosong")
    public void deleteChaosong(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> resMap = new HashMap<String, Object>(16);
        List<String> list0 = jdbcTemplate4Public.queryForList("select id from y9_common_tenant", String.class);
        for (String tenantId : list0) {
            try {
                Y9LoginUserHolder.setTenantId(tenantId);
                String sql = "delete from FF_CHAOSONG where STATUS = 1";
                jdbcTemplate.execute(sql);
                LOGGER.info("********************同步成功{}***************************", tenantId);
            } catch (Exception e) {
                LOGGER.warn("********************同步失败{}***************************", tenantId, e);
            }
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(resMap));
    }

    /**
     *
     * @param tenantId
     * @param request
     * @param response
     */
    @ResponseBody
    @RequestMapping(value = "/tongbubianliang")
    public void tongbubianliang(String tenantId, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> resMap = new HashMap<String, Object>(16);
        List<String> list0 = jdbcTemplate.queryForList("SELECT PROC_INST_ID_ from act_hi_procinst", String.class);
        for (String processInstanceId : list0) {
            try {
                Y9LoginUserHolder.setTenantId(tenantId);
                ProcessParamModel processParamModel =
                    processParamManager.findByProcessInstanceId(tenantId, processInstanceId);
                String searchTerm = processParamModel.getTitle() + "|" + processParamModel.getCustomNumber() + "|"
                    + processParamModel.getCustomLevel() + "|" + processParamModel.getItemName();
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String sql = "SELECT count(ID_) from act_hi_varinst where PROC_INST_ID_ = '" + processInstanceId
                    + "' and NAME_ = 'searchTerm'";

                int num = jdbcTemplate.queryForObject(sql, Integer.class);

                sql = "INSERT INTO act_hi_varinst (" + "	ID_," + "	REV_," + "	PROC_INST_ID_," + "	EXECUTION_ID_,"
                    + "	TASK_ID_," + "	NAME_," + "	VAR_TYPE_," + "	SCOPE_ID_," + "	SUB_SCOPE_ID_," + "	SCOPE_TYPE_,"
                    + "	BYTEARRAY_ID_," + "	DOUBLE_," + "	LONG_," + "	TEXT_," + "	TEXT2_," + "	CREATE_TIME_,"
                    + "	LAST_UPDATED_TIME_ " + " )" + " VALUES" + "	(" + "		'" + processInstanceId + "',"
                    + "		0," + "		'" + processInstanceId + "'," + "		'" + processInstanceId + "',"
                    + "		NULL," + "		'searchTerm'," + "		'string'," + "		NULL," + "		NULL,"
                    + "		NULL," + "		NULL," + "		NULL," + "		NULL," + "		'" + searchTerm + "',"
                    + "		NULL," + "		'" + sdf.format(date) + "'," + "		'" + sdf.format(date) + "'" + "	)";
                if (num == 0) {
                    jdbcTemplate.execute(sql);
                }

                sql = "SELECT count(ID_) from act_ru_variable where PROC_INST_ID_ = '" + processInstanceId
                    + "' and NAME_ = 'searchTerm'";

                num = jdbcTemplate.queryForObject(sql, Integer.class);

                sql = "INSERT INTO act_ru_variable (" + "	ID_," + "	REV_," + "	TYPE_," + "	NAME_,"
                    + "	EXECUTION_ID_," + "	PROC_INST_ID_," + "	TASK_ID_," + "	SCOPE_ID_," + "	SUB_SCOPE_ID_,"
                    + "	SCOPE_TYPE_," + "	BYTEARRAY_ID_," + "	DOUBLE_," + "	LONG_," + "	TEXT_," + "	TEXT2_ " + " )"
                    + " VALUES" + "	(" + "		'" + processInstanceId + "'," + "		1," + "		'string',"
                    + "		'searchTerm'," + "		'" + processInstanceId + "'," + "		'" + processInstanceId
                    + "'," + "		NULL," + "		NULL," + "		NULL," + "		NULL," + "		NULL,"
                    + "		NULL," + "		NULL," + "		'" + searchTerm + "'," + "		NULL " + "	)";
                if (num == 0) {
                    jdbcTemplate.execute(sql);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(resMap));
    }

    /**
     * 同步
     *
     * @param request
     * @param response
     */
    @ResponseBody
    @RequestMapping(value = "/tongbuChaosong")
    public void tongbuChaosong(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> resMap = new HashMap<String, Object>(16);
        List<String> list0 = jdbcTemplate4Public.queryForList("select id from y9_common_tenant", String.class);
        for (String tenantId : list0) {
            try {
                Y9LoginUserHolder.setTenantId(tenantId);
                String sql = "SELECT * from FF_CHAOSONG where STATUS = 1";
                List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
                int i = 0;
                for (Map<String, Object> map : list) {
                    try {
                        String year0 = ((String)map.get("CREATETIME")).substring(0, 4);
                        String opinionState = map.get("opinionState") == null ? "" : map.get("opinionState").toString();
                        String opinionContent =
                            map.get("opinionContent") == null ? "" : map.get("opinionContent").toString();
                        String opinionGroup = map.get("opinionGroup") == null ? "" : map.get("opinionGroup").toString();
                        String processSerialNumber =
                            map.get("PROCESSSERIALNUMBER") == null ? "" : map.get("PROCESSSERIALNUMBER").toString();
                        String taskId = map.get("TASKID") == null ? "" : map.get("TASKID").toString();
                        sql = "INSERT INTO FF_CHAOSONG_" + year0 + " (" + "	ID," + "	CREATETIME," + "	ITEMID,"
                            + "	ITEMNAME," + "	PROCESSINSTANCEID," + "	READTIME," + "	SENDDEPTID,"
                            + "	SENDDEPTNAME," + "	SENDERID," + "	SENDERNAME," + "	STATUS," + "	SYSTEMNAME,"
                            + "	TASKID," + "	TENANTID," + "	TITLE," + "	USERID," + "	USERNAME," + "	USERDEPTID,"
                            + "	USERDEPTNAME," + "	opinionState," + "	opinionContent," + "	opinionGroup,"
                            + "	PROCESSSERIALNUMBER" + " )" + " VALUES" + "	(" + "		'" + map.get("ID") + "',"
                            + "		'" + map.get("CREATETIME") + "'," + "		'" + map.get("ITEMID") + "',"
                            + "		'" + map.get("ITEMNAME") + "'," + "		'" + map.get("PROCESSINSTANCEID") + "',"
                            + "		'" + map.get("READTIME") + "'," + "		'" + map.get("SENDDEPTID") + "',"
                            + "		'" + map.get("SENDDEPTNAME") + "'," + "		'" + map.get("SENDERID") + "',"
                            + "		'" + map.get("SENDERNAME") + "'," + "		'1'," + "		'"
                            + map.get("SYSTEMNAME") + "'," + "		'" + taskId + "'," + "		'" + map.get("TENANTID")
                            + "'," + "		'" + map.get("TITLE") + "'," + "		'" + map.get("USERID") + "',"
                            + "		'" + map.get("USERNAME") + "'," + "		'" + map.get("USERDEPTID") + "',"
                            + "		'" + map.get("USERDEPTNAME") + "'," + "		'" + opinionState + "'," + "		'"
                            + opinionContent + "'," + "		'" + opinionGroup + "'," + "		'" + processSerialNumber
                            + "'" + "	)";
                        jdbcTemplate.execute(sql);
                    } catch (Exception e) {
                        i += 1;
                        e.printStackTrace();
                    }
                }
                LOGGER.info("********************同步成功{},{}条数据***************************", tenantId, list.size() - i);
                LOGGER.info("********************同步失败{}条数据***************************", i);
            } catch (Exception e) {
                LOGGER.warn("********************同步失败{}***************************", tenantId, e);
            }
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(resMap));
    }

}