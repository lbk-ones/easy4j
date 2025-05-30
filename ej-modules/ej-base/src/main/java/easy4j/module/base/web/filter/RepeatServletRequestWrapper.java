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
package easy4j.module.base.web.filter;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

/**
 * RepeatServletRequestWrapper
 * 构建可重复读取的http请求流
 *
 * @author bokun.li
 */
public class RepeatServletRequestWrapper extends HttpServletRequestWrapper {
    private final byte[] requestBody; // 缓存请求体

    public RepeatServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        // 1. 读取并缓存请求体
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 5];
        int bytesRead;
        try (InputStream inputStream = request.getInputStream()) {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
        }
        requestBody = baos.toByteArray();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        // 2. 返回基于缓存的输入流，允许重复读取
        final ByteArrayInputStream bis = new ByteArrayInputStream(requestBody);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return bis.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int read() throws IOException {
                return bis.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        // 3. 返回基于缓存的 Reader
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    // 可选：直接获取请求体字符串
    public String getRequestBody() {
        return new String(requestBody);
    }
}