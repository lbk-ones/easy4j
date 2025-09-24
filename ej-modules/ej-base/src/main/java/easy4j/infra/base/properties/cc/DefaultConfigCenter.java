package easy4j.infra.base.properties.cc;

import easy4j.infra.base.starter.env.Easy4j;

public class DefaultConfigCenter extends AbstractConfigCenter{
    public DefaultConfigCenter() {
        super();
    }

    @Override
    public String defaultGet(String key) {
        return Easy4j.getProperty(key);
    }
}
