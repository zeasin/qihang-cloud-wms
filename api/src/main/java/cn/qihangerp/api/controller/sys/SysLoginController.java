package cn.qihangerp.api.controller.sys;

import cn.qihangerp.common.PasswordValidator;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.common.ResultVoEnum;
import cn.qihangerp.common.config.RedisCache;
import cn.qihangerp.common.sys.ISysUserService;
import cn.qihangerp.common.sys.SysUser;
import cn.qihangerp.common.vo.UserVo;
import cn.qihangerp.model.entity.SysMenuMms;
import cn.qihangerp.model.entity.SysMenuWms;
import cn.qihangerp.model.request.LoginBody;
import cn.qihangerp.model.entity.SysMenu;
import cn.qihangerp.model.vo.RouterVo;
import cn.qihangerp.service.ISysMenuService;
import cn.qihangerp.service.SysMenuMmsService;
import cn.qihangerp.service.SysMenuWmsService;
import cn.qihangerp.service.SysPermissionService;
import cn.qihangerp.security.common.Constants;
import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.security.common.SecurityUtils;
import cn.qihangerp.security.service.SysLoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * 登录验证
 * 
 * @author qihang
 */
@Slf4j
@RestController
public class SysLoginController
{
    @Autowired
    private SysLoginService loginService;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private ISysMenuService menuService;
    @Autowired
    private SysMenuMmsService mmsMenuService;
    @Autowired
    private SysMenuWmsService wmsMenuService;
    @Autowired
    private SysPermissionService permissionService;
    @Autowired
    private ISysUserService userService;
    /**
     * 登录方法
     * 
     * @param loginBody 登录信息
     * @return 结果
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody loginBody) {
        SysUser sysUser = userService.selectUserByUserName(loginBody.getUsername());
        if (sysUser != null) {
            log.info("==========登录用户{}={}=====", sysUser.getUserName(), sysUser.getUserType());
            if (sysUser.getStatus().equals("1")) {
                return AjaxResult.error("用户已锁定，请联系管理员解锁!");
            }
            var b = PasswordValidator.validatePassword(loginBody.getUsername(), loginBody.getPassword());
            b = ResultVo.success();
            if (b.getCode() == ResultVoEnum.SUCCESS.getIndex()) {
                // 验证密码
                boolean b1 = SecurityUtils.matchesPassword(loginBody.getPassword(), sysUser.getPassword());
//                String pwd = SecurityUtils.encryptPassword(loginBody.getPassword());
                if(!b1){
                    return AjaxResult.error(500,"密码不正确");
                }
                try {
                    AjaxResult ajax = AjaxResult.success();
                    // 生成令牌
                    String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
                            loginBody.getUuid());
                    ajax.put(Constants.TOKEN, token);
                    redisCache.deleteObject(loginBody.getUsername());
                    return ajax;
                } catch (cn.qihangerp.security.UserPasswordNotMatchException s) {
                    return AjaxResult.error(500, "密码不正确");
                } catch (Exception e) {
                    return AjaxResult.error(100, e.getMessage());
                }
            } else {
                return AjaxResult.error(500, b.getMsg());
            }
        } else {
            return AjaxResult.error("用户不存在");
        }
    }
//    @PostMapping("/login")
//    public AjaxResult login(@RequestBody LoginBody loginBody)
//    {
//        try {
//            AjaxResult ajax = AjaxResult.success();
//            // 生成令牌
//            String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
//                    loginBody.getUuid());
//            Map<String,Object> map = new HashMap<String,Object>();
//            map.put("accessToken",token);
//            map.put("expiresIn",7200);
//            map.put("refreshToken",token);
//            map.put("tokenType","Bearer");
//            ajax.put("data",map);
//            ajax.put(Constants.TOKEN, token);
//            return ajax;
//        }catch (Exception e){
//            return AjaxResult.error(e.getMessage());
//        }
//    }

    /**
     * 获取用户信息
     * 
     * @return 用户信息
     */
//    @GetMapping("getInfo")
//    public AjaxResult getInfo()
//    {
//        SysUser user = SecurityUtils.getLoginUser().getUser();
//        // 角色集合
//        Set<String> roles = permissionService.getRolePermission(user);
//        // 权限集合
//        Set<String> permissions = permissionService.getMenuPermission(user);
//        AjaxResult ajax = AjaxResult.success();
//        ajax.put("user", user);
//        ajax.put("roles", roles);
//        ajax.put("permissions", permissions);
//        return ajax;
//    }
    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("getInfo")
    public AjaxResult getInfo() {
//        if(SecurityUtils.getLoginUser().getUser().getUserIdentity()!=0) return AjaxResult.error(401,"身份不匹配");
        UserVo user = SecurityUtils.getLoginUser().getUser();
        log.info("========登录用户：{}==身份：{}=====", user.getUserName(), user.getUserIdentity());
        SysUser sysUser = userService.selectUserById(user.getUserId());
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(sysUser);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(sysUser);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("user", user);
        ajax.put("roles", roles);
        ajax.put("permissions", permissions);
        return ajax;
    }
    /**
     * 获取路由信息
     * 
     * @return 路由信息
     */
    @GetMapping("getRouters")
    public AjaxResult getRouters() {
        try {
            Long userId = SecurityUtils.getUserId();
            SysUser sysUser = userService.selectUserById(userId);
            log.info("==========登录用户{}={}=====", sysUser.getUserName(), sysUser.getUserType());
            if (sysUser.getUserType().equals("00")) {
                List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
                return AjaxResult.success(menuService.buildMenus(menus));
            } else if (sysUser.getUserType().equals("20")) {
                // 商户（商户独立菜单）
                List<SysMenuMms> menus = mmsMenuService.selectMenuAll();
                List<RouterVo> routerVos = mmsMenuService.buildMenus(menus);
                return AjaxResult.success(routerVos);
            } else if (sysUser.getUserType().equals("10")) {
                // 云仓库（独立菜单）
                List<SysMenuWms> menus = wmsMenuService.selectMenuTree();
                List<RouterVo> routerVos = wmsMenuService.buildMenus(menus);
                return AjaxResult.success(routerVos);
            } else {
                return AjaxResult.error(500,"登录身份未知");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error(500,e.getMessage());
        }
    }
//    @GetMapping("getRouters")
//    public AjaxResult getRouters()
//    {
//        Long userId = SecurityUtils.getUserId();
//        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
//        return AjaxResult.success(menuService.buildMenus(menus));
//    }
}
