package easy4j.infra.context.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class NacosServicesRegisterEvent extends ApplicationEvent {

    private String serverName;

    private String group;

    private Integer port;

    private String ipAddr;


    public NacosServicesRegisterEvent(Object source, String serverName, String group,Integer port) {
        super(source);
        this.serverName = serverName;
        this.group  = group;
        this.port  = port;
    }


}
