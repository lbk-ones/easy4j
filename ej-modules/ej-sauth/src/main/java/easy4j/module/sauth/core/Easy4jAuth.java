package easy4j.module.sauth.core;

import easy4j.module.sauth.user.BaseUser;

public interface Easy4jAuth {

    void login(String username,String password);


    BaseUser getCurrentUser();

}
