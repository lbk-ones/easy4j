package easy4j.infra.context.api.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import easy4j.infra.context.api.user.UserContext;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class BaseReq implements Serializable {


    @JsonIgnore
    @Transient
    private transient UserContext userInfo;

}
