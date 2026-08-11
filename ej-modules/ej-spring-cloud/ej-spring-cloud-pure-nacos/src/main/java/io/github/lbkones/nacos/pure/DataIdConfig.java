package io.github.lbkones.nacos.pure;

import lombok.Getter;

import java.util.Objects;

/**
 * 这个配置类是从nacos搬运过来的
 * @since 2.1.6
 */
@Getter
public class DataIdConfig {

    /**
     * the data id of extended configuration.
     */
    private String dataId;

    /**
     * the group of extended configuration, the default value is DEFAULT_GROUP.
     */
    private String group = "DEFAULT_GROUP";

    /**
     * whether to support dynamic refresh, the default does not support .
     */
    private boolean refresh = false;

    public DataIdConfig() {
    }

    public DataIdConfig(String dataId) {
        this.dataId = dataId;
    }

    public DataIdConfig(String dataId, String group) {
        this(dataId);
        this.group = group;
    }

    public DataIdConfig(String dataId, boolean refresh) {
        this(dataId);
        this.refresh = refresh;
    }

    public DataIdConfig(String dataId, String group, boolean refresh) {
        this(dataId, group);
        this.refresh = refresh;
    }

    public DataIdConfig setDataId(String dataId) {
        this.dataId = dataId;
        return this;
    }

    public DataIdConfig setGroup(String group) {
        this.group = group;
        return this;
    }

    public DataIdConfig setRefresh(boolean refresh) {
        this.refresh = refresh;
        return this;
    }

    @Override
    public String toString() {
        return "Config{" + "dataId='" + dataId + '\'' + ", group='" + group + '\''
                + ", refresh=" + refresh + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DataIdConfig config = (DataIdConfig) o;
        return refresh == config.refresh && Objects.equals(dataId, config.dataId)
                && Objects.equals(group, config.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataId, group, refresh);
    }

}