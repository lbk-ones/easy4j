# Easy4j 权限模块



## 客户端与服务端为一体，且不新建用户表
```properties
easy4j.simple-auth-enable=true
easy4j.simple-auth-is-server=true
easy4j.simple-auth-user-impl-type=default
# 单体式服务不需要注册到nacos因为服务端会默认注册到nacos所以这里要改成false
easy4j.simple-auth-register-to-nacos=false
```

## 客户端与服务端为一体，新建用户表，不用默认用户表
```properties
easy4j.simple-auth-enable=true
easy4j.simple-auth-is-server=true
easy4j.simple-auth-user-impl-type=extra
# 单体式服务不需要注册到nacos因为服务端会默认注册到nacos所以这里要改成false
easy4j.simple-auth-register-to-nacos=false
```

## 客户端与服务端为分体部署，且不新建用户表

> 分体部署必须满足是sca环境且使用nacos作为注册中心

### 服务端
```properties
easy4j.simple-auth-enable=true
easy4j.simple-auth-is-server=true
easy4j.simple-auth-user-impl-type=default

```
### 客户端
```properties
easy4j.simple-auth-enable=true
easy4j.simple-auth-is-server=false
```

## 会话存储
> 会话默认自动建立表来存储token,也可以选择使用redis来存储会话token
```properties

easy4j.simple-auth-session-storage-type=redis

# 会话过期时间 默认三个小时
easy4j.session-expire-time-seconds=10800

# 会话刷新剩余时间，秒为单位，默认十分钟
easy4j.session-refresh-time-remaining=600

```

## 服务端对接外部用户
> 写在服务端里面
```java
/**
 * 不使用内部默认用户表 使用外部表 则实现接口LoadUserByDb来供服务端查询用户信息
 */
@Component
public class AuthQueryDbUser implements LoadUserByDb {

    // 举个栗子 可以不是这个
    @Autowired
    SysUserMapper sysUserMapper;

    // 将查出来的外部用户信息SysUser 转为SecurityUser、SecurityUser类实现了ISecurityEasy4jUser接口
    @Override
    public ISecurityEasy4jUser loadUserByUserName(String username) {
        LambdaQueryWrapper<SysUser> sysUserLambdaQueryWrapper = new LambdaQueryWrapper<>(SysUser.class);
        sysUserLambdaQueryWrapper.eq(SysUser::getUserCode,username);
//        sysUserLambdaQueryWrapper.eq(SysUser::getIsEnabled,"1");
        SysUser sysUser = sysUserMapper.selectOne(sysUserLambdaQueryWrapper);
        if(null!=sysUser){
            String isEnabled = sysUser.getIsEnabled();
            if(!StrUtil.equals(isEnabled,"1")){
                throw new EasyException(I18nAuthConstant.B00003);
            }
            return SysUser2Easy4jBo.toSysUser2Easy4jBo(sysUser);
        }
        return null;
    }
    // 根据用户id来查询,权限模块的设计：用户ID是不对外的而username可以理解为账号
    // 最后还是要转为 SecurityUser
    @Override
    public ISecurityEasy4jUser loadUserByUserId(long userId) {
        SysUser sysUser = sysUserMapper.selectById(userId);
        if(null!= sysUser){
            return SysUser2Easy4jBo.toSysUser2Easy4jBo(sysUser);
        }
        return null;
    }
}

```

## 服务端外部权限对接
> 还是针对服务端来做的
```java
/**
 * 对接外部权限
 */
@Component
public class AuthLoadAuthorityBy implements LoadAuthorityBy {


    @Autowired
    AuthMapper authMapper;
    // 权限大体组成:用户>角色>菜单>按钮
    @Override
    public Set<SecurityAuthority> loadSecurityAuthoritiesByUsername(String userName) {

        List<SecurityAuthority> authorities =  authMapper.selectAuthorityByUserName(userName);
        // load user
        return new HashSet<>(authorities);
    }

}

```

## 接口内用户/权限信息获取方式
```text
// 获取在线用户名
OnlineUserInfo onlineUser = Easy4jAuth.getOnlineUser();
// 用户信息
ISecurityEasy4jUser userInfo = onlineUser.getUser();
// 权限信息
Set<SecurityAuthority> authorityList = onlineUser.getAuthorityList();
```

## 权限token/AccessToken的获取方式
> 权限token名称默认为 X-Access-Token 前端需要在请求头里面携带这个字段
```text
// SpringMVC请求参数中可以这样获取
@RequestHeader(value = SysConstant.X_ACCESS_TOKEN) String xacessToken
// 或者直接从请求头中获取 SysConstant.X_ACCESS_TOKEN
// 或者从在线用户信息里面获取
```

## 服务端认证授权（登录，退出登录，授权）
