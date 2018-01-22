---
layout: post
title: Spring Security源码分析四：Spring Social实现微信社交登录 
categories: Spring Security
description: Spring Security
keywords: Spring Security
---
> 社交登录又称作社会化登录（Social Login），是指网站的用户可以使用腾讯QQ、人人网、开心网、新浪微博、搜狐微博、腾讯微博、淘宝、豆瓣、MSN、Google等社会化媒体账号登录该网站。

## 前言 ##
在上一章[Spring-Security源码分析三-Spring-Social社交登录过程](https://longfeizheng.github.io/2018/01/09/Spring-Security%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E4%B8%89-Spring-Social%E7%A4%BE%E4%BA%A4%E7%99%BB%E5%BD%95%E8%BF%87%E7%A8%8B/)中，我们已经实现了使用`Spring Social`+`Security`的QQ社交登录。本章我们将实现微信的社交登录。（微信和QQ登录的大体流程相同，但存在一些细节上的差异，下面我们来简单实现一下）


### 准备工作
1. 熟悉[OAuth2.0协议标准](https://oauth.net/2/)，微信登录是基于[OAuth2.0中的authorization_code模式](https://tools.ietf.org/html/rfc6749#section-4.1)的授权登录；
2. [微信开放平台](https://open.weixin.qq.com/cgi-bin/frame?t=home/web_tmpl&lang=zh_CN)申请网站应用开发，获取`appid`和`appsecret`
3. 熟读[网站应用微信登录开发指南](https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1419316505&token=6505faac65c26a79bc1b0218aa8cd24c0e24bceb&lang=zh_CN)
4. 参考[Spring-Security源码分析三-Spring-Social社交登录过程](https://longfeizheng.github.io/2018/01/09/Spring-Security%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E4%B8%89-Spring-Social%E7%A4%BE%E4%BA%A4%E7%99%BB%E5%BD%95%E8%BF%87%E7%A8%8B/#%E5%87%86%E5%A4%87%E5%B7%A5%E4%BD%9C)的准备工作

为了方便大家测试，博主在某宝租用了一个月的appid和appSecret

| appid  | `wxfd6965ab1fc6adb2`  |
| ------------ | ------------ |
| appsecret  | `66bb4566de776ac699ec1dbed0cc3dd1`  |

### 目录结构
[![http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/Spring%20Security/spring_social_weixin.png](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/Spring%20Security/spring_social_weixin.png "http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/Spring%20Security/spring_social_weixin.png")](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/Spring%20Security/spring_social_weixin.png "http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/Spring%20Security/spring_social_weixin.png")

[参考](https://docs.spring.io/spring-social/docs/1.1.0.RELEASE/reference/htmlsingle/#section_creatingAProviderProject)

1. `api` 定义api绑定的公共接口
2. `config` 微信的一些配置信息
3. `connect`与服务提供商建立连接所需的一些类。


#### 定义返回用户信息接口
```java
public interface Weixin {
    WeixinUserInfo getUserInfo(String openId);
}
```

这里我们看到相对于QQ的`getUserInfo`微信多了一个参数`openId`。这是因为[微信文档](https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1419316505&token=6505faac65c26a79bc1b0218aa8cd24c0e24bceb&lang=zh_CN)中在[OAuth2.0的认证流程示意图](https://longfeizheng.github.io/2018/01/09/Spring-Security%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E4%B8%89-Spring-Social%E7%A4%BE%E4%BA%A4%E7%99%BB%E5%BD%95%E8%BF%87%E7%A8%8B/#oauth20%E7%9A%84%E8%AE%A4%E8%AF%81%E6%B5%81%E7%A8%8B%E7%A4%BA%E6%84%8F%E5%9B%BE)第五步时，微信的`openid` 同`access_token`一起返回。而`Spring Social`获取`access_token`的类`AccessGrant.java`中没有`openid`。因此我们自己需要扩展一下`Spring Social`获取令牌的类（`AccessGrant.java`）；

#### 处理微信返回的access_token类(添加openid)
```java
@Data
public class WeixinAccessGrant extends AccessGrant{

    private String openId;

    public WeixinAccessGrant() {
        super("");
    }

    public WeixinAccessGrant(String accessToken, String scope, String refreshToken, Long expiresIn) {
        super(accessToken, scope, refreshToken, expiresIn);
    }
}

```
#### 实现返回用户信息接口
```java
public class WeiXinImpl extends AbstractOAuth2ApiBinding implements Weixin {

    /**
     * 获取用户信息的url
     */
    private static final String WEIXIN_URL_GET_USER_INFO = "https://api.weixin.qq.com/sns/userinfo?openid=";

    private ObjectMapper objectMapper = new ObjectMapper();

    public WeiXinImpl(String accessToken) {
        super(accessToken, TokenStrategy.ACCESS_TOKEN_PARAMETER);
    }

    /**
     * 获取用户信息
     *
     * @param openId
     * @return
     */
    @Override
    public WeixinUserInfo getUserInfo(String openId) {
        String url = WEIXIN_URL_GET_USER_INFO + openId;

        String result = getRestTemplate().getForObject(url, String.class);
        if(StringUtils.contains(result, "errcode")) {
            return null;
        }

        WeixinUserInfo userInfo = null;

        try{
            userInfo = objectMapper.readValue(result,WeixinUserInfo.class);
        }catch (Exception e){
            e.printStackTrace();
        }

        return userInfo;
    }

    /**
     * 使用utf-8 替换默认的ISO-8859-1编码
     * @return
     */
    @Override
    protected List<HttpMessageConverter<?>> getMessageConverters() {
        List<HttpMessageConverter<?>> messageConverters = super.getMessageConverters();
        messageConverters.remove(0);
        messageConverters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
        return messageConverters;
    }
}
```
与`QQ`获取用户信息相比,`微信`的实现类中少了一步通过`access_token`获取`openid`的请求。`openid`由自己定义的扩展类`WeixinAccessGrant`中获取；

#### WeixinOAuth2Template处理微信返回的令牌信息

```java
@Slf4j
public class WeixinOAuth2Template extends OAuth2Template {

    private String clientId;

    private String clientSecret;

    private String accessTokenUrl;

    private static final String REFRESH_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/refresh_token";

    public WeixinOAuth2Template(String clientId, String clientSecret, String authorizeUrl, String accessTokenUrl) {
        super(clientId, clientSecret, authorizeUrl, accessTokenUrl);
        setUseParametersForClientAuthentication(true);
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.accessTokenUrl = accessTokenUrl;
    }

    /* (non-Javadoc)
     * @see org.springframework.social.oauth2.OAuth2Template#exchangeForAccess(java.lang.String, java.lang.String, org.springframework.util.MultiValueMap)
     */
    @Override
    public AccessGrant exchangeForAccess(String authorizationCode, String redirectUri,
                                         MultiValueMap<String, String> parameters) {

        StringBuilder accessTokenRequestUrl = new StringBuilder(accessTokenUrl);

        accessTokenRequestUrl.append("?appid="+clientId);
        accessTokenRequestUrl.append("&secret="+clientSecret);
        accessTokenRequestUrl.append("&code="+authorizationCode);
        accessTokenRequestUrl.append("&grant_type=authorization_code");
        accessTokenRequestUrl.append("&redirect_uri="+redirectUri);

        return getAccessToken(accessTokenRequestUrl);
    }

    public AccessGrant refreshAccess(String refreshToken, MultiValueMap<String, String> additionalParameters) {

        StringBuilder refreshTokenUrl = new StringBuilder(REFRESH_TOKEN_URL);

        refreshTokenUrl.append("?appid="+clientId);
        refreshTokenUrl.append("&grant_type=refresh_token");
        refreshTokenUrl.append("&refresh_token="+refreshToken);

        return getAccessToken(refreshTokenUrl);
    }

    @SuppressWarnings("unchecked")
    private AccessGrant getAccessToken(StringBuilder accessTokenRequestUrl) {

        log.info("获取access_token, 请求URL: "+accessTokenRequestUrl.toString());

        String response = getRestTemplate().getForObject(accessTokenRequestUrl.toString(), String.class);

        log.info("获取access_token, 响应内容: "+response);

        Map<String, Object> result = null;
        try {
            result = new ObjectMapper().readValue(response, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //返回错误码时直接返回空
        if(StringUtils.isNotBlank(MapUtils.getString(result, "errcode"))){
            String errcode = MapUtils.getString(result, "errcode");
            String errmsg = MapUtils.getString(result, "errmsg");
            throw new RuntimeException("获取access token失败, errcode:"+errcode+", errmsg:"+errmsg);
        }

        WeixinAccessGrant accessToken = new WeixinAccessGrant(
                MapUtils.getString(result, "access_token"),
                MapUtils.getString(result, "scope"),
                MapUtils.getString(result, "refresh_token"),
                MapUtils.getLong(result, "expires_in"));

        accessToken.setOpenId(MapUtils.getString(result, "openid"));

        return accessToken;
    }

    /**
     * 构建获取授权码的请求。也就是引导用户跳转到微信的地址。
     */
    public String buildAuthenticateUrl(OAuth2Parameters parameters) {
        String url = super.buildAuthenticateUrl(parameters);
        url = url + "&appid="+clientId+"&scope=snsapi_login";
        return url;
    }

    public String buildAuthorizeUrl(OAuth2Parameters parameters) {
        return buildAuthenticateUrl(parameters);
    }

    /**
     * 微信返回的contentType是html/text，添加相应的HttpMessageConverter来处理。
     */
    protected RestTemplate createRestTemplate() {
        RestTemplate restTemplate = super.createRestTemplate();
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
        return restTemplate;
    }
}
```
与`QQ`处理令牌类相比多了三个全局变量并且复写了`exchangeForAccess`方法。这是因为`微信`在通过`code`获取`access_token`是传递的参数是`appid`和`secret`而不是[标准](https://github.com/spring-projects/spring-social/blob/79341986fec8b663f5a3c059e61823c24765d9ec/spring-social-core/src/main/java/org/springframework/social/oauth2/OAuth2Template.java#L150)的`client_id`和`client_secret`。

#### WeixinServiceProvider连接服务提供商
```java
public class WeixinServiceProvider extends AbstractOAuth2ServiceProvider<Weixin> {

    /**
     * 微信获取授权码的url
     */
    private static final String WEIXIN_URL_AUTHORIZE = "https://open.weixin.qq.com/connect/qrconnect";
    /**
     * 微信获取accessToken的url(微信在获取accessToken时也已经返回openId)
     */
    private static final String WEIXIN_URL_ACCESS_TOKEN = "https://api.weixin.qq.com/sns/oauth2/access_token";

    public WeixinServiceProvider(String appId, String appSecret) {
        super(new WeixinOAuth2Template(appId, appSecret, WEIXIN_URL_AUTHORIZE, WEIXIN_URL_ACCESS_TOKEN));
    }

    @Override
    public Weixin getApi(String accessToken) {
        return new WeiXinImpl(accessToken);
    }
}
```

#### WeixinConnectionFactory连接服务提供商的工厂类
```java
public class WeixinConnectionFactory extends OAuth2ConnectionFactory<Weixin> {

    /**
     * @param appId
     * @param appSecret
     */
    public WeixinConnectionFactory(String providerId, String appId, String appSecret) {
        super(providerId, new WeixinServiceProvider(appId, appSecret), new WeixinAdapter());
    }

    /**
     * 由于微信的openId是和accessToken一起返回的，所以在这里直接根据accessToken设置providerUserId即可，不用像QQ那样通过QQAdapter来获取
     */
    @Override
    protected String extractProviderUserId(AccessGrant accessGrant) {
        if(accessGrant instanceof WeixinAccessGrant) {
            return ((WeixinAccessGrant)accessGrant).getOpenId();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.social.connect.support.OAuth2ConnectionFactory#createConnection(org.springframework.social.oauth2.AccessGrant)
     */
    public Connection<Weixin> createConnection(AccessGrant accessGrant) {
        return new OAuth2Connection<Weixin>(getProviderId(), extractProviderUserId(accessGrant), accessGrant.getAccessToken(),
                accessGrant.getRefreshToken(), accessGrant.getExpireTime(), getOAuth2ServiceProvider(), getApiAdapter(extractProviderUserId(accessGrant)));
    }

    /* (non-Javadoc)
     * @see org.springframework.social.connect.support.OAuth2ConnectionFactory#createConnection(org.springframework.social.connect.ConnectionData)
     */
    public Connection<Weixin> createConnection(ConnectionData data) {
        return new OAuth2Connection<Weixin>(data, getOAuth2ServiceProvider(), getApiAdapter(data.getProviderUserId()));
    }

    private ApiAdapter<Weixin> getApiAdapter(String providerUserId) {
        return new WeixinAdapter(providerUserId);
    }

    private OAuth2ServiceProvider<Weixin> getOAuth2ServiceProvider() {
        return (OAuth2ServiceProvider<Weixin>) getServiceProvider();
    }

}
```
#### WeixinAdapter将微信api返回的数据模型适配Spring Social的标准模型
```java
public class WeixinAdapter implements ApiAdapter<Weixin> {

    private String openId;

    public WeixinAdapter() {
    }

    public WeixinAdapter(String openId) {
        this.openId = openId;
    }

    @Override
    public boolean test(Weixin api) {
        return true;
    }

    @Override
    public void setConnectionValues(Weixin api, ConnectionValues values) {
        WeixinUserInfo userInfo = api.getUserInfo(openId);
        values.setProviderUserId(userInfo.getOpenid());
        values.setDisplayName(userInfo.getNickname());
        values.setImageUrl(userInfo.getHeadimgurl());
    }

    @Override
    public UserProfile fetchUserProfile(Weixin api) {
        return null;
    }

    @Override
    public void updateStatus(Weixin api, String message) {

    }
}
```

#### WeixinAuthConfig创建工厂和设置数据源

```java
@Configuration
public class WeixinAuthConfig extends SocialAutoConfigurerAdapter {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ConnectionSignUp myConnectionSignUp;

    @Override
    protected ConnectionFactory<?> createConnectionFactory() {
        return new WeixinConnectionFactory(DEFAULT_SOCIAL_WEIXIN_PROVIDER_ID, SecurityConstants.DEFAULT_SOCIAL_WEIXIN_APP_ID,
                SecurityConstants.DEFAULT_SOCIAL_WEIXIN_APP_SECRET);
    }

    @Override
    public UsersConnectionRepository getUsersConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator) {
        JdbcUsersConnectionRepository repository = new JdbcUsersConnectionRepository(dataSource,
                connectionFactoryLocator, Encryptors.noOpText());
        if (myConnectionSignUp != null) {
            repository.setConnectionSignUp(myConnectionSignUp);
        }
        return repository;
    }

    /**
     * /connect/weixin POST请求,绑定微信返回connect/weixinConnected视图
     * /connect/weixin DELETE请求,解绑返回connect/weixinConnect视图
     * @return
     */
    @Bean({"connect/weixinConnect", "connect/weixinConnected"})
    @ConditionalOnMissingBean(name = "weixinConnectedView")
    public View weixinConnectedView() {
        return new SocialConnectView();
    }

}

```
#### 社交登录配置类

由于社交登录都是通过`SocialAuthenticationFilter`过滤器拦截的，如果 [上一章](https://longfeizheng.github.io/2018/01/09/Spring-Security%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E4%B8%89-Spring-Social%E7%A4%BE%E4%BA%A4%E7%99%BB%E5%BD%95%E8%BF%87%E7%A8%8B/#socialconfig-%E7%A4%BE%E4%BA%A4%E9%85%8D%E7%BD%AE%E4%B8%BB%E7%B1%BB) 已经配置过，则本章不需要配置。

效果如下：

[![http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/Spring%20Security/weixin.gif](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/Spring%20Security/weixin.gif "http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/Spring%20Security/weixin.gif")](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/Spring%20Security/weixin.gif "http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/Spring%20Security/weixin.gif")

## 代码下载 ##
从我的 github 中下载，[https://github.com/longfeizheng/logback](https://github.com/longfeizheng/logback)










