package io.github.lbkones.config.api;

import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.ServiceLoaderUtils;
import easy4j.infra.common.utils.SysLog;

import java.util.List;

public class CcSpiFactory {

    public static final List<CcSpi> ccSpiList = ServiceLoaderUtils.load(CcSpi.class);

    private static volatile CcSpi ccSpi = null;

    public static CcSpi get() {
        if (ccSpi == null) {
            synchronized (CcSpiFactory.class) {
                if (ccSpi == null) {
                    ccSpi = ListTs.get(ccSpiList, 0);
                    if (null == ccSpi) {
                        ccSpi = new DefaultCcSpi();
                    }
                    String name = ccSpi.getName();
                    System.out.println(SysLog.compact("current config center is " + name));
                }
            }
        }
        return ccSpi;
    }
}
