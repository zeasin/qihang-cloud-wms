package cn.qihangerp.api.controller.sys;

import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.model.entity.SysConfig;
import cn.qihangerp.security.common.SecurityUtils;
import cn.qihangerp.service.SysConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/system/config")
public class SysConfigController {
    private final SysConfigService configService;

    /**
     * 获取系统培训
     * @param key sys.name 系统名称
     * @return
     */
    @GetMapping(value = "getConfigValue/{key}")
    public AjaxResult getConfigValue(@PathVariable String key)
    {
        String suf = "";
        try {
            Integer userIdentity = SecurityUtils.getUserIdentity();
            if (userIdentity == null || userIdentity == 0) {
                // 总部
                suf = "-总部端";
            } else if (userIdentity == 20) {
                suf = "-商户端";
            } else if (userIdentity == 10) {
                suf = "-仓库端";
            } else if (userIdentity == 30) {
                suf = "-供应商端";
            }
        }catch (Exception e) {}
        List<SysConfig> list = configService.list(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        if(list!=null && !list.isEmpty())
        {
            list.get(0).setConfigValue( list.get(0).getConfigValue()+suf);
            return AjaxResult.success(list.get(0));
        }else{
            return AjaxResult.success();
        }
    }
}
