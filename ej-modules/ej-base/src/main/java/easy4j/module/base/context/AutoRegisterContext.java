package easy4j.module.base.context;

public interface AutoRegisterContext {


    void registerToContext(Easy4jContext easy4jContext);

    default String getName() {
        return this.getClass().getName();
    }

}
