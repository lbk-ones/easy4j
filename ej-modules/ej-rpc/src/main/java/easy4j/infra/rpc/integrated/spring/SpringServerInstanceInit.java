package easy4j.infra.rpc.integrated.spring;

import cn.hutool.core.exceptions.UtilException;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.integrated.IntegratedFactory;
import easy4j.infra.rpc.integrated.ServerInstanceInit;
import easy4j.infra.rpc.server.ServerMethodInvoke;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * spring整合配置
 *
 * @author bokun
 * @since 2.0.1
 */
@Slf4j
public class SpringServerInstanceInit implements BeanNameAware, ServerInstanceInit, ApplicationContextAware, BeanFactoryAware {

    private ApplicationContext applicationContext;
    private BeanFactory beanFactory;
    private String beanName;

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }


    public ListableBeanFactory getBeanFactory() {
        ListableBeanFactory factory = null == beanFactory ? applicationContext : (ListableBeanFactory) beanFactory;
        if (null == factory) {
            throw new UtilException("No ConfigurableListableBeanFactory or ApplicationContext injected, maybe not in the Spring environment?");
        } else {
            return (ListableBeanFactory) factory;
        }
    }

    @Override
    public Object instance(RpcRequest request) {
        ListableBeanFactory beanFactory1 = getBeanFactory();
        String serviceName = request.getServiceName();
        Object instance = null;
        try {
            String classIdentify = request.getInterfaceIdentify();
            Class<?> classByClassIdentify = ServerMethodInvoke.getClassByClassIdentify(classIdentify);
            if (classByClassIdentify != null && !ClassUtil.isBasicType(classByClassIdentify)) {
                String serverName = IntegratedFactory.getConfig().getServer().getServerName();
                if (!StrUtil.equals(serviceName, serverName)) {
                    instance = beanFactory1.getBean(serviceName, classByClassIdentify);
                } else {
                    instance = beanFactory1.getBean(classByClassIdentify);
                }
            }
        } catch (Exception ignored) {
        }
        return instance;
    }
}
