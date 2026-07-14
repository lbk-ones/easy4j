package io.github.lbkones.nacos.pure;

import jakarta.annotation.Resource;

public class CloudPropertiesRefreshHolder {

    public static CloudPropertiesRefresh cloudPropertiesRefresh;

    @Resource
    public void setPropertiesRefresh(CloudPropertiesRefresh cloudPropertiesRefresh) {
        CloudPropertiesRefreshHolder.cloudPropertiesRefresh = cloudPropertiesRefresh;
    }
}
