package easy4j.infra.rpc.integrated;

import cn.hutool.core.exceptions.UtilException;
import easy4j.infra.rpc.domain.RpcRequest;
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
        Object bean = this.beanFactory.getBean(this.beanName);
        IntegratedFactory.register(bean);
    }

    public  ListableBeanFactory getBeanFactory() {
        ListableBeanFactory factory = null == beanFactory ? applicationContext : (ListableBeanFactory) beanFactory;
        if (null == factory) {
            throw new UtilException("No ConfigurableListableBeanFactory or ApplicationContext injected, maybe not in the Spring environment?");
        } else {
            return (ListableBeanFactory)factory;
        }
    }

    @Override
    public Object instance(RpcRequest request) {
        String classIdentify = request.getClassIdentify();
        Class<?> classByClassIdentify = ServerMethodInvoke.getClassByClassIdentify(classIdentify);
        return getBeanFactory().getBean(classByClassIdentify);
    }
}
