package easy4j.infra.context;

public interface AutoRegisterContext {


    void registerToContext(Easy4jContext easy4jContext);

    default String getName() {
        return this.getClass().getName();
    }

}
