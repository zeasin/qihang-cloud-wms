package cn.qihangerp.service;

import cn.qihangerp.model.entity.SysMenuWms;
import cn.qihangerp.model.vo.RouterVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 1
* @description 针对表【sys_menu_wms(菜单权限表)】的数据库操作Service
* @createDate 2025-12-02 12:18:57
*/
public interface SysMenuWmsService extends IService<SysMenuWms> {
    List<SysMenuWms> selectMenuTree();
    List<RouterVo> buildMenus(List<SysMenuWms> menus);
}
