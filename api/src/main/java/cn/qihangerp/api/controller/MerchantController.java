package cn.qihangerp.api.controller;

import cn.qihangerp.common.*;
import cn.qihangerp.common.enums.EnumUserType;
import cn.qihangerp.common.sys.ISysUserService;
import cn.qihangerp.common.sys.SysUser;
import cn.qihangerp.model.bo.MerchantAddBo;
import cn.qihangerp.model.entity.ErpMerchant;
import cn.qihangerp.model.entity.ErpWarehouse;
import cn.qihangerp.model.entity.OShop;
import cn.qihangerp.model.query.MerchantQuery;
import cn.qihangerp.security.LoginUser;
import cn.qihangerp.security.common.BaseController;
import cn.qihangerp.security.common.SecurityUtils;
import cn.qihangerp.service.ErpMerchantService;
import cn.qihangerp.service.ErpWarehouseMerchantService;
import cn.qihangerp.service.ErpWarehouseService;
import cn.qihangerp.service.OShopService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/merchant")
public class MerchantController extends BaseController {
    private final ErpMerchantService merchantService;
    private final ErpWarehouseService warehouseService;
    private final ISysUserService userService;
    private final OShopService shopService;
    private final ErpWarehouseMerchantService warehouseMerchantService;


    @GetMapping("/list")
    public TableDataInfo list(MerchantQuery bo, PageQuery pageQuery)
    {
        Integer userIdentity = SecurityUtils.getUserIdentity();
        if(userIdentity == null||userIdentity==0){
            // 管理员
//            PageResult<ErpMerchant> pageResult = merchantService.queryPageList(bo, pageQuery);
            var list = merchantService.list(new LambdaQueryWrapper<ErpMerchant>().eq(ErpMerchant::getStatus,0));

            return getDataTable(list);
        }else if(userIdentity == EnumUserType.MERCHANT.getIndex()){
            //商户
//            LoginUser loginUser = SecurityUtils.getLoginUser();
            ErpMerchant merchant = merchantService.getById(SecurityUtils.getDeptId());
            List<ErpMerchant> list = new ArrayList<>();
            list.add(merchant);
            return getDataTable(list);
        }else if(userIdentity == EnumUserType.STORE.getIndex()) {
            // 店铺
            OShop shop = shopService.getById(SecurityUtils.getDeptId());
            if(shop!=null){
                ErpMerchant merchant = merchantService.getById(shop.getMerchantId());
                List<ErpMerchant> list = new ArrayList<>();
                list.add(merchant);
                return getDataTable(list);
            }else{
                return getDataTable(new ArrayList<>());
            }
        } else{
            return getDataTable(new ArrayList<>());
        }


    }

    @GetMapping("/pageList")
    public TableDataInfo pageList(MerchantQuery bo, PageQuery pageQuery)
    {
        Integer userIdentity = SecurityUtils.getUserIdentity();
        Long merchantId = 0l;
        if(userIdentity == null||userIdentity==0){
            // 管理员
            PageResult<ErpMerchant> pageResult = merchantService.queryPageList(bo, pageQuery);
            ErpMerchant self = new ErpMerchant();
            self.setId(0L);
            self.setName("总部自营");
            if(pageResult.getRecords()!=null){
                if(pageResult.getRecords().size()==0){
                    pageResult.setRecords(new ArrayList<ErpMerchant>());
                }
                pageResult.getRecords().add(0,self);

            }else{
                pageResult.setRecords(new ArrayList<ErpMerchant>());
                pageResult.getRecords().add(0,self);
            }

            return getDataTable(pageResult);
        }else if(userIdentity==20){
            //商户
            LoginUser loginUser = SecurityUtils.getLoginUser();
            ErpMerchant self = new ErpMerchant();
            self.setId(SecurityUtils.getDeptId());
            self.setName("商户自营");
            List<ErpMerchant> list = new ArrayList<>();
            list.add(self);
            return getDataTable(list);
        }else{
            merchantId = -1L;
            return getDataTable(new ArrayList<>());
        }


    }

    /**
     * 全部商户（仅总部查询）
     * @return
     */
    @GetMapping("/list_all")
    public AjaxResult listAll()
    {
        Integer userIdentity = SecurityUtils.getUserIdentity();
        Long merchantId = 0l;
        if(userIdentity==0){
            var list = merchantService.list(new LambdaQueryWrapper<ErpMerchant>().eq(ErpMerchant::getStatus,0));
            long count = list.stream().filter(x -> x.getId() == 0).count();
            if(count == 0) {
                ErpMerchant self = new ErpMerchant();
                self.setId(0L);
                self.setName("总部自营");
                list.add(0, self);
            }
            return AjaxResult.success(list);
        }else {
            return AjaxResult.success();
        }
    }


    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(merchantService.getById(id));
    }


    @PostMapping("/add")
    public AjaxResult add(@RequestBody MerchantAddBo bo)
    {
        Integer userIdentity = SecurityUtils.getUserIdentity();
        if(userIdentity == null||userIdentity!=0){
            return AjaxResult.error("没有权限添加商户");
        }
//        if(StringUtils.isEmpty(bo.getLoginName())) return AjaxResult.error("请填写登录账号");
//        if(StringUtils.isEmpty(bo.getLoginPwd())) return AjaxResult.error("请填写登录密码号");

        if(StringUtils.hasText(bo.getLoginName())) {
            ErpMerchant merchant = merchantService.selectUserByUserName(bo.getLoginName());
            if (merchant != null) return AjaxResult.error("登录账号已存在，请重新输入");
            String pwd = SecurityUtils.encryptPassword(bo.getLoginPwd());
            bo.setLoginPwd(pwd);

            SysUser user = new SysUser();
            user.setUserName(bo.getLoginName());
            user.setPhonenumber(bo.getMobile());
            if (!userService.checkUserNameUnique(user)) {
                return error("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
            } else if (cn.qihangerp.common.utils.StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
                return error("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
            } else if (cn.qihangerp.common.utils.StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
                return error("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
            }
        }else{
            bo.setLoginName("");
            bo.setLoginPwd("");
        }
        ResultVo<ErpMerchant> resultVo = merchantService.add(bo, getUsername());
        if(resultVo.getCode()==0 && StringUtils.hasText(bo.getLoginName())){
            SysUser user = new SysUser();
            user.setUserName(bo.getLoginName());
            user.setPhonenumber(bo.getMobile());
            user.setCreateBy(getUsername());
            user.setRoleId(0L);
            user.setDeptId(resultVo.getData().getId());
            user.setCreateBy("添加商户");
            user.setRemark("商户："+bo.getName());
            user.setCreateTime(new Date());
            String pwd = SecurityUtils.encryptPassword(bo.getLoginPwd());
            user.setPassword(pwd);
            user.setNickName(bo.getName());
            user.setUserType("20");
            userService.insertUser(user);
        }

        return AjaxResult.success();
    }

    @PutMapping("/edit")
    public AjaxResult edit(@RequestBody ErpMerchant bo)
    {
        if(bo.getId()==null) return AjaxResult.error("缺少参数：id");

        Integer userIdentity = SecurityUtils.getUserIdentity();
        Long merchantId = 0L;
        if(userIdentity == null||userIdentity!=0){
            return AjaxResult.error("没有权限修改商户");
        }
        bo.setUpdateBy(getUsername());
        bo.setUpdateTime(new Date());
        merchantService.updateById(bo);
        return toAjax(1);
    }

    @DeleteMapping("/del/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        if(ids == null) return AjaxResult.error("缺少参数：id");
        boolean containsOne = Arrays.asList(ids).contains(1L);
        if(containsOne){
            return AjaxResult.error("不能删除自营商户数据");
        }

        Integer userIdentity = SecurityUtils.getUserIdentity();
        if(userIdentity == null||userIdentity!=0){
            return AjaxResult.error("没有权限删除商户");
        }
        merchantService.removeBatchByIds(Arrays.stream(ids).toList());
        return AjaxResult.success();
    }

    /**
     * 获取商户账号列表
     * @param id
     * @return
     */
    @GetMapping(value = "/getLoginAccount/{id}")
    public AjaxResult getLoginAccount(@PathVariable("id") Long id) {
        SysUser sysUser = new SysUser();
        sysUser.setUserType(EnumUserType.MERCHANT.getIndex()+"");
        sysUser.setDeptId(id);
        List<SysUser> sysUsers = userService.selectUserList(sysUser);
        return AjaxResult.success(sysUsers);
    }


    @PostMapping("/setLoginName")
    public AjaxResult setLoginName(@RequestBody MerchantAddBo bo)
    {
        Integer userIdentity = SecurityUtils.getUserIdentity();
        if(userIdentity == null||userIdentity!=0){
            return AjaxResult.error("没有权限修改商户");
        }
        if(bo.getId()==null) return AjaxResult.error("缺少参数：id");
        if(StringUtils.isEmpty(bo.getLoginName())) return AjaxResult.error("请填写登录账号");
        if(StringUtils.isEmpty(bo.getLoginPwd())) return AjaxResult.error("请填写登录密码号");

        // 查询用户名是否存在
        SysUser sysUser = userService.selectUserByUserName(bo.getLoginName());
        if(sysUser!=null && (sysUser.getDeptId()==null||sysUser.getDeptId()!=bo.getId())){
            return AjaxResult.error("用户名已存在");
        }
        String pwd = SecurityUtils.encryptPassword(bo.getLoginPwd());
        bo.setLoginPwd(pwd);
        ResultVo<ErpMerchant> resultVo = merchantService.setLoginName(bo.getId(), bo, getUsername());
        if(resultVo.getCode()==0) {

            if(sysUser==null) {
                // 新增sysuser
                SysUser userNew = new SysUser();
                userNew.setUserName(bo.getLoginName());
                userNew.setPhonenumber(resultVo.getData().getMobile());
                userNew.setCreateBy(getUsername());
                userNew.setRoleId(0L);
                userNew.setDeptId(resultVo.getData().getId());
                userNew.setCreateBy("添加商户");
                userNew.setRemark("商户："+resultVo.getData().getName());
                userNew.setCreateTime(new Date());
                userNew.setPassword(pwd);
                userNew.setNickName(resultVo.getData().getName());
                userNew.setUserType(EnumUserType.MERCHANT.getIndex()+"");
                userService.insertUser(userNew);
            }else{
                // 修改密码
                userService.resetUserPwd(bo.getLoginName(),pwd);
            }
            return AjaxResult.success();
        }
        else
            return AjaxResult.error(resultVo.getMsg());
    }
}
