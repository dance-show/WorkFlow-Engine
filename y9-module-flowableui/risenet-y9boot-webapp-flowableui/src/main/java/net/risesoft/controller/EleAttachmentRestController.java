package net.risesoft.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.risesoft.api.itemadmin.AttachmentApi;
import net.risesoft.api.itemadmin.EleAttachmentApi;
import net.risesoft.enums.BrowserTypeEnum;
import net.risesoft.model.itemadmin.AttachmentModel;
import net.risesoft.model.itemadmin.EleAttachmentModel;
import net.risesoft.model.user.UserInfo;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.util.Y9FileUtil;
import net.risesoft.y9public.entity.Y9FileStore;
import net.risesoft.y9public.service.Y9FileStoreService;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 附件
 *
 * @author zhangchongjie
 * @date 2024/06/05
 */
@Validated
@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping(value = "/vue/eleAttachment", produces = MediaType.APPLICATION_JSON_VALUE)
public class EleAttachmentRestController {

    private final Y9FileStoreService y9FileStoreService;

    private final EleAttachmentApi eleAttachmentApi;

    /**
     * 附件下载
     *
     * @param id 附件id
     */
    @GetMapping(value = "/download")
    public void download(@RequestParam @NotBlank String id, HttpServletResponse response, HttpServletRequest request) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        try {
            EleAttachmentModel model = eleAttachmentApi.findById(tenantId, id).getData();
            String filename = model.getName();
            String filePath = model.getFileStoreId();
            if (request.getHeader("User-Agent").toLowerCase().indexOf("firefox") > 0) {
                filename = new String(filename.getBytes(StandardCharsets.UTF_8), "ISO8859-1");// 火狐浏览器
            } else {
                filename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
            }
            OutputStream out = response.getOutputStream();
            response.reset();
            response.setHeader("Content-disposition", "attachment; filename=\"" + filename + "\"");
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            response.setContentType("application/octet-stream");
            y9FileStoreService.downloadFileToOutputStream(filePath, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            LOGGER.error("附件下载失败", e);
        }
    }

    /**
     * 删除附件
     *
     * @param ids 附件ids，逗号隔开
     * @return Y9Result<String>
     */
    @PostMapping(value = "/delFile")
    public Y9Result<String> delFile(@RequestParam @NotBlank String ids) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        eleAttachmentApi.delFile(tenantId, ids);
        return Y9Result.successMsg("删除成功");
    }

    /**
     * 获取附件列表
     *
     * @param processSerialNumber 流程编号
     * @param attachmentType      附件类型
     * @return Y9Result<List < EleAttachmentModel>>
     */
    @GetMapping(value = "/list")
    public Y9Result<List<EleAttachmentModel>> list(@RequestParam @NotBlank String processSerialNumber, @RequestParam String attachmentType) {
        return eleAttachmentApi.findByProcessSerialNumberAndAttachmentType(Y9LoginUserHolder.getTenantId(), processSerialNumber, attachmentType);
    }

    /**
     * 附加打包zip下载
     *
     * @param processSerialNumber 流程编号
     * @param attachmentType      附件类型
     */
    @GetMapping(value = "/packDownload")
    public void packDownload(@RequestParam @NotBlank String processSerialNumber, @RequestParam @NotBlank String attachmentType, HttpServletResponse response, HttpServletRequest request) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        try {
            List<EleAttachmentModel> list = eleAttachmentApi.findByProcessSerialNumberAndAttachmentType(tenantId, processSerialNumber, attachmentType).getData();
            // 拼接zip文件,之后下载下来的压缩文件的名字
            String base_name = "附件" + new Date().getTime();
            String fileZip = base_name + ".zip";
            String packDownloadPath = Y9Context.getWebRootRealPath() + "static" + File.separator + "packDownload";
            File folder = new File(packDownloadPath);
            if (!folder.exists() && !folder.isDirectory()) {
                folder.mkdirs();
            }
            String zipPath = packDownloadPath + File.separator + fileZip;
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(zipPath));
            ZipOutputStream zos = new ZipOutputStream(bos);
            ZipEntry ze;
            for (EleAttachmentModel eleAttachmentModel : list) {
                String filename = eleAttachmentModel.getName();
                String fileStoreId = eleAttachmentModel.getFileStoreId();
                byte[] fileByte = y9FileStoreService.downloadFileToBytes(fileStoreId);
                InputStream bis = new ByteArrayInputStream(fileByte);
                ze = new ZipEntry(filename);
                zos.putNextEntry(ze);
                int s;
                while ((s = bis.read()) != -1) {
                    zos.write(s);
                }
                bis.close();
            }
            zos.flush();
            zos.close();
            boolean b = request.getHeader("User-Agent").toLowerCase().indexOf(BrowserTypeEnum.FIREFOX.getValue()) > 0;
            if (b) {
                fileZip = new String(fileZip.getBytes(StandardCharsets.UTF_8), "ISO8859-1");
            } else {
                fileZip = URLEncoder.encode(fileZip, StandardCharsets.UTF_8);
            }
            OutputStream out = response.getOutputStream();
            response.reset();
            response.setHeader("Content-disposition", "attachment; filename=\"" + fileZip + "\"");
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            response.setContentType("application/octet-stream");
            // 浏览器下载临时文件的路径
            DataInputStream in = new DataInputStream(new FileInputStream(zipPath));
            byte[] byte1 = new byte[2048];
            // 之后用来删除临时压缩文件
            File reportZip = new File(zipPath);
            try {
                while ((in.read(byte1)) != -1) {
                    out.write(byte1);
                }
                out.flush();
            } catch (Exception e) {
                LOGGER.error("下载失败", e);
            } finally {
                if (out != null) {
                    out.close();
                }
                in.close();
                // 删除服务器本地产生的临时压缩文件
                reportZip.delete();
            }
        } catch (Exception e) {
            LOGGER.error("下载失败", e);
        }
    }

    /**
     * 上传附件
     *
     * @param file                文件
     * @param processSerialNumber 流程编号
     * @param attachmentType      文件来源
     * @return Y9Result<String>
     */
    @PostMapping(value = "/upload")
    public Y9Result<Object> upload(MultipartFile file, @RequestParam @NotBlank String processSerialNumber, @RequestParam @NotBlank String miJi, @RequestParam @NotBlank String attachmentType) {
        UserInfo person = Y9LoginUserHolder.getUserInfo();
        String tenantId = Y9LoginUserHolder.getTenantId();
        try {
            String originalFilename = file.getOriginalFilename();
            String fileName = FilenameUtils.getName(originalFilename);
            String fullPath = "/" + Y9Context.getSystemName() + "/" + tenantId + "/attachmentFile" + "/" + processSerialNumber;
            Y9FileStore y9FileStore = y9FileStoreService.uploadFile(file, fullPath, fileName);
            String storeId = y9FileStore.getId();
            EleAttachmentModel eleAttachmentModel = new EleAttachmentModel();
            eleAttachmentModel.setAttachmentType(attachmentType);
            eleAttachmentModel.setName(originalFilename);
            eleAttachmentModel.setMiJi(miJi);
            eleAttachmentModel.setProcessSerialNumber(processSerialNumber);
            eleAttachmentModel.setFileStoreId(storeId);
            eleAttachmentModel.setUploadTime(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
            eleAttachmentModel.setPersonId(person.getPersonId());
            eleAttachmentModel.setPersonName(person.getName());
            return eleAttachmentApi.saveOrUpdate(tenantId, eleAttachmentModel);
        } catch (Exception e) {
            LOGGER.error("上传失败", e);
        }
        return Y9Result.failure("上传失败");
    }

}