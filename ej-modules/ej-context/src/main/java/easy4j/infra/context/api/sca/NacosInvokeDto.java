/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.infra.context.api.sca;

import easy4j.infra.common.utils.EasyMap;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.Map;

@Data
@Builder
public class NacosInvokeDto {

    // 服务名称
    private String serverName;

    private String group;

    private String path;

    private Object body;

    private String accessToken;

    // HttpHeaders headers = new HttpHeaders();
    // headers.set("var1", "value1")
    private HttpHeaders httpHeaders;



    // 是否跳过header赋值，调用其他服务时候可能需要这个功能
    private boolean resetHeader;

    /**
     * @see org.springframework.http.HttpMethod
     */
    private HttpMethod method;


    /**
     * query传参，传参自动追加到query
     */
    private Map<String, Object> paramMap;

    private boolean isJson;

    private byte[] resData;


}
