package com.Reflux.ReApi.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.Reflux.ReApi.common.ErrorCode;
import com.Reflux.ReApi.config.GatewayConfig;
import com.Reflux.ReApi.constant.CommonConstant;
import com.Reflux.ReApi.constant.UserConstant;
import com.Reflux.ReApi.exception.BusinessException;
import com.Reflux.ReApi.exception.ThrowUtils;
import com.Reflux.ReApi.mapper.UserMapper;
import com.Reflux.ReApi.model.entity.User;
import com.Reflux.ReApi.model.enums.UserRoleEnum;
import com.Reflux.ReApi.model.request.user.UserQueryRequest;
import com.Reflux.ReApi.model.vo.LoginUserVO;
import com.Reflux.ReApi.model.vo.UserVO;
import com.Reflux.ReApi.service.UserService;
import com.Reflux.ReApi.utils.JWTRespond;
import com.Reflux.ReApi.utils.JwtUtils;
import com.Reflux.ReApi.utils.SqlUtils;
import com.Reflux.ReApi.utils.UserHolder;
import com.Reflux.clientSdk.client.ReApiClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.Reflux.ReApi.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现
 *
 * @author Aaron
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /*@Resource
    private StringRedisTemplate stringRedisTemplate;*/
    @Resource
    private GatewayConfig gatewayConfig;
    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "reflux";
    private static final ExecutorService HandleUserSecretKey_EXECUTOR = Executors.newSingleThreadExecutor();

    // todo 获取sdk
    @Override
    public ReApiClient getReApiClient(HttpServletRequest request) {
        // 获取当前登录用户
        //User loginUser = userService.getLoginUserBySession(request);
        LoginUserVO loginUserVO = this.getLoginUserByThreadLocal();
        // 从数据库查完整信息
        User loginUser = this.getById(loginUserVO.getId());
        String accessKey = loginUser.getAccessKey();
        String secretKey = loginUser.getSecretKey();
        //log.info("admin生成ReApiClient，secretKey为：{}",secretKey);
        ReApiClient reApiClient = new ReApiClient(accessKey, secretKey);
        // 设置网关地址，使用配置类，直接注入新网关地址，避免魔法值，方便上线
        reApiClient.setGateway_Host(gatewayConfig.getHost());
        return reApiClient;
    }
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 分配accessKey，secretKey
            String accessKey= DigestUtils.md5DigestAsHex((SALT+userAccount+RandomUtil.randomNumbers(5)).getBytes());
            String secretKey= DigestUtils.md5DigestAsHex((SALT+userAccount+RandomUtil.randomNumbers(8)).getBytes());

            // 4. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setUserName("游客");

            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密密码，md5不可逆
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        // bing说：如果前端请求没有携带session，那么request.getSession()方法将会创建一个新的HttpSession对象。
        // 因此，request.getSession().setAttribute(USER_LOGIN_STATE, user)不会报错。
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    @Override
    public LoginUserVO userLoginByMpOpen(WxOAuth2UserInfo wxOAuth2UserInfo, HttpServletRequest request) {
        String unionId = wxOAuth2UserInfo.getUnionId();
        String mpOpenId = wxOAuth2UserInfo.getOpenid();
        // 单机锁
        synchronized (unionId.intern()) {
            // 查询用户是否已存在
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("unionId", unionId);
            User user = this.getOne(queryWrapper);
            // 被封号，禁止登录
            if (user != null && UserRoleEnum.BAN.getValue().equals(user.getUserRole())) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "该用户已被封，禁止登录");
            }
            // 用户不存在则创建
            if (user == null) {
                user = new User();
                user.setUnionId(unionId);
                user.setMpOpenId(mpOpenId);
                user.setUserAvatar(wxOAuth2UserInfo.getHeadImgUrl());
                user.setUserName(wxOAuth2UserInfo.getNickname());
                boolean result = this.save(user);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败");
                }
            }
            // 记录用户的登录态
            request.getSession().setAttribute(USER_LOGIN_STATE, user);
            return getLoginUserVO(user);
        }
    }

    /**
     * 从session中获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUserBySession(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }
    @Override
    public LoginUserVO getLoginUserByThreadLocal() {
        LoginUserVO user = UserHolder.getUser();
        ThrowUtils.throwIf(user==null,ErrorCode.NOT_LOGIN_ERROR);
        return user;
    }



    /**
     * 获取当前登录用户用session（允许未登录）
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUserBySessionPermitNull(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        return this.getById(userId);
    }
    /**
     * 获取当前登录用户用threadLocal（允许未登录）
     *
     * @return
     */
    @Override
    public LoginUserVO getLoginUserByThreadLocalPermitNull() {
        // 先判断是否已登录
        LoginUserVO currentUser = UserHolder.getUser();
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        return currentUser;
    }



    @Override
    public boolean isAdmin(LoginUserVO user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            // 由于我们使用了自定义请求头，导致之前的跨域处理都无效了，
            // 所以此处前端可能没携带session，session为空抛异常
            // throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
            ThrowUtils.throwIf(UserHolder.getUser()==null,ErrorCode.NOT_LOGIN_ERROR);
        }
        // 移除登录态
        // 下面好像不需要动
        // bing说：因为如果getSession()方法返回的HttpSession对象中没有名为USER_LOGIN_STATE的属性，
        // 那么removeAttribute()方法将不会执行任何操作。
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * user脱敏
     * @param user
     * @return
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String unionId = userQueryRequest.getUnionId();
        String mpOpenId = userQueryRequest.getMpOpenId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
        queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public boolean updateSecretKey(Long id) {
        User user = this.getById(id);
        String accessKey = DigestUtil.md5Hex(SALT + user.getUserAccount() + RandomUtil.randomNumbers(5));
        String secretKey = DigestUtil.md5Hex(SALT + user.getUserAccount() + RandomUtil.randomNumbers(8));
        user.setSecretKey(secretKey);
        user.setAccessKey(accessKey);
        return this.updateById(user);
    }

    @Override
    public JWTRespond getJWT(LoginUserVO loginUserVO, HttpServletRequest request) {
        JWTRespond jwtRespond = new JWTRespond();
        if(loginUserVO !=null ){
            //自定义信息
            Map<String , Object> claims = new HashMap<>();
            claims.put(UserConstant.ID, loginUserVO.getId());
            claims.put(UserConstant.USERNAME, loginUserVO.getUserName());
            claims.put(UserConstant.USERROLE, loginUserVO.getUserRole());
            claims.put(UserConstant.USERAVATAR, loginUserVO.getUserAvatar());
            //使用JWT工具类，生成身份令牌
            String token = JwtUtils.generateJwt(claims);
            jwtRespond.setToken(token);
            HandleUserSecretKey_EXECUTOR.submit(() -> {
                try {
                    // 1.查询用户AccessKey和SecretKey，生成客户端
                    // ReApiClient reApiClient = this.getReApiClient(request);
                    // 2.使用SecretKey生成签名sign
                    // 额，客户端竟然没暴露生成签名的接口，还挺小心
                    // reApiClient.
                    // 所以对于已登录用户直接拿账号密码签名吧......
                    // 3.以AccessKey为key，sign为value，存入redis
                    // 4.设置有效期用户登出删除key
                    log.info("JWT_token:{}",token);
                }catch (Exception e){
                    //打印失败信息然后啥也不用干
                    e.printStackTrace();
                }
            });
        }
        jwtRespond.setLoginUserData(loginUserVO);
        return jwtRespond;
    }
}
