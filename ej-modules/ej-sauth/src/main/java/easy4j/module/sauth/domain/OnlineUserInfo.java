package easy4j.module.sauth.domain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.module.sauth.context.SecurityContext;
import easy4j.module.sauth.core.loadauthority.LoadAuthorityApi;
import easy4j.module.sauth.core.loaduser.LoadUserApi;
import easy4j.module.sauth.core.loaduser.LoadUserBy;
import easy4j.module.sauth.session.SessionStrategy;
import lombok.Data;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Set;

/**
 * 在线信息、用户信息、权限信息组合
 *
 * @author bokun.li
 * @date 2025-07-26
 */
@Data
public class OnlineUserInfo {

    public static final String REDIS_KEY_PREFIX_AUTHORITY = "easy4j:user:authority:";

    ISecurityEasy4jSession session;

    ISecurityEasy4jUser user;

    Set<SecurityAuthority> authorityList;

    @JsonIgnore
    public final static Handler handler = new Handler();

    public OnlineUserInfo() {
    }

    public OnlineUserInfo(ISecurityEasy4jSession session) {
        this.session = session;
    }

    public OnlineUserInfo(ISecurityEasy4jUser user) {
        this.user = user;
    }

    public OnlineUserInfo(ISecurityEasy4jSession session, ISecurityEasy4jUser user) {
        this.session = session;
        this.user = user;
    }

    public String getUserName() {
        if (null != user) {
            return user.getUsername();
        }
        return "";
    }

    public String getUserNameCn() {
        if (null != user) {
            return user.getUsernameCn();
        }
        return "";
    }

    // 权限列表获取
    public void handlerAuthorityList(String username) {
        if (CollUtil.isNotEmpty(this.authorityList)) {
            return;
        }
        // 权限可以缓存一下
        username = getUsername(username);
        boolean redisEnable = Easy4j.getProperty(SysConstant.EASY4J_REDIS_ENABLE, boolean.class);
        boolean isCache = Easy4j.getProperty(SysConstant.EASY4J_SIMPLE_AUTH_IS_CACHE_AUTHORITY, boolean.class);
        if (redisEnable && isCache) {
            try {
                Object bean = SpringUtil.getBean(SysConstant.REDIS_CACHE_MANAGER);
                if (bean instanceof CacheManager) {
                    CacheManager cacheManager = (CacheManager) bean;
                    Cache cache = cacheManager.getCache(SysConstant.PARAM_PREFIX);
                    if (null != cache) {
                        Cache.ValueWrapper valueWrapper = cache.get(REDIS_KEY_PREFIX_AUTHORITY + username);
                        if (null != valueWrapper) {
                            Object o = valueWrapper.get();
                            this.authorityList = JacksonUtil.toSet(JacksonUtil.toJson(o), SecurityAuthority.class);
                        } else {
                            Set<SecurityAuthority> authorityList1 = LoadAuthorityApi.getAuthorityList(username);
                            cache.put(REDIS_KEY_PREFIX_AUTHORITY + username, authorityList1);
                            this.authorityList = authorityList1;
                        }
                    }
                }
            } catch (Throwable e) {
                // degrade
                this.authorityList = LoadAuthorityApi.getAuthorityList(username);
            }
        } else {
            this.authorityList = LoadAuthorityApi.getAuthorityList(username);
        }


    }

    private String getUsername(String username) {
        if (StrUtil.isBlank(username)) {
            if (user != null) {
                username = user.getUsername();
            }
            if (StrUtil.isBlank(username) && session != null) {
                username = session.getUserName();
            }
        }
        return username;
    }

    // session获取
    public void handlerSession(String username) {
        if (session == null || session.isValid()) {
            username = getUsername(username);
            this.session = handler.getSessionStrategy().getSessionByUserName(username);
        }
    }


    /**
     * 内部类进行处理
     * 如果当前是客户端，且用户信息应该在服务端查询
     * 如果当前就是服务端那就应该是直接查询数据库，这一次查询出来的用户信息缓存到当前线程去
     */
    public static class Handler {

        private volatile SessionStrategy sessionStrategy;
        private volatile SecurityContext securityContext;
        private volatile LoadUserBy loadUserByUserName;

        public SessionStrategy getSessionStrategy() {
            if (sessionStrategy == null) {
                synchronized (Handler.class) {
                    if (sessionStrategy == null) {
                        sessionStrategy = SpringUtil.getBean(SessionStrategy.class);
                    }
                }
            }
            return sessionStrategy;
        }

        public SecurityContext getSecurityContext() {
            if (securityContext == null) {
                synchronized (Handler.class) {
                    if (securityContext == null) {
                        securityContext = SpringUtil.getBean(SecurityContext.class);
                    }
                }
            }
            return securityContext;
        }

        public LoadUserBy getLoadUserByUserName() {
            if (loadUserByUserName == null) {
                synchronized (Handler.class) {
                    if (loadUserByUserName == null) {
                        loadUserByUserName = SpringUtil.getBean(LoadUserBy.class);
                    }
                }
            }
            return loadUserByUserName;
        }

        /**
         * 根据用户ID查询用户信息
         *
         * @author bokun.li
         * @date 2025-07-26
         */
        public ISecurityEasy4jUser getOnlineUserInfoByUserId(long userId) {

            return LoadUserApi.getUserByUserId(userId);
        }

        /**
         * 根据用户名查询用户信息
         *
         * @author bokun.li
         * @date 2025-07-26
         */
        public ISecurityEasy4jUser getOnlineUserInfoByUserName(String username) {

            return LoadUserApi.getByUserName(username);
        }


    }


}
