package io.github.lbkones.config.api;

import easy4j.infra.base.resolve.StandAbstractEasy4jResolve;

import java.util.Map;

public abstract class AbstractCcSpi extends StandAbstractEasy4jResolve implements CcSpi {
    public Map<String, String> bootParameters;

    public ConfigChange configChange;

    @Override
    public void setBootParameters(Map<String, String> bootParameters) {
        this.bootParameters = bootParameters;
    }

    @Override
    public void subscribe(ConfigChange configChange) {
        if(this.configChange==null) this.configChange = configChange;
    }

}
