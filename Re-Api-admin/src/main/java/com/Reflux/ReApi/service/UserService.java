package com.Reflux.ReApi.service;

import com.Reflux.ReApi.model.entity.User;
import com.Reflux.ReApi.model.vo.LoginUserVO;
import com.Reflux.ReApi.model.vo.UserVO;
import com.Reflux.ReApi.utils.JWTRespond;
import com.Reflux.clientSdk.client.ReApiClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.Reflux.ReApi.model.request.user.UserQueryRequest;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;

/**
 * 用户服务
 *
 * @author Reflux
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户登录（微信开放平台）
     *
     * @param wxOAuth2UserInfo 从微信获取的用户信息
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLoginByMpOpen(WxOAuth2UserInfo wxOAuth2UserInfo, HttpServletRequest request);

    /**
     * 从session中获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUserBySession(HttpServletRequest request);
    /**
     * 从ThreadLocal中获取当前登录用户
     *
     * @return
     */
    LoginUserVO getLoginUserByThreadLocal();

    /**
     * 获取当前登录用户Session（允许未登录）
     *
     * @param request
     * @return
     */
    User getLoginUserBySessionPermitNull(HttpServletRequest request);
    /**
     * 获取当前登录用户ThreadLocal（允许未登录）
     *
     * @return
     */
    LoginUserVO getLoginUserByThreadLocalPermitNull();


    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(LoginUserVO user);


    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    boolean updateSecretKey(Long id);

    /**
     * 给登录用户生成JWT令牌
     * @param loginUserVO
     * @return
     */
    JWTRespond getJWT(LoginUserVO loginUserVO, HttpServletRequest request);
    /**
     * 创建SDK客户端
     *
     * @param request 当前会话
     * @return SDK客户端
     */
    ReApiClient getReApiClient(HttpServletRequest request);
}
