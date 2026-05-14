package io.github.lbkones.config.api;

public class ConfigCenterFactory {

    private volatile static ConfigCenter configCenter = null;

    public static ConfigCenter get(){
        if(configCenter == null){
            synchronized (ConfigCenterFactory.class){
                if(configCenter == null){
                    configCenter = new DefaultConfigCenter();
                }
            }
        }
        return configCenter;
    }

}
