package easy4j.infra.context.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class NacosSauthServerRegisterEvent extends ApplicationEvent {

    private String serverName;

    public NacosSauthServerRegisterEvent(Object source, String serverName) {
        super(source);
        this.serverName = serverName;
    }


}
