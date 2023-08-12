# 前端

这部分，知识星球里有位大哥整理得非常好，所以我就不搞那么详细了，知道大概流程就行，具体细节看他笔记

## 1、初始化

初始化好前端项目后还需要`npm install` 和`yarn`安装相关依赖

### 1、项目瘦身：

执行 `yarn add eslint-config-prettier --dev yarn add eslint-plugin-unicorn --dev` 
然后修改`node_modules/@umijs/lint/dist/config/eslint/index.js`文件注释`// es2022: true`
以后执行`i18n-remove`然后手动删掉locales目录即可移除多国语言模块

### 2、自动生成对应接口：

![image-20230722143449670](API接口开放平台笔记.assets/image-20230722143449670.png)

![image-20230722143510484](API接口开放平台笔记.assets/image-20230722143510484.png)

#### 设置服务器URL以及开启保存token：

把服务器地址写上，并设置保存token，这个文件下可以只保留请求拦截器和响应拦截器，其他错误处理全删掉。

![image-20230722144608052](API接口开放平台笔记.assets/image-20230722144608052.png)

### 3、修改登录接口

![image-20230722143630186](API接口开放平台笔记.assets/image-20230722143630186.png)

修改登录逻辑，并使用定时器，保证页面渲染在点击登录之前完成：

![image-20230722144446603](API接口开放平台笔记.assets/image-20230722144446603.png)

在此文件中还需把LoginParams改成自己的登录请求

把表单里的用户名和密码的name属性的值改为后端对应字段

#### 识别：头像和用户名：

![image-20230722144750303](API接口开放平台笔记.assets/image-20230722144750303.png)

#### 设置用户注销：

在同一文件下改为调用生成登出函数，并编写登出逻辑为登出后返回自动登录页面，把原有的logout函数删掉。
或者把登出逻辑直接写在原有的logout函数里

![image-20230722144845240](API接口开放平台笔记.assets/image-20230722144845240.png)

### 4、全局状态：

#### 添加全局状态：

![image-20230722143905013](API接口开放平台笔记.assets/image-20230722143905013.png)

#### 保存全局状态：

找到app.tsx全局入口文件：

在页面首次加载时保存全局状态信息

![image-20230722144122825](API接口开放平台笔记.assets/image-20230722144122825.png)

#### 删掉多余代码：

![image-20230722144058564](API接口开放平台笔记.assets/image-20230722144058564.png)

### 5、登录权限修改：

把参数修改为我们保存的全局状态

![image-20230722145010552](API接口开放平台笔记.assets/image-20230722145010552.png)

## 2、增删改

### 修改接口调用：

修改下面三个函数的调用，和类型为openapi生成的接口和类型，并移入`TableList`模块内

![image-20230722145342062](API接口开放平台笔记.assets/image-20230722145342062.png)

### 填写表格信息：

![image-20230722145520581](API接口开放平台笔记.assets/image-20230722145520581.png)

### 表格操作调用：

表格的操作部分需要调用生成的增删改的函数（删除已经给好）：

![image-20230722145739732](API接口开放平台笔记.assets/image-20230722145739732.png)

### 增加模态框：

在最后的return函数里增加模态框，用于创建和更新时与用户交互：

![image-20230722145652081](API接口开放平台笔记.assets/image-20230722145652081.png)

![image-20230722145658450](API接口开放平台笔记.assets/image-20230722145658450.png)

### 编写模态框组件：

![image-20230722145917086](API接口开放平台笔记.assets/image-20230722145917086.png)

![image-20230722145930738](API接口开放平台笔记.assets/image-20230722145930738.png)

### 调用request与后端联调：

注意参数和返回值要和request的要求一致

![image-20230722150017008](API接口开放平台笔记.assets/image-20230722150017008.png)

该模块中可以自行删掉批量删除的函数，因为用不到。

到此增删改基本完成，可以自行摸索显示多个表格的操作。

# 后端：

## 1、上传文件

不想麻烦的朋友直接跳最后结果看结论。

### 需求1：

上传到阿里云OSS的文件目录需要根据业务用户来划分：

怎么从前端获取用户？

```java
@PostMapping("/upload") 
public BaseResponse<String> uploadFile(
    @RequestPart("file") MultipartFile multipartFile,
    UploadFileRequest uploadFileRequest, HttpServletRequest request) {}
```

#### 第一个办法：

从`HttpServletRequest`的`request`中取出对应的session，就可以获得用户了。

```java
User loginUser = userService.getLoginUser(request);
// 未登录
ThrowUtils.throwIf(useId==null,ErrorCode.NOT_LOGIN_ERROR);
// 文件目录：根据业务、用户来划分，最后以文件名结尾
String uuid = RandomStringUtils.randomAlphanumeric(8);
String filename = uuid + "-" + multipartFile.getOriginalFilename();
String filepath = String.format("%s/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId(), filename);
```

出现了问题，查看浏览器控制台，发现前端在请求头里没携带cookie！而响应头里却有cookie

第一个想法是前端携带session了吗？跨域了吗？

前端已经设置了：`withCredentials:true,`

后端也进行了跨域处理：

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 覆盖所有请求
        registry.addMapping("/**")
            // 允许发送 Cookie
            .allowCredentials(true)
            // 放行哪些域名（必须用 patterns，否则 * 会和 allowCredentials 冲突）
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .exposedHeaders("*");
    }
}
```

不清楚为什么没成功传递`session`。

可以看到已登录，但是只有响应标头有cookie，而请求标头没cookie

![image-20230726212211870](API接口开放平台笔记.assets/image-20230726212211870.png)

所以结论是：不止后端要处理跨域，前端也要处理跨域？这个想法很明显错误，因为：

要知道前后端都配置跨域，浏览器会报多个跨域头的问题，所以到底为什么跨域了阿？

PS：

当我解决了这个问题，回过头来思考时发现，应该不是跨域问题，其实就是单纯这个Upload组件的默认上传方法只会传默认的请求头，如果需要传session需要额外配置。

```typescript
<Upload
name="file"
listType="picture-circle"
showUploadList={false}
// 后端处理文件请求的接口
action="http://localhost:8101/api/file/upload"
// 不要写data={{UploadFileRequest}}，这意味着有很多个参数
// 而我们选择把参数全封入UploadFileRequest里了
data={UploadFileRequest}
beforeUpload={beforeUpload}
onChange={handleChange}
>
    {imageUrl ? (
     <img
     src={data?.userAvatar}
alt="avatar"
style={{ width: '100%', borderRadius: '10px' }}
/>
) : (
    uploadButton
)}
    </Upload>
```

#### 第二个办法：

让前端在`UploadFileRequest`中携带用户ID

```java
@PostMapping("/upload") 
public BaseResponse<String> uploadFile(
    @RequestPart("file") MultipartFile multipartFile,
    UploadFileRequest uploadFileRequest) {}
```

但是这样很危险，因为没有session验证身份，前端只要随便传个ID都能上传文件

```java
Long useId = uploadFileRequest.getUseId();//直接从uploadFileRequest拿useID
// 未登录
ThrowUtils.throwIf(useId==null,ErrorCode.NOT_LOGIN_ERROR);
// 文件目录：根据业务、用户来划分，最后以文件名结尾
String uuid = RandomStringUtils.randomAlphanumeric(8);
String filename = uuid + "-" + multipartFile.getOriginalFilename();
String filepath = String.format("%s/%s/%s", fileUploadBizEnum.getValue(), useId, filename);     
```

成功通过用户ID和业务进行划分云端目录并上传。

### 需求2：

完成头像上传后需要更新头像

#### 方法一：

后端上传成功后，前端再发送一次请求进行更新：

```typescript
// 更新用户头像
const updateUserAvatar = async (id: number, userAvatar: string) => {
    // 更新用户头像
    const res = await updateUserUsingPOST({
        id,
        userAvatar,
    });
    if (res.code !== 0) {
        message.success(`更新用户头像失败`);
    } else {
        getUserInfo(id);
    }
}; 
const handleChange: UploadProps['onChange'] =(info: UploadChangeParam<UploadFile>) => {
    if (info.file.status === 'uploading') {
        console.log("UploadFileRequest:",UploadFileRequest);
        setLoading(true);
        return;
    }
    if (info.file.status === 'done') {
        // const res = await uploadFileUsingPOST({
        //     file: info.file.originFileObj as any
        // })
        if (info.file.response.code === 0) {
            message.success(`上传成功`);
            const id = initialState?.loginUser?.id as number;
            // 设置新头像的url
            const userAvatar = info.file.response.data;
            setLoading(false);
            setImageUrl(userAvatar);
            //发送更新请求
            updateUserAvatar(id, userAvatar);
        }
    }
};
```

显然，这种做法有很大风险，如果前端发送更新请求失败，那么图片上传成功，用户头像却没改变，白白上传了垃圾文件到云端。

#### 方法二：

##### 使用事务：

后端根据前端业务类型Biz来判断是否需要更新数据库头像，使用事务注解保证操作操作成功

```java
//进行事务操作
@Transactional
public String uploadFileByBiz(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum, Long useId, String filepath) throws IOException {
    String file = aliOSSUtils.uploadFile2OSS(multipartFile, filepath);
    //如果是头像上传业务，那么要马上更新头像
    if(FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)){
        User user = userService.getById(useId);
        user.setUserAvatar(file);
        userService.updateById(user);
    }
    return file;
}
```

这里有问题，那就是已经上传的文件能回滚吗？？

答案是不能：因为@Transactional注解只能回滚数据库操作，而文件上传操作不是数据库操作。

##### 办法一：

可以采用消息队列加开子线程使用while循环不停监控消息队列，如果消息队列有消息，那么子线程进行更新。

不过杀鸡焉用牛刀。

##### 办法二：

而我们注意到：其实上传文件的URL不需要等上传成功才能拿到。

所以我们可以先进行数据库更新，再进行文件上传，如果文件上传失败，那么数据库是可以回滚的。

```java
//进行事务操作
@Transactional
public String uploadFileByBiz(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum, Long useId, String filepath) throws IOException {
    //如果是头像上传业务，那么要马上更新头像
    if(FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)){
        // 使用工具类提前拿到URL
        String url = aliOSSUtils.URL(filepath);
        User user = userService.getById(useId);
        user.setUserAvatar(url);
        userService.updateById(user);
    }
    return aliOSSUtils.uploadFile2OSS(multipartFile, filepath);
}
```

而前端发送上传图片请求成功后，加载用户新头像发送的不再是更新请求，而是查询请求：

别忘了更新全局状态：

```typescript
// 获取用户信息，更新全局状态
const getUserInfo = async (id: any) => {
    return getUserVOByIdUsingGET({ id }).then((res) => {
        if (res.data) {
            setInitialState((s: any) => ({ ...s, loginUser: res.data }));
            setData(res.data);
            setImageUrl(res.data.userAvatar);
        }
    });
};
```

### 需求3：

虽然解决了上传文件的问题，但是安全问题却还没解决，只要有用户的id，就能够给你的服务器上传文件！

#### 方法一：

查阅官方文档发现：

在上传文件时增加`headers`参数，来设置上传的请求头部，IE10 以上有效。那么校验身份也就迎刃而解了。

```typescript
<Upload
name="file"
listType="picture-circle"
showUploadList={false}
// 后端处理文件请求的接口
action="http://localhost:8101/api/file/upload"
data={UploadFileRequest}
headers	={？？？}			
beforeUpload={beforeUpload}
onChange={handleChange}
>
    {imageUrl ? (
     <img
     src={data?.userAvatar}
alt="avatar"
style={{ width: '100%', borderRadius: '10px' }}
/>
) : (
    uploadButton
)}
    </Upload>
```

#### 方法二：

上面的办法不能长长久久，所以我选择使用`JWT`令牌，`JWT`令牌可以跨域。

PS：做到这里的时候，我还是以为是因为前端直接调用接口，而不是调用封装的函数导致的跨域...

其实只是单纯的Upload组件的默认上传方法只会传默认的请求头。

##### 后端：

首先要写`JWT`的生成与解析函数

```java
    /**
         * 生成JWT令牌
         * @param claims JWT第二部分负载 payload 中存储的内容
         * @return
         */
        public static String generateJwt(Map<String, Object> claims){
            String jwt = Jwts.builder()
                    .addClaims(claims)//自定义信息（有效载荷）
                    .signWith(SignatureAlgorithm.HS256, signKey)//签名算法（头部）
                    .setExpiration(new Date(System.currentTimeMillis() + expire))//过期时间
                    .compact();
            return jwt;
        }

        /**
         * 解析JWT令牌
         * @param jwt JWT令牌
         * @return JWT第二部分负载 payload 中存储的内容
         */
        public static Claims parseJWT(String jwt){
            Claims claims = Jwts.parser()
                    .setSigningKey(signKey)//指定签名密钥
                    //指定令牌Token,把刚传解析之前写入的有效载荷
                    .parseClaimsJws(jwt)
                    .getBody();
            return claims;
        }
```

登录成功调用`JWT`生成器生成`token`返回给前端：

构建一个新的类，把需要返回给前端的`token`和`LoginUser`数据封装。这样就能做到不入侵之前的代码了

```java
@Data
public class JWTRespond {
    private String token;//设置token
    private Object LoginUserData;//把LoginUser传进data，就不用侵入源代码了
}
```

修改前的UserController：

```java
LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
return ResultUtils.success(loginUserVO);
```

修改后：

```java
LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
//
JWTRespond jwtRespond = new JWTRespond();
if(loginUserVO !=null ){
    //自定义信息
    Map<String , Object> claims = new HashMap<>();
    claims.put(UserConstant.ID, loginUserVO.getId());
    claims.put(UserConstant.USERNAME,loginUserVO.getUserName());
    claims.put(UserConstant.USERROLE,loginUserVO.getUserRole());
    claims.put(UserConstant.USERAVATAR,loginUserVO.getUserAvatar());
    //使用JWT工具类，生成身份令牌
    String token = JwtUtils.generateJwt(claims);
    jwtRespond.setToken(token);
    log.info("JWT_token:{}",token);
}
jwtRespond.setLoginUserData(loginUserVO);
return ResultUtils.success(jwtRespond);
```

后端需要增加拦截器，拦截除了登录请求以外的所有请求：

拦截器中要解析token，看看用户是否登录

`JWT`能解决分布式服务器的一大原因就是，它比对的是数据库的数据，而不是每台服务器各自存储的`session`。

并把解析出来的数据保存到ThreadLocal，方便后续取出当前登录用户。

```java
@Override
public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    System.out.println("preHandle .... ");
    //1.获取请求url
    //2.判断请求url中是否包含login，如果包含，说明是登录操作，放行，此处已在注册器里实现
    //todo 需要实现前端接收JWT的token，下面才能进行下去。
    //3.获取请求头中的令牌（token），下面这行解析需要和前端约定好
    String token = request.getHeader("Authorization").replace("Bearer ", "");
    //String token = request.getHeader("Authorization");
    log.info("从请求头中获取的令牌：{}", token);
    //4.判断令牌是否存在，如果不存在，返回错误结果（未登录）
    if (!StringUtils.hasLength(token)) {
        log.info("Token不存在");
        ResponseForFrontend(response);
        return false;//不放行
    }
    //5.解析token，如果解析失败，返回错误结果（未登录）
    try {
        // 已登录的用户，保存到UserHolder中
        Claims claims = JwtUtils.parseJWT(token);
        LoginUserVO loginUserVO=new LoginUserVO();
        loginUserVO.setId((Long) claims.get(UserConstant.ID));
        loginUserVO.setUserName((String) claims.get(UserConstant.USERNAME));
        loginUserVO.setUserRole((String) claims.get(UserConstant.USERROLE));
        loginUserVO.setUserAvatar((String) claims.get(UserConstant.USERAVATAR));
        UserHolder.saveUser(loginUserVO);
    } catch (Exception e) {
        log.info("令牌解析失败!");
        //创建响应结果对象
        ResponseForFrontend(response);
        return false;
    }
    //6.放行
    return true;
}
@Override
public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 									Object handler, Exception ex) throws Exception {
    UserHolder.removeUser();
}
```

`ThreadLocal`保存当前登录用户

```java
public class UserHolder {
    private static final ThreadLocal<LoginUserVO> tl = new ThreadLocal<>();

    public static void saveUser(LoginUserVO user){
        tl.set(user);
    }

    public static LoginUserVO getUser(){
        return tl.get();
    }

    public static void removeUser(){
        tl.remove();
    }
}
```

##### 前端：

到此后端改造完成，开始改造前端。

`JWT`能解决跨域请求的一大原因就是，它传的不是cookie或者session，而是自定义请求头

该自定义请求头的数据保存在浏览器的`localStorage`中，基本操作如下：

```java
sessionStorage //浏览器关闭即失效
localStorage //长期有效

sessionStorage.变量名 = 变量值   // 保存数据
sessionStorage.setItem("变量名","变量值") // 保存数据
sessionStorage.变量名  // 读取数据
sessionStorage.getItem("变量名") // 读取数据
sessionStorage.removeItem("变量名") // 清除单个数据
sessionStorage.clear()  // 清除所有sessionStorage保存的数据

localStorage.变量名 = 变量值   // 保存数据
localStorage.setItem("变量名","变量值") // 保存数据
localStorage.变量名  // 读取数据
localStorage.getItem("变量名") // 读取数据
localStorage.removeItem("变量名") // 清除单个数据
localStorage.clear()  // 清除所有sessionStorage保存的数据
```

在`src/app.tsx`文件下，使用拦截器，给每次请求添加上`token`：

```java
//配置拦截器，从localStorage中取出token并放入请求头的Authorization字段中
const authHeaderInterceptor = (url: string, options: RequestOptionsInit) => {
    let token = localStorage.getItem('token');
    if (null === token) {
        token = '';
    }
    const authHeader = {  Authorization: `Bearer ${token}` };
    return {
        url: `${url}`,
        options: { ...options, interceptors: true, headers: authHeader },
    };
};

export const request : RequestConfig= {
    ...errorConfig,
    // 新增自动添加AccessToken的请求前拦截器
    requestInterceptors: [authHeaderInterceptor],
};
```

然后前端还需在登录时保存后端响应的`token`，在登出时，删除对应的`token`。

还是得看官方文档，看官方文档一下就解决了，找博客结果一个能用的都没有

如果你跑不通我的代码，也应该去看官方文档！

参考文档：https://beta-pro.ant.design/docs/request-cn

##### 前后端联动思路：

1. 访问首页，前端首先会发送查询当前登录用户的请求，而不是直接发送登录请求（这好像是框架自带的逻辑）

2. 此时应该拦截该请求，查看有没有token，有，证明之前登录过，存UserHolder，放行，返回当前登录用户信息，实现自动登录

   没有，返回未登录

3. 然后用户正式登录，该请求不会被拦截，用户登陆后，前端会再次发出查询当前登录用户的请求，此时重复步骤2即可

##### 前后端联调：

小知识：

由于我们发送了一个自定义请求头，所以是复杂请求。

`cors`跨域复杂请求会先发送一个方法为`OPTIONS`的预检请求，这个请求是用来验证本次请求是否安全的，

因为复杂请求可能对服务器数据产生副作用。例如`delete`或者`put`，都会对服务器数据进行修改，所以在请求之前都要先询问服务器，当前网页所在域名是否在服务器的许可名单中，服务器允许后，浏览器才会发出正式的请求，否则不发送正式请求。

而过滤器会把预请求当做真正的请求去判断，预请求`OPTIONS`没携带我们的自定义请求头`AUTHORIZATION`，此时如果取`getHeader`就会有空指针异常，所以在过滤器判断token之前先判断是不是预请求`OPTIONS`。

如果是则应该放行，因为后台的拦截器若将option请求进行权限拦截，真正的请求就不进行发送

不是则取出token进行下一步判断。

```java
String token="";
// 如果不是预请求，则取出请求头
if (!request.getMethod().equals("OPTIONS")) {
    // 3.获取请求头中的令牌（token），下面这行解析需要和前端约定好
    token= request.getHeader(AUTHORIZATION).replace("Bearer ", "");
}
//如果是，则应该放行
else{
    return true;
}
```

这个问题真的夸张！为什么，因为我们已经在后端配置了跨域处理，前端控制台也没报Options请求或者跨域之类的提示信息，（我真的没第一时间想到是Options请求）这简直完美了诠释**会者不难，难者不会**这句话阿！

##### 结果展示：

可以看到，成功登录，并且请求携带`JWT` `token`：

![image-20230727101352444](API接口开放平台笔记.assets/image-20230727101352444.png)

##### 新的问题：

登出函数又炸了。查看后端，可以发现请求没携带`session`导致抛空指针异常了。

```java
@Override
public boolean userLogout(HttpServletRequest request) {
    if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
    }
    // 移除登录态
    request.getSession().removeAttribute(USER_LOGIN_STATE);
    return true;
}
```

![image-20230727153047332](API接口开放平台笔记.assets/image-20230727153047332.png)

修改一下登出的逻辑就能解决`session`为空的问题。

改成判断ThreadLocal里有没有当前用户

```java
@Override
public boolean userLogout(HttpServletRequest request) {
    if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
        // 由于我们使用了自定义请求头，导致之前的跨域处理都无效了，
        // 所以此处前端可能没携带session，session为空抛异常
        // todo，暂时没想好怎么处理
        // throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        ThrowUtils.throwIf(UserHolder.getUser()==null,ErrorCode.NOT_LOGIN_ERROR);
    }
    // 移除登录态，下面不知道会不会空指针，不过没动也没报错
    request.getSession().removeAttribute(USER_LOGIN_STATE);
    return true;
}
```

但是到底为什么，明明后端已经设置了跨域处理，可之前对付跨域的操作全都无效了？

### 重测上传文件：

#### 新的`bug`：

发现做了那么多，结果上传文件的请求竟然不会携带自定义请求头！原来请求没携带cookie但并不是跨域问题

上`github`发现确实有这个情况。

![image-20230727155810947](API接口开放平台笔记.assets/image-20230727155810947.png)

![image-20230727155835077](API接口开放平台笔记.assets/image-20230727155835077.png)

所以最后还是的使用第一个方法，在上传文件时增加`headers`参数，来设置上传的请求头部。

```java
<Upload
    //...略
    headers={{
        Authorization:localStorage.getItem('token')||''
    }}
//...略
>
    //...略
    </Upload>
```

#### 最后结果：

可以看到，前端成功携带自定义请求头，并且上传文件成功

![image-20230727162052983](API接口开放平台笔记.assets/image-20230727162052983.png)

![image-20230727162234152](API接口开放平台笔记.assets/image-20230727162234152.png)

所以一开始就应该直接在`Upload`组件里的`header`请求参数中添加`session`的请求头的。

真是夸张，所以没系统学过前端就不要去多碰。真是绕了好大一个圈子！

#### 跨域处理失效真相：

既然一开始其实本来就没有跨域，那为什么跨域请求在我们添加了拦截器处理`JWT`令牌之后又真的出现了呢？

等等，你说...在我们添加了拦截器以后？

拦截器如下：

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    //拦截器对象
    @Resource
    private LoginCheckInterceptor loginCheckInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //注册自定义拦截器对象，拦截所有路径，排除登录，不拦截登录
        registry.addInterceptor(loginCheckInterceptor)
                //.addPathPatterns("/**")//拦截所有请求，而且在放行以后还会被拦截回来
                //排除路径
                .excludePathPatterns(
                        //有注册，登录，和微信登录三个需要放行
                        "/user/login",
                        "/user/login/wx_open",
                        "/user//register",
                        //放行swagger
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources",
                        "/v2/api-docs"
                );
    }
}
```

跨域处理如下：

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 覆盖所有请求
        registry.addMapping("/**")
                // 允许发送 Cookie
                .allowCredentials(true)
                // 放行哪些域名（必须用 patterns，否则 * 会和 allowCredentials 冲突）
                // 放行所有域名...
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                //表示访问请求中允许携带哪些Header信息，如：Accept、Accept-Language、Content-Language、Content-Type
                .allowedHeaders("*")
                //暴露哪些头部信息(因为跨域访问默认不能获取全部头部信息)
                .exposedHeaders("*");
    }
}
```

现在同时重写`WebMvcConfigurer`接口的`addCorsMappings`和`addInterceptors`的两个方法，它们谁先生效？？

答案已经呼之欲出了，我们自定义的拦截器中有三个拦截步骤：

- ​	preHandle方法：目标资源方法执行前执行。 返回true：放行    返回false：不放行

- ​	postHandle方法：目标资源方法执行后执行

- ​	afterCompletion方法：视图渲染完毕后执行，最后执行

而很遗憾，我们的跨域处理是第四个拦截，而拦截器是责任链，一旦有一个拦截器不放行，后面都不会再执行，所以跨域处理失效了。

真是...庸人自扰阿，真相竟然是这样...

知道了这个问题以后，就是查找解决方案了，其实我上面已经给出了一种解决方案，那就是拦截器里判断`Options`，但很明显，这样不够优雅，我们要让跨域处理先于自定义拦截器，这样才是最好的！

答案是：做不到...至少在`WebMvcConfigurer`里做不到，这个需要阅读源码。

走投无路了吗？不！我们知道一个http请求，先走filter，到达servlet后才进行拦截器的处理，如果我们把cors放在filter里，就可以优先于权限拦截器执行了！！！

1、配置如下：

```java
@Configuration
public class MyCorsFilterConfig {
    @Bean
    public CorsFilter  corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 当allowCredentials 为 true 时，allowOrigins 不能包含特殊值“*”，
        // 因为无法在“Access-Control-Allow-Origin”响应标头上设置该值。
        // 要允许一组源的凭据，请明确列出它们或考虑改用“allowedOriginPatterns”。
        //config.addAllowedOrigin("*");
        config.addAllowedOriginPattern("*");
        // 允许发送 Cookie
        config.setAllowCredentials(true);
        //config.addAllowedMethod("GET", "POST", "PUT", "DELETE", "OPTIONS");
        config.addAllowedMethod("*");
        //表示访问请求中允许携带哪些Header信息，如：Accept、Accept-Language、Content-Language、Content-Type
        config.addAllowedHeader("*");
        //暴露哪些头部信息(因为跨域访问默认不能获取全部头部信息)
        config.addExposedHeader("*");
        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
        // 覆盖所有请求
        configSource.registerCorsConfiguration("/**", config);
        return new CorsFilter(configSource);
    }
}
```

2、原来拦截器里的跨域处理只需要注释掉@`Configuration`即可不交给IOC容器管理

3、删掉自定义拦截器里的`Options`判断，

4、打开浏览器，再次进行测试，完美解决跨域处理失效：

![image-20230727235512851](API接口开放平台笔记.assets/image-20230727235512851.png)

然而文件上传还是没携带`session`，再次实锤只是单纯的Upload组件的默认上传方法只会传默认的请求头。

![image-20230727235604649](API接口开放平台笔记.assets/image-20230727235604649.png)

5、把过滤器也注释掉，再进行对照实验，可以看到典中典的问题再次出现：

![image-20230727235825667](API接口开放平台笔记.assets/image-20230727235825667.png)

6、把Options判断加回（但是过滤器不加回），还是跨域，为什么，因为`login`操作被放行了，没有进行自定义拦截器的Options判断，这也解释了为什么之前`跨域拦截器`和自定义拦截器`Options判断`能成功实现业务，因为放行后的`login`被`跨域拦截器`处理了，所以才能成功登录，之后就都是`Options判断`在起作用了：

![image-20230728000706363](API接口开放平台笔记.assets/image-20230728000706363.png)

最后总结：学习绝不能囫囵吞枣，如果我单纯的以为上传文件不携带cookie是跨域问题，如果我添加判断`Options`就不理会后端跨域处理为什么失效，那么，就全完蛋咯。

#### 流程图1：

下面是第一种可行方案，不够优雅：

![image-20230803144432856](API接口开放平台笔记.assets/image-20230803144432856.png)

#### 流程图2：

![image-20230803145106006](API接口开放平台笔记.assets/image-20230803145106006.png)

如果你看了我的笔记还不能解决问题，那么我解决问题的参考文档如下：

https://github.com/spring-projects/spring-boot/issues/9595

https://blog.csdn.net/z69183787/article/details/109483337

https://spring.io/blog/2015/06/08/cors-support-in-spring-framework

## 2、签名优化

### 调用接口逻辑：

#### 为什么需要签名认证：

1、安全性，不能随便谁都来调用接口

2、适合无需保存登录状态的场景，方便后期扩展业务，比如提供一些免登录的接口供游客玩耍吸引客户

#### 实现方式：

通过 http request header 头传递参数

参数1：accessKey：调用标识 例如：UserA、UserB（需要尽量复杂、无序、无规律）

参数2：secretKey：秘钥

（可以理解为用户名和密码，区别：accessKey和secretKey是无状态的）

不可以将秘钥直接在服务器之间传递，有可能在传递中途拦截

解决方法：给密码加密，sign（签名）

密方式有：对称加密、非对称加密、md5 签名（不可解密）

实现流程：传递 用户参数 + 秘钥 => 签名生成算法 => 生成不可解密的值。

参数3：用户请求参数

参数4：sign 

**如何知道签名是正确的？**

服务器端和客户端使用一致的参数和生成算法去生成签名，只要一致，则通过。

**如何防重放？**

参数5：加 nonce 随机数，只能用一次。

服务器端要保存用过的随机数

参数6：加 timestamp 时间戳，校验时间戳是否过期

### 调用接口流程：

1、`InterfaceInfoController`的`invokeInterfaceInfo`函数判断接口是否存在，是否关闭

根据当前用户的密钥生成新的`Client`，然后调用`Client`的`invokeInterfaceInfo`函数去调用接口

2、`Client`会调用签名加密算法，对用户的密钥进行签名，并加到HTTP请求头中，然后将请求导向网关

3、网关会对签名进行校验，校验成功才会正式向调用接口。

上面过程中，Client和网关都调用了签名加密算法，会耗费大量时间。

可以看到，调用一次接口要一秒多，夸张点要4秒，体验非常不好

![image-20230727164319806](API接口开放平台笔记.assets/image-20230727164319806.png)

原因应该是：

网关需要从数据库中查出`SecretKey`重新生成签名来校验，查数据库和生成签名这个操作耗费了大量时间

```java
// 校验签名
// 应该通过 accessKey 查询数据库中的 secretKey 生成 sign 和前端传递的 sign 对比
String secretKey = invokeUser.getSecretKey();
//log.info("网关得到tSecretKey：{}", secretKey);
// todo 从Redis中直接取出已经签名好的sign
String serverSign = SignUtils.genSign(body, secretKey);
if (!sign.equals(serverSign)) {
    throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "签名错误！");
}
```

### 优化方法：

因为密钥是保存在数据库里持久化保存的，一旦被截获密钥，那么截获者就可以用密钥就拥有了永久调用资格

既然密钥不能直接在服务器之间传输，那么签名可不可以？

答案是肯定可以，因为你不传签名怎么对比认证？

那么重新生成签名对比这一步，我们是否可以变成查询Redis缓存对比？

#### 问题一：

有朋友可能会想，那我是不是只需要截获签名就可以获得访问权？

对的，但是就算截获了，Redis有缓存机制，只要用户不活跃或者主动登出，我们就删除缓存，这样就能保证最低限度的安全性，不是说截获了签名就获得了永久调用资格，而且Redis对前端不可见，有安全性。

#### 问题二：

你这样，那我还传什么签名，`Client`直接查缓存看看有没有签名不就行了，好像是哦。

#### 问题三：

甚至干脆别传什么AccessKey和secretKey来签名了，就拿用户账号密码签名不就好了？

说得对，对于登录用户直接拿用户账号密码签名存Redis就可以了，所以Client端要先判断一下Redis，没有再根据AccessKey和secretKey生成签名，为什么最后还是要AccessKey和secretKey呢？

因为我设想的有三个调用我的开放平台的场景：

1. 未登录用户，有几次体验接口的资格
2. 已登录用户，更快速的调用，更多的调用资格
3. 分发SDK给其他项目使用，只需要AccessKey和secretKey。

### 业务逻辑：

1. 前端，是否选择免登录，是，可以调用后端注册功能，生成临时账号保存在Redis中，并把临时账号保存在全局变量中。这样就能记录该账号调用接口的次数并加以限制，当用户关闭网页，由于使用JWT`localStorage`的存在，也能防止次数刷新的情况。（其实未登录怎么记录状态，怎么防止前端生成一大堆垃圾账号来调用接口，是个大问题）
2. 服务器端，查询是否是临时账号，否，异步生成签名并存入缓存，是，则可以选择没有动作，也可以为了代码逻辑一致性，也生成签名存入缓存
3. 客户端，查询缓存是否有签名，没有，重新生成签名并传输网关，有，则不需要传输签名
4. 网关，查看签名字段是否有值，有值重新生成签名对比，没值查询缓存
5. 分发SDK时，服务端是由该项目成员自己编写，他不会通过request生成新的客户端，而是通过配置文件依赖注入来生成客户端，对于这种情况，只能在调用客户端的时候才生成签名并传输给网关了。
6. 所以上面的优化只是针对网页调用的优化。

### 实现：

暂时放弃未注册端，先优化在线平台的接口调用.......

总结：上面优化对代码改动比较大，而且没针对分发SDK的情况，对于未登录的状态记录也很复杂，放弃了。

把想法记录在这里，将来有新的想法，再去实现吧。

## 3、用户上传接口

网站的运营不能只靠管理者，还要提供良好的社区，这个功能想要实现，还是需要好好考虑的。

初步设想功能，参考了`nero`哥的设计：

API商店：展示接口，供用户开通接口，查看接口

我的接口：展示用户开通的接口和用户创建的接口，并且能管理用户创建的接口

管理中心：管理员管理所有接口并分析接口数据

个人中心：修改资料，上传头像，重置密钥，注册等功能

![image-20230729115825125](API接口开放平台笔记.assets/image-20230729115825125.png)

### 1、现有的问题：

还是要缕一缕我们之前的接口调用逻辑

在服务器端的`InterfaceInfoController`的调用接口函数中`invokeInterfaceInfo`，该函数会根据用户`accessKey`和`secretKey`生成对应客户端，然后传参数调用客户端的`getUsernameByPost`函数

```java
User loginUser = userService.getLoginUser(request);
String accessKey = loginUser.getAccessKey();
String secretKey = loginUser.getSecretKey();
YuApiClient tempClient = new YuApiClient(accessKey, secretKey);
Gson gson = new Gson();
com.yupi.yuapiclientsdk.model.User user = gson.fromJson(userRequestParams, com.yupi.yuapiclientsdk.model.User.class);
//调用客户端对应的接口处理函数被写死了
String usernameByPost = tempClient.getUsernameByPost(user);
return ResultUtils.success(usernameByPost);
```

而客户端则会将对应接口请求导向网关

```java
public String getUsernameByPost(User user) {
    String json = JSONUtil.toJsonStr(user);
    // path被写死了
    HttpResponse httpResponse = HttpRequest.post(GATEWAY_HOST + "/api/name/user")
        .addHeaders(getHeaderMap(json))
        .body(json)
        .execute();
    System.out.println(httpResponse.getStatus());
    String result = httpResponse.body();
    System.out.println(result);
    return result;
}
```

网关最后将请求导向真正的接口

```java
@PostMapping("/user")
    public String getUsernameByPost(@RequestBody User user, HttpServletRequest request) {
        String result = "POST 用户名字是" + user.getUsername();
        return result;
    }
```

可以看到，灵活性扩展性非常差，接口端增加新的接口A时，对应客户端就要新增一个处理该接口并导向网关的函数AA，而服务端如果按着这个逻辑走下去，也不能灵活的去调用对应的客户端函数AA，甚至需要大量的`if-else`判断才知道该调用客户端的哪个处理接口的函数。

硬编码带来的问题就是，我们现在做出来的只是一个测试玩具，只能勉强调用通一个接口罢了。

那么我们该怎么解决上面的问题呢？

### 2、客户端层面：

客户端层面，想要做到灵活调用，那么我们就不可以在导向网关时把path写死

```java
 HttpResponse httpResponse = HttpRequest.post(GATEWAY_HOST + "/api/name/user")
```

所以就需要服务端传path参数，这样客户端就可以灵活处理接口导向网关的业务

对应的数据库增加path字段就省略了。

```java
public String invokeInterface(long id,String params, String path, String method ) throws UnsupportedEncodingException {
    String result=null;
    try(
        //没写死URL，并且增加try-catch，保证健壮性
        HttpResponse httpResponse = HttpRequest.post(GATEWAY_HOST + path)
        // 处理中文编码
        .header("Accept-Charset", CharsetUtil.UTF_8)
        .addHeaders(getHeaderMap(id,params, method))
        .body(params)
        .execute())
    {
        result=JSONUtil.formatJsonStr(httpResponse.body());
    }
    return result;
}
```

而服务端也不需要单独调用客户端对应的处理接口函数了。

```java
ReApiClient reApiClient = interfaceInfoService.getReApiClient(request);
String invokeResult = null;
try {
    // 执行方法，没写死调用客户端对应的接口处理函数，而是根据参数灵活判断
    invokeResult = reApiClient.invokeInterface(id,requestParams, path, method);
    if (StringUtils.isBlank(invokeResult)) {
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口数据为空");
    }
} catch (Exception e) {
    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口验证失败");
}
```

### 3、接口端层面：

现在还有的问题是，接口端，当用户新增一个可以调用的接口时，难道我们在自己的接口端也要写一遍对应的调用吗？很明显这也是一种硬编码，为什么会产生这种情况，原因是，我们虽然指定了URL，但没指定Host，

```java
@PostMapping("/api/poison")
public String poisonChicken(HttpServletRequest request) {
    // 下面这个URL，虽然path可以确定，但host只能写死，因为根本没传host参数
    String url = "http://api.btstu.cn/api/poison";
    String body = URLUtil.decode(request.getHeader("body"), CharsetUtil.CHARSET_UTF_8);
    HttpResponse httpResponse = HttpRequest.get(url + "?" + body)
        .execute();
    return httpResponse.body();
}
```

所以解决问题的方式也很简单了，那就是指定`Host`，这样对于用户上传的接口我们就可以灵活的调用了

而我们在用户上传接口的时候，并不会要求用户自己手段把URL分为Host和path。

最后思路如下：

1. 数据库接口必要要有URL字段和path字段以及method字段
2. 对于path字段，只有管理员能改动，代表本地接口访问接口端的路径
3. 对于用户，则是默认路径，统一导向接口端的一个处理外部接口的方法中
4. 在这个统一处理外部接口的方法中，我们需要分别处理几个常用的请求方式，因为不同的请求，调用Http的方式也不同，这就用上了method字段
5. 最后呈现效果如下：

### 4、后端联动：

服务器端的`InterfaceInfoController`的调用接口函数中`invokeInterfaceInfo`，生成客户端，调用客户端来调用真正的接口

```java
String invokeResult = null;
try {
    // 执行方法
    invokeResult = reApiClient.invokeInterface(id,requestParams, url, method,path);
    if (StringUtils.isBlank(invokeResult)) {
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口数据为空");
    }
} catch (Exception e) {
    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口验证失败");
}
return ResultUtils.success(invokeResult);
```

Client端统一处理来自服务器的调用

```java
public String invokeInterface(long id,String params, String url, String method,String path) throws UnsupportedEncodingException {
    String result=null;
    try(
        // 转发给网关
        HttpResponse httpResponse = HttpRequest.post(GATEWAY_HOST + path)
        // 处理中文编码
        .header("Accept-Charset", CharsetUtil.UTF_8)
        // 请求头携带必要参数
        .addHeaders(getHeaderMap(id,params, method,path,url))
        .body(params)
        .execute())
    {
        result=JSONUtil.formatJsonStr(httpResponse.body());
    }
    return result;
}
```

网关，根据ID，URL，method，path，查询是否正确匹配，然后转发到接口端（其实能走到这一步已经判断完了签名认证，证明不是对网关的直接攻击，而是走的服务器，所以此处直接通过ID查询接口也可以）：

```java
InterfaceInfo interfaceInfo = null;
try {
    interfaceInfo = innerInterfaceInfoService.getInterfaceInfo(InterfaceInfoId,URL,method,path);
} catch (Exception e) {
    log.error("getInvokeInterface error", e);
}
if (interfaceInfo == null) {
    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口不存在！");
}
// 5. 请求转发，调用模拟接口
// 5.1 去除无用请求头，不知道能不能提高一点速度
headers.remove(APIHeaderConstant.SIGN);
headers.remove(APIHeaderConstant.TIMESTAMP);
headers.remove(APIHeaderConstant.ACCESSKEY);
// 一次性判断的这个不能删
// headers.remove(APIHeaderConstant.NONCE);
// 只留下URL，path，method，InterfaceID四个请求头
ServerHttpRequest newRequest = request.mutate().headers(httpHeaders -> httpHeaders.addAll(headers)).build();
// 6. 响应日志
//return handleResponse(exchange, chain, interfaceInfo.getId(), invokeUser.getId());
return handleResponse(exchange.mutate().request(newRequest).build(), chain, interfaceInfo.getId(), invokeUser.getId());

```

接口端，根据URL和method以及参数，如果是第三方接口调用，全导向下面路径，进行POST或者GET的第三方接口调用：

```java
@RestController
public class GeneralAPIController {
    @PostMapping("/general/api")
    public String GeneralAPI(HttpServletRequest request) {
        String url = request.getHeader(APIHeaderConstant.URL);
        String method = request.getHeader(APIHeaderConstant.METHOD);
        String body = URLUtil.decode(request.getHeader("body"), CharsetUtil.CHARSET_UTF_8);
        // 如果是get请求
        String result = null;
        if(method.equals(MethodEnum.GET.getValue())){
            try(HttpResponse httpResponse = HttpRequest.get(url + "?" + body).execute()) {
                result = httpResponse.body();
            }
        }
        else if(method.equals(MethodEnum.POST.getValue())){
            try(HttpResponse httpResponse = HttpRequest.post(url)
                // 处理中文编码
                .header("Accept-Charset", CharsetUtil.UTF_8)
                // 传递参数
                .body(body)
                .execute())
            {
                result= httpResponse.body();
            }
        }
        // put和post基本一致
        // 不用转JSON，因为Client收到结果会转
        return result;
    }
}
```

接下来，修改服务器端的`InterfaceInfoController`，让老百姓也能创建和发布接口

这里需要频繁的判断是不是本人或者管理员，建议使用AOP切面

思路：

1. 给注解设置userRole和userId两个参数
2. AOP中该注解这两个参数必须至少传一个
3. 如果有两个参数：
   1. 看是不是本人
   2. 看是不是管理员
   3. 都不是，抛异常
4. 如果只有其中一个参数
   1. 看是不是本人，不是抛异常
   2. 看是不是管理员，不是抛异常

新注解：

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UserAuthCheck {

    /**
     * 必须有某个角色，或者必须是某个用户
     *
     * @return
     */
    String mustRole() default "";

    /**
     * 
     * @return
     */
    long mustId() default 0L;

}
```

新AOP，注意这个AOP还是不要和原本的AuthInterceptor合并，因为有些资源就算是本人，如果不是管理员也不能使用（你只有游戏账号的使用权，哈哈哈哈）

```java
@Aspect
@Component
public class UserAuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 执行拦截
     *
     * @param joinPoint
     * @param UserAuthCheck
     * @return
     */
    @Around("@annotation(UserAuthCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, UserAuthCheck UserAuthCheck) throws Throwable {
        LoginUserVO loginUser = userService.getLoginUserByThreadLocal();
        long mustId = UserAuthCheck.mustId();
        String mustRole = UserAuthCheck.mustRole();
        // 必须传两个参数，为了省事
        ThrowUtils.throwIf(mustId==0L&&StringUtils.isBlank(mustRole),ErrorCode.NO_AUTH_ERROR);

        // 必须有该权限才通过
        if (StringUtils.isNotBlank(mustRole)) {
            UserRoleEnum mustUserRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
            if (mustUserRoleEnum == null) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            String userRole = loginUser.getUserRole();
            // 如果被封号，直接拒绝
            if (UserRoleEnum.BAN.equals(mustUserRoleEnum)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            // 必须有管理员权限
            if (UserRoleEnum.ADMIN.equals(mustUserRoleEnum)) {
                if (!mustRole.equals(userRole)) {
                    //当前用户不是管理员，那就看是不是本人发布的资源
                    ThrowUtils.throwIf(!loginUser.getId().equals(mustId),ErrorCode.NO_AUTH_ERROR);
                }
            }
        }
        // 有管理员权限，或者是本人，放行通过权限校验，放行
        return joinPoint.proceed();
    }
}
```

上面这个想法是非常天真的，因为我当时想着：在想要使用注解的类中添加静态变量：代表用户ID，然后在拦截器中就可以对该静态变量进行赋值，然后作为参数传进该注解里，结果告诉我注解传参只能是常量！那就尴尬了。

所以我们还是单纯同时判断是不是管理员和本人就好吧，自行搞定。

最后在接口的参数校验过程加上下面这个判断：

```java
LoginUserVO loginUser = userService.getLoginUserByThreadLocal();
if(!userService.isAdmin(loginUser)){
    // 如果不是管理员，而接口路径又不是默认路径，抛异常
    ThrowUtils.throwIf(!interfaceInfo.getPath().equals(DefaultPath.PATH),ErrorCode.PARAMS_ERROR);
}
// 还应该校验接口url、host是否合法。
```

最后一些创建接口和删除接口的逻辑细节，比如是否要处理多张表，要不要事务，又或者是哪些函数是用户本人或者管理员可用，就自行梳理吧，只要合理就行。

### 5、前端：

#### 1、关键数据设置修改权限

不是管理员不允许修改或创建`path`表单

```typescript
<ProFormText
name="path"
label="接口路径"
initialValue={values.path}
rules={[{ required: true, message: '接口路径不可为空！' }]}
hidden={initialState?.loginUser?.userRole!=='admin'}
/>
```

#### 2、个人接口页面的展示：

**要求：展示用户开通的接口或者用户创建的接口**

设计思路：使用一个全局响应式变量`flag`，初始为0，代表显示用户已经开通的接口。

此时会显示我创建的接口按钮，点击按钮后，`flag`会发生改变，并根据`flag`重新加载数据

`flag`为0，加载的是开通的接口，`flag`为1加载的是创建的接口

```typescript
const [flag, setFlag] = useState(0);
const handleOnclick = () => {
    // 点击按钮修改flag状态
    setFlag(flag === 1 ? 0 : 1);
    loadData();
  };
<div>
    {flag === 0 ? (
     <Button type="primary" shape="round" onClick={handleOnclick}>
    查看我创建的接口
</Button>
) : null}
{flag === 1 ? (
    <Button type="primary" shape="round" onClick={handleOnclick}>
    查看我开通的接口
 </Button>
 ) : null}
</div>
```

很简单的逻辑，结果前端又出大bug了

![image-20230728214626998](API接口开放平台笔记.assets/image-20230728214626998.png)

将第一次点击事件前后的值打印出来，可以看到毫无变化！

![image-20230728214634091](API接口开放平台笔记.assets/image-20230728214634091.png)

这个问题又一次诠释了会者不难难者不会的道理

由于React的状态更新是异步的，所以在调用loadData函数之前，所以可能无法获得最新的状态值。为了解决这个问题，可以使用useEffect钩子来监听flag状态的变化，并在变化后调用loadData函数。

直到解决了这个问题，我才想起来鱼皮似乎提过，但当时根本不理解是什么意思，所以也没放心上。

唉！我的时间！

```java
useEffect(() => {
    loadData();
  }, [flag]);
```

#### 2、增加用户管理自己接口的功能

思路：在前端卡片中增加一个按钮或者`Tooltip`，然后重定向到接口管理页面

使用重定向轻松能实现组件的复用。

我也曾想过把所有的`columns`抽成一个组件，分别供管理员和用户管理接口，但是搞得太复杂了，前端苦手难以实现。

```typescript
item.isOwnerByCurrentUser === true
    ? [
    <Tooltip title="已创建该接口" key="share">
    <div
    onClick={() => {
        history.push('/admin/interface_info');
    }}
        >
            管理接口
</div>
</Tooltip>,
]
: [
    <Tooltip title="已开通该接口" key="share">
    <SmileOutlined />
    </Tooltip>,
],
    ]}
```

在接口管理页面，根据用户身份获取接口数据，管理员可以管理所有接口，用户只能管理自己创建的。

```typescript
request={async (params) => {
    console.log('---------->', params);
    console.log('用户角色：', initialState?.loginUser?.userRole );
    // 根据角色获取不同的数据，user只能获取本人创建的接口
    const res = await (loginUser?.userRole === 'admin' ? 
                       listInterfaceInfoVOByPageUsingPOST : ListOwnInterfaceInfoVOByPageUsingPOST)({
        ...params,
    });
    if (res?.data) {
        return {
            data: res?.data.records ?? [],
            success: true,
            total: res.data.total ?? 0,
        };
    } else {
        return {
            data: [],
            success: false,
            total: 0,
        };
    }
}}
```

还可以增加不管用户身份只要是从我的接口页面跳转过来的，都只能展示创建的接口的功能。但是需要设置一个变量，让两个组件监听这个变量的变化，也很烦

#### 3、接口显示是否关闭

很简单，不说了

```typescript
item.status===1?
    [
    <Tooltip title="分享" key="share">
    <ShareAltOutlined />
    </Tooltip>,
]:
[
    <Tooltip title="接口已关闭" key="share">
    <FrownOutlined />
    </Tooltip>,
],
```

#### 4、展示效果：

![image-20230729122709004](API接口开放平台笔记.assets/image-20230729122709004.png)

#### 5、method下拉选项

由于我们后端处理第三方接口时是通过枚举判断请求方法：

```java
    POST("POST", "POST"),
    GET("GET", "GET"),
    PUT("PUT", "PUT");
```

所以前端最好只能在这三个方法中选择，修改`columns`：

```typescript
{
    title: '请求方法',
        dataIndex: 'method',
            valueType: 'select',
                valueEnum: {
                    GET: {
                        text: 'GET',
                    },
                        POST: {
                            text: 'POST',
                        },
                            PUT:{
                                text: 'PUT',
                            },
                },
                    formItemProps: {
                        rules: [
                            {
                                required: false,
                            },
                        ],
                    },
},
```

修改填写表单由原来的`ProFormText`变为`ProFormSelect`：

```typescript
<ProFormSelect
name="method"
label="请求方法"
options={[
         { label: 'GET', value: 'GET' },
         { label: 'POST', value: 'POST' },
         { label: 'PUT', value: 'PUT' },
         ]}
rules={[{ required: true, message: '请求方法不可为空！' }]}
/>
```

完美。

### 6、前后端联调：

第一次联调就炸了，经过调试，发现，是去除请求头的时候挂掉了。

```java
// 5. 请求转发，调用模拟接口
// 5.1 去除无用请求头，不知道能不能提高一点速度
headers.remove(APIHeaderConstant.SIGN);
headers.remove(APIHeaderConstant.TIMESTAMP);
headers.remove(APIHeaderConstant.ACCESSKEY);
// 一次性判断的这个不能删
// headers.remove(APIHeaderConstant.NONCE);
// 只留下URL，path，method，InterfaceID四个请求头
ServerHttpRequest newRequest = request.mutate().headers(httpHeaders -> httpHeaders.addAll(headers)).build();
// 6. 响应日志
return handleResponse(exchange.mutate().request(newRequest).build(), chain, interfaceInfo.getId(), invokeUser.getId());

```

先不管这个问题的原因，恢复之前不去除无用请求头的代码：

联调成功：

![image-20230729121227025](API接口开放平台笔记.assets/image-20230729121227025.png)

其他接口调用，一次十秒真的夸张阿，看来还是有必要优化一下调用速度：

![image-20230729121352733](API接口开放平台笔记.assets/image-20230729121352733.png)

这次不小心给VSC前端代码打了一个断点，让我发现其实是可以调试前端的，😂，我之前以为前端只能控制台输出来调试，尴尬阿。

### 7、创建外部接口功能测试：

先创建接口，这个没什么好说的，关键是能否调用这个外部接口！

![image-20230729123316449](API接口开放平台笔记.assets/image-20230729123316449.png)

发布，发布成功，证明接口能调用得通：

![image-20230729124136533](API接口开放平台笔记.assets/image-20230729124136533.png)

但是也别高兴得太早，可能返回值是null之类的

在线调用测试：

网关报错，线程池炸了，看报错地点是nacos报的错：

```
Task java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask@21ac0e8 rejected from java.util.concurrent.ScheduledThreadPoolExecutor@609f5b83[Terminated, pool size = 0, active threads = 0, queued tasks = 0, completed tasks = 5]
	at java.util.concurrent.ThreadPoolExecutor$AbortPolicy.rejectedExecution(ThreadPoolExecutor.java:2063) ~[na:1.8.0_201]
```

![image-20230729150937377](API接口开放平台笔记.assets/image-20230729150937377.png)

网上有说版本不一致的：

https://www.cnblogs.com/fanshuyao/p/14572830.html

https://github.com/alibaba/spring-cloud-alibaba/issues/2186

我的只是单纯路径没对应上，大家多检查一下网关的路由断言：

```java
      routes:
        - id: api_route
          uri: http://localhost:8123
          predicates:
            - Path=/**
```

再进行测试，搞定：

![image-20230729154802872](API接口开放平台笔记.assets/image-20230729154802872.png)

只要你的接口是免费的，开放的，免密钥的，那么就可以接入我的Re-API使用，真正实现开放平台。

### 8、最后的问题：

你这样搞一通操作下来，那要是分发SDK给其他项目组这种情况，其他项目组使用Client那不是还需要知道一大堆参数吗？能不能打上Client加一个`.`就能自动联想我们要调用的对应函数，实现`URL，method，path，id`的自动填写，使用该SDK的人只需要考虑`requestParams`就可以了，因为只有`requestParams`是可能有变化的。

```java
invokeResult = reApiClient.invokeInterface(id,requestParams, url, method,path);
```

就像鱼皮最开始写的代码一样，只需要传参数就可以调用对应接口，不需要使用者考虑一些固定的额外参数：

```java
Gson gson = new Gson();
com.yupi.yuapiclientsdk.model.User user = gson.fromJson(userRequestParams, com.yupi.yuapiclientsdk.model.User.class);
String usernameByPost = tempClient.getUsernameByPost(user);
```

想法很好，我没辙了。

因为鱼皮的代码，其实也是从数据库中查出对应的接口先的，我上面的参数都是在查出接口的基础上实现的，我们在分发SDK的时候，而总不可能把数据库也交给别人吧：

```java
long id = interfaceInfoInvokeRequest.getId();
String userRequestParams = interfaceInfoInvokeRequest.getUserRequestParams();
// 判断是否存在
InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
```

所以我的结论是：想利用SDK使用对应的接口，接口参数至少要上我们的网站查出来才能使用。

实现`URL，method，path，id`的自动填写，让使用该SDK的人只需要考虑`requestParams`这个功能，我暂时没法子实现，希望其他人有更好的想法。

## 4、Maven多模块构建

首先是`common`模块，我们要保证该模块与分离之前同包同名，这样就不需要我们手动修改`import`的路径了

然后是`common`包的结构，首先，为了实现`Dubbo`远程调用我们项目的内部接口，减少冗余代码，`common`包需要有基本的`Service`接口，而这些接口需要返回原本`model`里的`entity`，这就需要导入原本`model`。

导入原本的model以后，有哪些是需要精简的，不要添加进来的类呢？

我的想法是，没有，因为引入了`entity`，对应的`VO`需要吧，对应的请求也需要吧，枚举类或者常量，以及`common`的类也需要，为什么需要？因为分模块肯定是为了以后能复用这个`common`包，别的项目需要使用我们的实体类的时候，难道又需要他们封装一遍对应增删改查的`Request`吗

而`common`目录下的通用类也是同样的道理，因为它们非常常用，我们不需要每个项目都写一遍，直接引入`common`包就可以使用了。

其实这就像万用模板一样，只不过万用模板是把这些直接整合到一个项目里了而已。

![image-20230728093208798](API接口开放平台笔记.assets/image-20230728093208798.png)

注意事项

1. 使用父工程进行版本锁定，统一管理子工程的版本

2. 使用父工程传递一些通用的依赖，比如`Lombok`

3. 使用父工程来做聚合

4. 在这些会被其他模块引入的模块中，`pom`文件里的依赖尽量使用`optional`，让依赖不能传递，避免依赖不需要的依赖减缓构建速度。

   ```xml
   <optional>true</optional>
   ```

其他模块的分法，都大差不差，只要保证包名一样，就不会出现手动去删包导包的情况。

我还分了工具类模块，添加了父工程。分模块是好的习惯，其实并不复杂，反而条理分明。

真正导致复杂的原因是：把项目做完了才去分模块！！！

## 5、上传接口的安全性问题

### 考虑：

某个用户上线了很多接口，并发布，刚开始是能接通的，但很快就失效了，这就会导致大量不能接通的垃圾接口阻碍用户寻找能正常使用的接口：

### 1、接口安全性之解决办法1：

1、用户调用接口失败后，将接口关闭。（异想天开，不可能用户传错参数导致失败也把接口给关了吧）

2、用户调用接口失败后，使用默认参数再调用一次接口，若还是失败，下线该接口，该步骤可以异步进行。

```java
String invokeResult = null;
try {
    // 执行方法
    invokeResult = reApiClient.invokeInterface(id,requestParams, url, method,path);
    if (StringUtils.isBlank(invokeResult)) {
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口数据为空");
    }
} catch (Exception e) {
    //调用失败，开子线程使用默认参数确认接口是否可用，本线程继续直接返回旧值
    HandleInterface_EXECUTOR.submit(() -> {
        try {
            // 1.查询初始参考请求参数
            String tempRequestParams = oldInterfaceInfo.getRequestParams();
            // 2.使用原始参考请求参数调用接口，不需要返回值，只需要看这个过程有没有抛异常
            reApiClient.invokeInterface(id, tempRequestParams, url, method, path);
        } catch (Exception ee) {
            // 3.调用失败，关闭该接口
            IdRequest idRequest = new IdRequest();
            idRequest.setId(id);
            this.offlineInterfaceInfo(idRequest);
            // 4.输出关闭接口的日志
            log.info("接口调用失败，关闭该接口{}",id);
            // 5.打印失败日志，就不抛异常了。因为下面已经抛过了
            ee.printStackTrace();
        }
    });
    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口验证失败");
}
```

### 2、自动关闭异常接口测试：

就拿nameCtroller测试吧，把路径从`/user`变为`/users`，这样由于与数据库接口路径不一致，该接口将调用不通

```java
@PostMapping("/users")
    public String getUsernameByPost(@RequestBody User user) {
        return  "NameControl: POST 用户名字是" + user.getUsername();
    }
```

发现问题，如果网关转发成功，最后接口端没找到路径并不会报错，只会报not found：

![image-20230729204655236](API接口开放平台笔记.assets/image-20230729204655236.png)

额，这个还真不知道怎么捕获这种错误。

换一个测试方式，直接异常：

```java
@PostMapping("/users")
    public String getUsernameByPost(@RequestBody User user) {
    	int i=1/0;
        return  "NameControl: POST 用户名字是" + user.getUsername();
    }
```

还是不行...

![image-20230729210652660](API接口开放平台笔记.assets/image-20230729210652660.png)

看来不解决这个问题没办法测试了，这个问题，应该是因为接口模块抛的异常，在服务端无法catch到吧？

不太清楚，但挺好解决的。

解决的办法就是，在不抛异常时，拿到结果，看里面是否含有关键字，含有则下线接口。

```java
@Override
public String getInvokeResult(InterfaceInfoInvokeRequest interfaceInfoInvokeRequest, HttpServletRequest request, InterfaceInfo oldInterfaceInfo) {
    // 接口请求地址
    Long id = oldInterfaceInfo.getId();
    String url = oldInterfaceInfo.getUrl();
    String method = oldInterfaceInfo.getMethod();
    // 接口请求路径
    String path = oldInterfaceInfo.getPath();
    String requestParams = interfaceInfoInvokeRequest.getUserRequestParams();
    // 获取SDK客户端
    ReApiClient reApiClient = userService.getReApiClient(request);
    // 设置网关地址
    reApiClient.setGatewayHost(gatewayConfig.getHost());
    String invokeResult ;
    try {
        // 执行方法
        invokeResult = reApiClient.invokeInterface(id,requestParams, url, method,path);
        if (StrUtil.isBlank(invokeResult)||invokeResult.contains("Whitelabel Error Page")) {
            // 直接抛异常，下面会catch到然后用默认参数重试接口
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    } catch (Exception e) {
        //调用失败，开子线程使用默认参数确认接口是否可用，本线程继续直接返回旧值
        HandleInterface_EXECUTOR.submit(() -> {
            try {
                // 1.查询初始参考请求参数
                String tempRequestParams = oldInterfaceInfo.getRequestParams();
                // 如果请求参数和默认参数一致，证明是发布接口请求，直接返回即可，不用关闭
                ThrowUtils.throwIf(requestParams.equals(tempRequestParams),ErrorCode.SYSTEM_ERROR, "接口验证失败");
                // 2.使用原始参考请求参数调用接口，不需要返回值，只需要看这个过程有没有抛异常
                String result = reApiClient.invokeInterface(id, tempRequestParams, url, method, path);
                if(result.contains("Whitelabel Error Page")){
                    // 不用写message，不管写什么都会被最后一个throw覆盖
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
            } catch (Exception ee) {
                // 还是抛异常，下线该接口
                offlineInterface(id);
                // 5.打印失败日志，就不抛异常了。
                ee.printStackTrace();
            }
        });
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口异常");
    }
    return invokeResult;
}
```

再次测试，这次没有返回结果了：

![image-20230729214720352](API接口开放平台笔记.assets/image-20230729214720352.png)

再次调用，可以看到接口已经关闭：

![image-20230729214859500](API接口开放平台笔记.assets/image-20230729214859500.png)

可以给数据库里接口的Status新增加一个状态，异常状态。不过我不想改了。

上面直接判断字符串里有没有`whitelabel Error Page`很不优雅！但不失为一种解决方案....

### 2.1：网关的全局异常处理

这里之所以会出现`whitelabel Error Page`的错误，是因为在接口端的异常，网关直接返回了错误结果，没有进行封装成统一响应，网关也无法用`@ControllerAdvice`，因为`filter`不在`SpringMvc`里：

![image-20230803093348184](API接口开放平台笔记.assets/image-20230803093348184.png)

解决办法，重写 ErrorWebExceptionHandler，然后就可以throw我们自定义的异常了：

```java
/**
 * 网关异常通用处理器，只作用在webflux 环境下 , 优先级低于 {@link ResponseStatusExceptionHandler} 执行
 */
@Slf4j
@Component
public class GlobalFilterExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }
        // 设置返回JSON
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        if (ex instanceof ResponseStatusException) {
            response.setStatusCode(((ResponseStatusException) ex).getStatus());
        }
        return handleBusinessException(exchange, ex);
    }

    public Mono<Void> handleBusinessException(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        return response.writeWith(Mono.fromSupplier(() -> {
            DataBufferFactory bufferFactory = response.bufferFactory();
            try {
                response.setStatusCode(HttpStatus.FORBIDDEN);
                log.error("{}\n {}", ex.getMessage(), ex.getStackTrace());
                // 封装成我们的统一响应
                BaseResponse<String> fail = ResultUtils.error(ErrorCode.SYSTEM_ERROR,ex.getMessage());
                log.error("{}",fail);
                return bufferFactory.wrap(objectMapper.writeValueAsBytes(fail));
            } catch (JsonProcessingException e) {
                return bufferFactory.wrap(new byte[0]);
            }
        }));
    }
}
```

网关异常处理的好处，当网关进行校验时，可以把错误信息直接返回给后端，由后端返回给前端，提示用户请求失败的原因：

```java
User invokeUser = innerUserService.getInvokeUser(accessKey);
if (invokeUser == null) {
    throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "accessKey 不合法！");
}
// 判断随机数是否存在，防止重放攻击
String key = KEY_PREFFIX+nonce;
String existNonce = (String) redisTemplate.opsForValue().get(key);
if (StringUtil.isNotBlank(existNonce)) {
    throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "请求重复！");
}
// 时间戳 和 当前时间不能超过 5 分钟 (300000毫秒)
long currentTimeMillis = System.currentTimeMillis() / 1000;
long difference = currentTimeMillis - Long.parseLong(timestamp);
if (Math.abs(difference) > 300000) {
    throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "请求超时！");
}
// 校验签名
// 应该通过 accessKey 查询数据库中的 secretKey 生成 sign 和前端传递的 sign 对比
String secretKey = invokeUser.getSecretKey();
//log.info("网关得到tSecretKey：{}", secretKey);
String serverSign = SignUtils.genSign(body, secretKey);
if (!sign.equals(serverSign)) {
    throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "签名错误！");
}
```

但经过测试，发现以上仅仅只是针对我们手动抛出的异常，也就是上面的全局异常处理只能处理网关层。

不过下级服务不存在的接口或者接口内部错误，还是会返回默认的错误结构，所以原本简单粗暴判断调用返回结果是否含有`"Whitelabel Error Page"`还是留下吧。

### 2.2：网关的异常处理2：

直接返回值设置状态码可以代替全局异常处理，然后服务端就能try-catch到异常，但是这时候服务端无法直接判断在网关哪一步挂掉，只知道挂掉了，需要去看网关的日志。

```java
private Mono<Void> handleNoAuth(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.setRawStatusCode(HttpStatus.FORBIDDEN.value());
        return response.setComplete();
    }
```

### 2.3：网关全局异常处理测试：

#### 1、测试捕获网关的校验异常：

前端发起测试，此时修改数据库

![image-20230803150939873](API接口开放平台笔记.assets/image-20230803150939873.png)

改库，模拟密钥无效的情况：

![image-20230803151032997](API接口开放平台笔记.assets/image-20230803151032997.png)

签名错误：

![image-20230803151135871](API接口开放平台笔记.assets/image-20230803151135871.png)

此时网关响应结果：

![image-20230803151232659](API接口开放平台笔记.assets/image-20230803151232659.png)

请求响应结果：（这里封了两层，对管理端的调用，code是ok，对接口端的调用是失败）：

![image-20230803152656689](API接口开放平台笔记.assets/image-20230803152656689.png)

前端响应结果：

![image-20230803151254476](API接口开放平台笔记.assets/image-20230803151254476.png)

顺利捕获网关的异常，并且直接反映到服务端，并提示用户，很舒服。

#### 2、测试捕获接口异常：

现在修改接口路径，路径和数据库不一致，将无法找到该接口：

![image-20230803151528334](API接口开放平台笔记.assets/image-20230803151528334.png)

网关试图捕获接口调用的异常：

```java
Mono<Void> voidMono ;
try{
    voidMono=handleResponse(exchange, chain, interfaceInfoId, userId);
}catch (Exception e){
    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"接口异常！调用失败");
}
return voidMono;
```

还是失败了：

![image-20230803154153177](API接口开放平台笔记.assets/image-20230803154153177.png)

不要在处理返回值的函数中抛异常，会有非阻塞式编程里使用阻塞编程的警告。而且也根本`catch`不到异常，因为对网关来说正常调用成功了，只是返回值是`whitelabel Error Page`罢了。

```java
ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        //thorw？
    }
```

### 2.4：为什么一定要捕获接口异常？

或许可以不用钻牛角尖，只要能把权限校验的异常和接口校验的异常区分就可以了，就像原来那样判断字符串里含不含`whitelabel Error Page`不就行了？

原因：其实有点白名单的思想，因为黑名单是拉不完的，不如设置白名单。

同理：很多异常都是`whitelabel Error Page`，可能有其他情况返回`whitelabel Error Page`，但接口完好，比如参数写错了？又或者是网关自身的问题。

如果不加以区分，那么定位错误的时候可能就定位到接口身上，忽略了网关。

### 2.5：装饰器修饰Response

对于网关请求下层接口（此时网关是服务器，下层接口是客户端）

如果请求路径不存在，返回的是：404 Not Found，

如果是语法错误或者参数错误则是：400 Bad Request ，比如请求参数格式不对

如果是下层接口内部错误，返回的是：500 Internal Server Error，比如前面模拟的1/0

或许我们只需要对500和404的返回结果进行装饰处理就可以辽。

注意一定要告知客户端Body的长度，如果不设置的话客户端会一直处于等待状态不结束！

```java
ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        if (body instanceof Flux) {
            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
            return super.writeWith(
                fluxBody.map(dataBuffer -> {
                    // 7. 调用成功，接口调用次数 + 1 invokeCount，拿到调用成功的 请求头和返回头
                    ServerHttpRequest invokeRequest = exchange.getRequest();
                    ServerHttpResponse invokeResponse = exchange.getResponse();
                    try {
                        postHandler(invokeRequest, invokeResponse, interfaceInfoId, userId);
                    } catch (Exception e) {
                        log.error("invokeCount error", e);
                    }
                    byte[] orgContent = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(orgContent);
                    DataBufferUtils.release(dataBuffer);//释放掉内存
                    // 构建日志
                    String data = new String(orgContent, StandardCharsets.UTF_8); //data
                    log.info("原始响应结果："+data);
                    // 打印日志
                    HttpStatus invokeStatusCode = invokeResponse.getStatusCode();
                    if(invokeStatusCode==HttpStatus.NOT_FOUND
                       ||invokeStatusCode==HttpStatus.INTERNAL_SERVER_ERROR
                       ||invokeStatusCode==HttpStatus.BAD_REQUEST){
                        data = String.
                            format("{\"code\": %d,\"msg\":\"%s\",\"data\":\"%s\"}",
                                   invokeResponse.getStatusCode().value(),
                                   invokeStatusCode==HttpStatus.BAD_REQUEST?
                                   "请求参数错误":
                                   (invokeStatusCode==HttpStatus.NOT_FOUND?
                                    "接口请求路径不存在":"接口内部异常"),
                                   "null");
                        log.info("响应结果：" + data);
                    }
                    // 告知客户端Body的长度，如果不设置的话客户端会一直处于等待状态不结束！
                    HttpHeaders headers = invokeResponse.getHeaders();
                    headers.setContentLength(data.length());
                    return bufferFactory.wrap(data.getBytes());
                })
            );
        } else {
            // 8. 调用失败，返回规范的错误码，这里永远不会执行，因为调用失败还是有orgContent返回，只不过是whitelabel Error Page
            log.error("<--- {} 响应code异常", getStatusCode());
        }
        return super.writeWith(body);
    }
```

返回结果很成功：

![image-20230803183441118](API接口开放平台笔记.assets/image-20230803183441118.png)

但是前端展示却，猜测是因为响应式编程？

经过测试发现，是因为`setContentLength`这一步，倘若不告知，那么很久才能出结果，但是写入完全，倘若告知，则写入不完全：

![image-20230803183837764](API接口开放平台笔记.assets/image-20230803183837764.png)

### 2.6：简单问题复杂化？

其实这里之所以会弄那么复杂的原因，就是在网关做了太多服务层了！

正确的姿势是，在接口层搞个全局异常处理器就可以辽。

实在没想到自己竟然会忘了规范。跟着视频做不思考，之后出问题也不多考虑一下，就硬着头皮做。

### 2.7：接口层异常处理测试

使用 `int i =1/0` 测试接口内部异常，响应成功，这种情况是要自动关闭接口的：

![image-20230803195642029](API接口开放平台笔记.assets/image-20230803195642029.png)

### 2.8：处理接口路径不存在的情况：

但是接口找不到的情况还是没统一响应。

所以在网关层还是要负责封装一下返回值？也可以选择在SDK端处理404的`Respond`。

如果还是要在网关层封装就要面对：spring gateway 截取response 长度缺失的问题

这个问题不是个例：https://www.cnblogs.com/garfieldcgf/p/10474898.html

最后问题暂时解决了，`setContentLength`似乎要`bytes.length`的值。

```java
byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
// 告知客户端Body的长度，如果不设置的话客户端会一直处于等待状态不结束
HttpHeaders headers = invokeResponse.getHeaders();
headers.setContentLength(bytes.length);
return bufferFactory.wrap(bytes);
```

测试结果（终于舒服了）：

![image-20230803212925585](API接口开放平台笔记.assets/image-20230803212925585.png)

### 2.9：bufferFactory.wrap写入不完全的原因：

对于响应结果：{"code": 404,"msg":"接口请求路径不存在","data":"null"}

字符串的长度比字节的短？？？

```java
log.info("date长度：{}",data.length());// 45
bytes.length// 63
```

在纯ASCII码中，字节数=字符串长度=字符数，因为每个字符有一个字节。

在Unicode中，byte/2=字符串长度=字符数，因为每个字符是2个字节。当ASCII码与其他双字节字符系统混合时，字节数等于ASCII字符数和双字节字符数*2。

很难知道如何计算长度。对于某些语言（如C），字符串的长度等于字节数。对于某些语言（如JS），字符串的长度等于字符数

原因参考文档：https://www.kmw.com/news/2914756.html

哇，巨坑无比！基础不牢地动山摇，会者不难难者不会。抄的代码害死人...

### 3、完整代码逻辑：

#### 3.1 管理端：

管理端调用接口，并根据返回值里的状态码来分析接口是否可用。

下面代码最好写在service层，controller层不要放那么重的业务。

```java
public String getInvokeResult(InterfaceInfoInvokeRequest interfaceInfoInvokeRequest, HttpServletRequest request, InterfaceInfo oldInterfaceInfo) {
    // 接口请求地址
    Long id = oldInterfaceInfo.getId();
    String url = oldInterfaceInfo.getUrl();
    String method = oldInterfaceInfo.getMethod();
    // 接口请求路径
    String path = oldInterfaceInfo.getPath();
    String requestParams = interfaceInfoInvokeRequest.getUserRequestParams();
    // 获取SDK客户端
    ReApiClient reApiClient = userService.getReApiClient(request);
    // 设置网关地址
    reApiClient.setGatewayHost(gatewayConfig.getHost());
    String invokeResult=null;
    try {
        // 执行方法
        invokeResult = reApiClient.invokeInterface(id,requestParams, url, method,path);
    } catch (Exception e) {
        // 调用失败，开子线程使用默认参数确认接口是否可用
        //tryAgainUsingOriginalParam(oldInterfaceInfo, id, url, method, path, requestParams, reApiClient);
        throw new BusinessException(ErrorCode.SYSTEM_ERROR,"接口调用失败");
    }
    // 走到下面，接口肯定调用成功了
    // 如果调用出现了接口内部异常或者路径错误，需要下线接口（网关已经将异常结果统一处理了）
    if (StrUtil.isBlank(invokeResult)) {
        throw new BusinessException(ErrorCode.SYSTEM_ERROR,"接口返回值为空");
    }
    else{
        JSONObject jsonObject = JSONUtil.parseObj(invokeResult);
        int code =(int) Optional.ofNullable(jsonObject.get("code")).orElse("-1");//要求接口返回必须是统一响应格式
        ThrowUtils.throwIf(code==-1,ErrorCode.SYSTEM_ERROR,"接口响应参数不规范");//响应参数里不包含code
        if(code==ErrorCode.SYSTEM_ERROR.getCode()){
            offlineInterface(id);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口异常，即将关闭接口");
        }
        else if(code==ErrorCode.NOT_FOUND_ERROR.getCode()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"接口路径不存在");
        }
        // 其他情况正常返回结果，不用抛异常
        return invokeResult;
    }
}
```

#### 3.2 网关端：

网关层对接口调用后的`Response`进行判断，专门对接口不存在的请求进行返回值装饰，装饰成统一响应：

```java
private Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain, long interfaceInfoId, long userId) {
    try {
        ServerHttpResponse originalResponse = exchange.getResponse();
        HttpStatus statusCode = originalResponse.getStatusCode();
        // 这个好像是网关处理的状态码？...
        if (statusCode == HttpStatus.OK) {
            ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                @Override
                public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                    if (body instanceof Flux) {
                        Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                        return super.writeWith(
                            fluxBody.map(dataBuffer -> {
                                // 7. 调用成功，接口调用次数 + 1 invokeCount，拿到调用成功的 请求头和返回头
                                ServerHttpRequest invokeRequest = exchange.getRequest();
                                ServerHttpResponse 
                                    invokeResponse = exchange.getResponse();
                                try {
                                    postHandler
                                       (invokeRequest, 
                                        invokeResponse, 
                                        interfaceInfoId, 
                                        userId);
                                } catch (Exception e) {
                                    log.error("invokeCount error", e);
                                }
                                byte[] orgContent = 
                                    new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(orgContent);
                                DataBufferUtils.release(dataBuffer);//释放掉内存
                                // 构建日志
                                String data =
                                    new String(orgContent, StandardCharsets.UTF_8); 
                                log.info("原始响应结果：" + data);
                                if (
                                    invokeResponse.getStatusCode() 
                                    	== HttpStatus.NOT_FOUND) {
                                    data = String.format
                                        ("{\"code\": %d,\"msg\":\"%s\",\"data\":\"%s\"}",
                                                         
                                         ErrorCode.NOT_FOUND_ERROR.getCode(), 
                                         "接口请求路径不存在", "null");
                                    log.info("响应结果：" + data);
                                }
                                DataBufferFactory bufferFactory 
                                    = invokeResponse.bufferFactory();
                                // log.info("date长度：{}",data.length());
                                // 打印日志
                                byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
                                // 告知客户端Body的长度，
                                //如果不设置的话客户端会一直处于等待状态不结束
                                HttpHeaders headers = invokeResponse.getHeaders();
                                headers.setContentLength(bytes.length);
                                return bufferFactory.wrap(bytes);
                            })
                        );
                    } else {
                        // 8. 调用失败，返回规范的错误码
                        log.error("<--- {} 响应code异常", getStatusCode());
                    }
                    return super.writeWith(body);
                }
            };

            // 流量染色，只有染色数据才能被调用
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header(DYE_DATA_HEADER, DYE_DATA_VALUE)
                .build();

            ServerWebExchange serverWebExchange = exchange.mutate()
                .request(modifiedRequest)
                .response(decoratedResponse)
                .build();
            return chain.filter(serverWebExchange);
        }
        //降级处理返回数据
        return chain.filter(exchange);
    } catch (Exception e) {
        log.error("网关处理异常响应.\n" + e);
        return chain.filter(exchange);
    }
}
```

#### 3.3 接口层：

接口层，使用全局异常处理器，返回所有异常的统一响应：

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "接口内部异常");
    }
}
```

#### 3.4 流程图：

####  ![image-20230803222319726](API接口开放平台笔记.assets/image-20230803222319726.png)

呵呵，真复杂阿。

#### 3.5 重测接口异常自动下线功能：

![image-20230803223900588](API接口开放平台笔记.assets/image-20230803223900588.png)

![image-20230803224012288](API接口开放平台笔记.assets/image-20230803224012288.png)

![image-20230803224027900](API接口开放平台笔记.assets/image-20230803224027900.png)

#### 3.6：是否还需要使用原始参数重测：

需要，在参数错误的时候还是需要开子线程异步使用原始参数进行测试。

防止恶意者上线接口后，修改了他的接口参数，造成本平台大量接口不可用，这时候使用原始参数重测便能知道是否是接口问题。

那么，由于接口层有了全局异常响应，网关接收的状态码都是ok，我们怎么区分接口层的参数错误和内部错误呢？

还是老规矩，网关层不要做太多业务，所以应该是接口层自己进行参数校验，然后抛出参数错误的异常。这样如果抛出的是内部异常，那就直接关闭，如果抛出的是参数错误，那就饶他一命，所以其实还是不需要用原始参数重测的。

![image-20230803232042540](API接口开放平台笔记.assets/image-20230803232042540.png)

前端的话，则需要完善参数输入的方式，帮助用户尽量不输错参数。

#### 3.7 逻辑图：

只有系统内部错误需要关闭接口。

![image-20230803231722291](API接口开放平台笔记.assets/image-20230803231722291.png)

#### 3.8 反思总结：

我在浪费时间。其实应该不做的。因为真有恶意的人，可以返回统一错误响应，这时候接口调用是正常，但是不可用！没法子了。

### 4、接口安全性之解决办法2：

定期清理长期关闭的接口，进行逻辑删除

每隔10天查询，一般查数据库要查定时间隔的两到三倍，防止定时任务失败，之前该删的没删。

```java
@Component
@Slf4j
public class DeleteExpireAPI {

    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;
    @Resource
    private InterfaceInfoService interfaceInfoService;


    /**
     * 每10天执行一次
     */
    @Scheduled(cron = "0 0 0 */10 * ?")
    public void run() {
        // 查询近 30 天内的数据
        Date thirtyDaysAgoDate = new Date(new Date().getTime() - 30 * 24 * 60 * 60 * 1000L);
        List<InterfaceInfo> interfaceInfoList = interfaceInfoMapper.listInterfaceInfoWithDelete(thirtyDaysAgoDate);
        if (CollectionUtils.isEmpty(interfaceInfoList)) {
            log.info("no Expire API");
            return;
        }
        final int pageSize = 500;
        int total = interfaceInfoList.size();
        log.info("DeleteExpireAPI start, total {}", total);
        for (int i = 0; i < total; i += pageSize) {
            int end = Math.min(i + pageSize, total);
            log.info("Delete from {} to {}", i, end);
            List<Long> ids = interfaceInfoList.stream().map(InterfaceInfo::getId).collect(Collectors.toList());
            interfaceInfoService.removeByIdsTranslator(ids.subList(i, end));
        }
        log.info("Delete end, total {}", total);
    }
}
```

暂时只想到这两个方案。

### 4.1、事务失效问题

删除接口需要删除对应的关系表，这个我之前写删除接口的时候已经实现了，不过只是单个接口的

再写一个`removeByIdsTranslator`，让它调用已经写好的函数即可，这里要特别注意Spring自调用事务失效的问题。Spring事务生效是对当前对象做了动态代理，拿到了代理对象，而直接自调用，那拿到的就是this对象，而不是代理对象。Spring很贴心的给了警告，而我以前遇到过这个问题，所以马上把所有用了@`Transactional`的方法检查了一遍，发现我文件上传的事务没注意这个问题，赶紧改正！

```java
@Override
@Transactional  //当前方法添加了事务管理
public boolean removeByIdTranslator(long id) {
    // 查询和接口名一样的用户接口关系表
    List<Long> userInterfaceInfoIdList = userInterfaceInfoService.lambdaQuery()
        .eq(UserInterfaceInfo::getInterfaceInfoId, id).list()
        .stream().map(UserInterfaceInfo::getId).collect(Collectors.toList());
    boolean b = this.removeById(id);
    if(b){
        return userInterfaceInfoService.removeBatchByIds(userInterfaceInfoIdList);
    }
    return false;
}

@Override
public boolean removeByIdsTranslator(List<Long> ids) {
    for (Long id : ids) {
        InterfaceInfoService proxy = (InterfaceInfoService) AopContext.currentProxy();
        proxy.removeByIdTranslator(id);
    }
    return true;
}
```

`AopContext`需要依赖aspectjweaver，此外还要在启动类加注解，鱼皮已经加过了。

```xml
<!--处理事务-->
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
</dependency>
```

### 4.2、sql语句

要求，接口关闭，未删除，

今天减去更新时间要大于十天，也就是关闭10天以上的接口，这些数据全都要查出来

```sql
<select id="listInterfaceInfoWithDelete" resultType="com.Reflux.ReApi.model.entity.InterfaceInfo">
        select *
        from interface_info
        /*delete == 0 代表未删除，status == 0 代表已关闭，并且更新时间要*/
        where (isDelete = 0 AND status = 0) AND updateTime <![CDATA[ <= ]]> #{tenDaysAgoDate};
    </select>
```

### 4.3、测试：

#### sql测试

现在设置了两条接口状态关闭，更新时间距离今天大于10天，并且未删除的数据

![image-20230729191016915](API接口开放平台笔记.assets/image-20230729191016915.png)

执行sql语句，成功

![image-20230729191112884](API接口开放平台笔记.assets/image-20230729191112884.png)

#### 定时任务测试

将定时任务修改为60s一次进行测试，完美完成任务：

![image-20230729191631387](API接口开放平台笔记.assets/image-20230729191631387.png)

### 3.4、一点点建议：

三个模块一定要都引入`dev`热更新的依赖，不然每次都重启三个项目，等半天。

另外，这个项目我建议最好先看一遍鱼皮的视频再把一些细节推敲一下再做。

不然，难道跟着视频把坑再踩一遍吗？尤其是把Common类分出来那部分，分代码真的特别特别累...

做项目一定要有整体的把握。

## 后续计划：

1. 完成redis判断密钥的改造
2. 完成API开放平台的微服务改造，接入全聚合搜索的接口！
3. 完成上线
4. 删除接口时，同步删除了用户接口表对应信息，但是没通知用户
5. 用户忘记密码
9. 其实我已经整吐了，很长一段时间不想动了

# 部署

1、使用宝塔下载mysql、redis、nacos。

修改主类的配置，添加VM选项如下，表示使用prod的配置文件，连接服务器mysql和nacos：

```sh
-Dspring.profiles.active=prod
```

启动主类：

## dubbo神经报错：

```
Failed to unregister dubbo
```

连接本地nacos，成功，😂

问题已经很清楚了，dubbo不能从一个nacos中注销然后映射到另一个nacos。

完全不清楚怎么修改。唉。

## 改造成feign：

没法子了，换feign吧。

让我们先来想想整个项目的框架：

gateway：微服务的网关，需要把请求导向admin和interface，同时为了避免在网关写太多业务，需要调用admin的方法，来降低耦合度，比如查询接口剩余次数

admin：管理接口端，提供服务给网关

interface：提供接口端，需要使用微服务调用其他项目接口，比如聚合搜索，并提供服务给网关

可以看到网关和接口提供端是需要调用别人的，那么需要引入下面依赖，包含负载均衡和feign调用优化的依赖：

而admin就只需要引入nacos服务发现依赖，让别人发现自己。

```xml
<!--nacos服务发现依赖-->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
<!--用于远程调用其他微服务提供的接口-->
<!--feign-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
<!--负载均衡-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-loadbalancer</artifactId>
</dependency>
<!--httpClient的依赖，改变feign底层客户端实现，使用连接池 -->
<dependency>
    <groupId>io.github.openfeign</groupId>
    <artifactId>feign-httpclient</artifactId>
</dependency>
```

网关配置如下，对管理端和接口提供端使用负载均衡和优先本地集群，并且配置feign的连接和日志（接口端也要配这个）：

```yml
server:
  port: 8090
spring:
  cloud:
    gateway:
      default-filters:
        - AddResponseHeader=source, reflux
      routes:
        - id: interface
          #uri: http://localhost:8123 #路由的目标地址 http就是固定地址
          uri: lb://interface_manager # 路由的目标地址 lb就是负载均衡，后面跟服务名称
          predicates:
            - Path=/interface/** #只要符合/interface导向interface端
        - id: admin
          #uri: http://localhost:8123 #路由的目标地址 http就是固定地址
          uri: lb://ReApi_admin
          predicates:
            - Path=/admin/** #把符合/admin的导向服务器admin
    nacos:
      server-addr: 
      discovery:
        cluster-name: CS # 集群名称
  redis:
    host: 
    port: 
    password: 
    database: 
interface_manager: # 给某个微服务配置负载均衡规则，这里是interface_manager服务
  ribbon:
    NFLoadBalancerRuleClassName: com.alibaba.cloud.nacos.ribbon.NacosRule # 负载均衡规则，优先本地集群
ReApi_admin:
  ribbon:
    NFLoadBalancerRuleClassName: com.alibaba.cloud.nacos.ribbon.NacosRule # 负载均衡规则，优先本地集群

feign:
  client:
    config:
      default: # default全局的配置
        loggerLevel: BASIC # 日志级别，BASIC就是基本的请求和响应信息
  httpclient:
    enabled: true # 开启feign对HttpClient的支持
    max-connections: 200 # 最大的连接数
    max-connections-per-route: 50 # 每个路径的最大连接数

```

最后就是把feign的接口调用抽取出来，把feign调用所需的pojo和默认的Feign配置都放到这个模块中，提供给所有消费者使用。记住，原则就是只抽取一两个pojo的的模块，不要删了pojo引入feign接口的依赖！这样虽然代码不重复了，但耦合度提高了！！！

最后就是由于下面这几个接口都是本地调用，没有专门封装request，偷懒直接一把梭了（鱼皮狂怒）

```java
@FeignClient("ReApi_admin")
public interface ReApi_admin_client {
    /**
     * - 声明信息
     * - 服务名称：
     * - 请求方式：
     * - 请求路径：
     * - 请求参数：
     * - 返回值类型：
     */

    @GetMapping("/inner/get/invokeUser")
    User getInvokeUser(String accessKey);

    /**
     * 修改：用户调用接口后，增加接口调用次数，减少接口剩余调用次数
     *
     * @param interfaceInfoId
     * @param userId
     * @return
     */

    @PutMapping("/inner/put/invokeCount")
    boolean invokeCount(long interfaceInfoId, long userId);

    /**
     * 查询用户剩余调用次数
     *
     * @param interfaceId
     * @param userId
     * @return
     */
    @GetMapping("/inner/get/leftNum")
    UserInterfaceInfo hasLeftNum(Long interfaceId, Long userId);

    /**
     * 用于调用接口没有关系表时添加默认关系
     *
     * @param interfaceId
     * @param userId
     * @return
     */
    @PutMapping("/inner/put/defaultUserInterfaceInfo")
    Boolean addDefaultUserInterfaceInfo(Long interfaceId, Long userId);


    /**
     * 通过id，url，method，path查询接口
     *
     * @param id
     * @param url
     * @param method
     * @param path
     * @return
     */
    @GetMapping("/inner/get/interfaceInfo")
    InterfaceInfo getInterfaceInfo(long id, String url, String method, String path);

```

在网关注入依赖就可以使用了，注意实体类都要引入FeignApi，别引成common的：

```java
@Resource
private ReApi_admin_client reApi_admin_clinet;

// 使用分布式Redis锁和Redis防重放和接口统计
private void postHandler(ServerHttpRequest request, ServerHttpResponse response, Long interfaceInfoId, Long userId) {
    RLock lock = redissonClient.getLock("api:add_interface_num");
    if (response.getStatusCode() == HttpStatus.OK) {
        CompletableFuture.runAsync(() -> {
            if (lock.tryLock()) {
                try {
                    addInterfaceNum(request, interfaceInfoId, userId);
                } finally {
                    lock.unlock();
                }
            }
        });
    }
}

private void addInterfaceNum(ServerHttpRequest request, Long interfaceInfoId, Long userId) {
    // 使用分布式锁实现接口总调用次数的增加
    String nonce = request.getHeaders().getFirst(APIHeaderConstant.NONCE);
    if (StringUtil.isEmpty(nonce)) {
        throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "请求重复");
    }
    UserInterfaceInfo userInterfaceInfo = reApi_admin_clinet.hasLeftNum(interfaceInfoId, userId);
    // 接口未绑定用户，这里是不能完全信任前端，有可能客户未开通接口也调用？或者是管理员不开通直接调用？
    if (userInterfaceInfo == null) {
        Boolean save = reApi_admin_clinet.addDefaultUserInterfaceInfo(interfaceInfoId, userId);
        if (save == null || !save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口绑定用户失败！");
        }
    }
    if (userInterfaceInfo != null && userInterfaceInfo.getLeftNum() <= 0) {
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "调用次数已用完！");
    }
    String key = KEY_PREFFIX+nonce;
    redisTemplate.opsForValue().set(key, 1, 5, TimeUnit.MINUTES);
    reApi_admin_clinet.invokeCount(interfaceInfoId, userId);
}
```

## feign踩坑：

一坑还比一坑高，在微服务场景下，服务间的调用可以通过`feign`的方式，但这里的问题是，网关是reactor模式，即异步调用模式，而feign调用为同步方式，这里直接通过feign调用会报错。

![image-20230802195959496](API接口开放平台笔记.assets/image-20230802195959496.png)

而dubbo没有这个问题，Dubbo是基于NIO的非阻塞实现并行调用，客户端不需要启动多线程即可完成并行调用多个远程服务，相对多线程开销较小。异步调用会返回一个Future对象。

第一个解决办法就是把feign调用封装，采用线程池，或者`CompletableFuture.runAsync`。

第二个办法就是去引一些小众依赖，已经实现了异步feign的封装依赖（github的星星很少）

成功几率不高。最好不在网关用feign吧，这是个教训。

## 解决dubbo神经报错

其实神经不是dubbo，当时我真的很绝望，我甚至去搂了一眼源码，看dubbo是怎么把yml的配置装配的。

结果机缘巧合之下，我发现：

![image-20230802222658194](API接口开放平台笔记.assets/image-20230802222658194.png)

瞌睡碰到枕头，正被怎么把feign变成异步搞得焦头烂额，结果发现之前难以解决的问题竟然是这样。

又一次诠释了会者不难难者不会的道理。端口！！！

我只是保持一个好习惯，现在本地连通线上的所有软件再部署到线上，毕竟要是本地跑不通，部署上去也不可能跑成功，结果却输得那么彻底，这一天真的过的无比绝望，幸好天无绝人之路，特在这记下，帮助下一个受伤的小朋友。

舒服了：

![image-20230802223246710](API接口开放平台笔记.assets/image-20230802223246710.png)

补充一下Docker部署会遇到的坑：https://blog.csdn.net/xieqj_0511/article/details/126891171

## 补充feign的小知识：

2、常规的feign调用是不经过网关的，这里记录一下，虽然本次没用到。

如果希望服务间调用也能够进入网关处理逻辑，比如验证，token验证或者需要经过网关过滤器处理，该如何实现呢？可以用`contextId`：

实现客户的请求通过网关访问其他服务：

```java
@FeignClient(contextId = "[唯一的名称]",name="网关服务名",fallback =xxx .class)
public interface Hello {
    @GetMapping("[其他服务名]/[具体的url]")
    String Hello(){
    }
}
```

## 改造微服务：

1、注意事项，服务名称不要用下划线，不然gateway会无法识别服务

```java
uri: lb://interface-manager
```

2、需要引入负载均衡依赖，才能实现上面根据服务名负载均衡

```xml
<!--负载均衡-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-loadbalancer</artifactId>
</dependency>
```

3、如果想要让网关成为一切微服务的门口，也就是前端先访问网关，再访问管理端，跨域处理需要放在网关！

总之就是前端先访问哪个模块，就在哪个模块做跨域处理。否则网关直接把options请求给拒了，哈哈。

## 后端部署：

将项目文件打包成jar包，由于使用了maven子父依赖，很方便全打包好了。

打包不成功就跳过测试单元。

打成jar包后运行命令加下面一行，指定为生成环境

```sh
--spring.profiles.active=prod
```

哇，这这么耗内存的吗？？？4G也抗不住造

![image-20230804095332626](API接口开放平台笔记.assets/image-20230804095332626.png)

查看nacos状态，腾讯云直接把它给杀掉了。

```sh
ps -ef | grep nacos
```

试图修改启动时的JVM参数：

- -Xmx1024M表示设置最大堆内存为1024M（即1G），这个值决定了java虚拟机能够使用的最大内存，如果超过这个值，会导致OutOfMemoryError异常。
- -Xms256M表示设置最小堆内存为256M，这个值决定了java虚拟机启动时分配给堆内存的初始大小，如果太小，会导致频繁的垃圾回收，影响性能。

这两个参数的推荐值取决于您运行的jar包的具体情况，例如jar包的大小，功能，依赖等。一般来说，您可以参考以下几个原则：

- 最大堆内存和最小堆内存应该尽量接近，避免堆内存频繁扩展或收缩。
- 最大堆内存不应该超过物理内存的80%，避免影响其他程序或系统的运行。
- 最小堆内存不应该低于jar包本身的大小，避免加载不完整或出错。

由于admin比较重要（jar包也超过了128），所以保持不动，网关和接口改为 -Xmx 512M 和 -Xms 128M试试。

![image-20230804112137328](API接口开放平台笔记.assets/image-20230804112137328.png)

## 前端部署：

参考哒这位大佬的，一把跑成功

```nginx
location / {   
	root   /root/services/api-frontend;(修改的地方，我这里将dist目录重命名为项目名了)   
	index  index.html index.htm;
	try_files $uri $uri/ /index.html;(会找遍你的root多级目录，如果没有配置只会在根目录中寻找，必须配置)
}
```

![image-20230804100719489](API接口开放平台笔记.assets/image-20230804100719489.png)

## 小问题：

一些无关紧要的问题：

1、更新代码重新打包后，打包没有成功：

![image-20230804103526651](API接口开放平台笔记.assets/image-20230804103526651.png)

重启似乎就行了

2、部署后前端的URL，似乎会自动识别后端的端口之后的路径？是我配置了nginx的原因吗？之前本地跑的时候需要手动添加端口号后的路径：

![image-20230804104148202](API接口开放平台笔记.assets/image-20230804104148202.png)

3、redisson连接错误 Unable to init enough connections amount Only 23 from 32 were initialized

官方说是网络问题？重启好像就可以了

## 大问题：

### 1、Dubbo的Qos-server端口问题：

#### 网关报错：

经典之前还好好的：

```
[DUBBO] qos-server can not bind localhost:22222, dubbo version: 3.0.13, current host: XXX
java.net.BindException: Address already in use: bind
```

查询之后发现：

consumer（网关）启动时qos-server也是使用的默认的22222端口，但是这时候端口已经被provider（管理端）给占用了，所以才会报错的

下图为管理端，可以看到已经占用了：

![image-20230804120532997](API接口开放平台笔记.assets/image-20230804120532997.png)

#### Qos介绍如下：

![image-20230804120723859](API接口开放平台笔记.assets/image-20230804120723859.png)

#### 解决办法：

我们只需要设置网关的qosPort为另一个就行了，其他保持默认。

参考文档：https://blog.csdn.net/u012988901/article/details/84503672

这个似乎不影响启动。

### 2、接口不需要参数情况：

这个，由于为了安全性，接口是一定要填默认参数的，可如果真不需要参数呢？

如果传`""`，那么传给后端就是`""""`。

暂时没想好怎么解决，如果是get请求，传=号就可以了：

```typescript
{
    title: '接口地址',
        dataIndex: 'url',
            valueType: 'text',
                hideInTable: true,
                    hideInSearch: true,
                        formItemProps: {
                            rules: [
                                {
                                    required: true,
                                    message:"参数为空请输入=号",
                                },
                            ],
                        },
},
```

如果是post请求，请求体可以为空的话，好烦啊，不管先了。

其实也不可以直接传null，否则后端接收的参数是null，但是传给网关的确实空串，这就会导致签名失败！因为签名是使用请求参数和secretkey来签名的。

### 3、接口返回值：

咱们接口返回值只接收JSON格式，返回图片或者文件都是乱码。这个也不管了。

### 4、总结

部署需要修改nacos、redis、mysql、网关的地址、网关的白名单（!）

## 前后端联调：

踩坑1：网关和网关白名单都不需要改，直接用localhost和127.0.0.1即可。

如果改成网关ip，那么就很有可能被防火墙拦下来！（这么想，其实上线所有host都可以改回localhost）

上线部署要积极打日志，否则挂在哪一步都不知道。`log.info`把每一步重要的结果都打出来，这样才可以顺利快速调通。

踩坑2：对于调用结果，转化JSON那一步，需要使用`trycatch`，防一手接口响应数据不规范。

```java
JSONObject jsonObject;
try {
    jsonObject = JSONUtil.parseObj(invokeResult);
}catch (Exception e){
    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"接口响应参数不规范");//JSON转化失败，响应数据不是JSON格式
}
int code =(int) Optional.ofNullable(jsonObject.get("code")).orElse("-1");//要求接口返回必须是统一响应格式
ThrowUtils.throwIf(code==-1,ErrorCode.SYSTEM_ERROR,"接口响应参数不规范");//响应参数里不包含code

```

## 结果展示：

![image-20230804200221679](API接口开放平台笔记.assets/image-20230804200221679.png)

![image-20230804200245047](API接口开放平台笔记.assets/image-20230804200245047.png)

![image-20230804200300506](API接口开放平台笔记.assets/image-20230804200300506.png)

现在我们去偷个api回来

![image-20230804200422215](API接口开放平台笔记.assets/image-20230804200422215.png)

偷取接口中：

![image-20230804200646090](API接口开放平台笔记.assets/image-20230804200646090.png)

由于我的代码发布的时候会调用一次接口，所以算是偷取成功了

![image-20230804203237831](API接口开放平台笔记.assets/image-20230804203237831.png)

## 在线测试：

1、上传头像功能失败

原因：upload组件的URL地址没有更改，这个应该学习后端定义全局常量，不然频繁修改魔法值太烦了！

未完成：

1、防重放不知道怎么测试

2、多人同时调用，能否正确增加次数

3、接口层的基本参数校验



## 最后一个扩展（还没实现，下面是别人的解法）：

使用反射机制方便调用接口。

以及在线下载SDK。

```java
private Object invokeInterfaceInfo(String classPath, String methodName, String userRequestParams,
                                   String accessKey, String secretKey) {
    try {
        Class<?> clientClazz = Class.forName(classPath);
        // 1. 获取构造器，参数为ak,sk
        Constructor<?> binApiClientConstructor = clientClazz.getConstructor(String.class, String.class);
        // 2. 构造出客户端
        Object apiClient =  binApiClientConstructor.newInstance(accessKey, secretKey);

        // 3. 找到要调用的方法
        Method[] methods = clientClazz.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                // 3.1 获取参数类型列表
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 0) {
                    // 如果没有参数，直接调用
                    return method.invoke(apiClient);
                }
                Gson gson = new Gson();
                // 构造参数
                Object parameter = gson.fromJson(userRequestParams, parameterTypes[0]);
                return method.invoke(apiClient, parameter);
            }
        }
        return null;
    } catch (Exception e) {
        e.printStackTrace();
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "调用方法出错！");
    }
}

@GetMapping("/sdk")
public void getSdk(HttpServletResponse response) throws IOException {
    // 获取要下载的文件
    org.springframework.core.io.Resource resource = new ClassPathResource("api-client-sdk-0.0.1.jar");
    File file = resource.getFile();

    // 设置响应头
    response.setContentType("application/octet-stream");
    response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());

    // 将文件内容写入响应
    try (InputStream in = new FileInputStream(file);
         OutputStream out = response.getOutputStream()) {
        byte[] buffer = new byte[4096];
        int length;
        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
        out.flush();
    } catch (IOException e) {
        // 处理异常
        e.printStackTrace();
    }
}
```



