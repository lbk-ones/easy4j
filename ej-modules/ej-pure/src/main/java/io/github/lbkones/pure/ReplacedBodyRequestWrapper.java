package io.github.lbkones.pure;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.http.MediaType;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ReplacedBodyRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] body;

    private final String originBody;

    public String getOriginBody() {
        return originBody;
    }

    public ReplacedBodyRequestWrapper(HttpServletRequest request, String body) {
        super(request);
        this.originBody = body;
        this.body = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 修复：使用字节流读取而不是字符流
     * 避免Tomcat在字符编码转换时出现MalformedInputException
     */
    public static String readRequestBody(HttpServletRequest request) throws IOException {
        try {
            String contentType = request.getContentType();
            // 不处理json之外的数据
            if(!StrUtil.contains(contentType, MediaType.APPLICATION_JSON_VALUE)){
                return "";
            }
            // 方式1：直接从输入流读取字节，然后手动转换为字符串
            ServletInputStream inputStream = request.getInputStream();
            // 20kb
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream,20480);
            byte[] body = bufferedInputStream.readAllBytes();

            // 获取请求的字符编码，如果没有指定则默认使用UTF-8
            String charset = request.getCharacterEncoding();
            if (charset == null || charset.isEmpty()) {
                charset = StandardCharsets.UTF_8.name();
            }

            return new String(body, charset);
        } catch (Exception e) {
            // 如果上述方式失败，尝试降级方案：尝试多种常见编码
            try {
                ServletInputStream inputStream = request.getInputStream();
                byte[] body = inputStream.readAllBytes();

                // 依次尝试UTF-8、ISO-8859-1、GBK
                try {
                    return new String(body, StandardCharsets.UTF_8);
                } catch (Exception ex1) {
                    try {
                        return new String(body, StandardCharsets.ISO_8859_1);
                    } catch (Exception ex2) {
                        return new String(body, "GBK");
                    }
                }
            } catch (Exception ex) {
                throw new IOException("Failed to read request body with multiple charset attempts", ex);
            }
        }
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream bais = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            public int read() { return bais.read(); }
            public boolean isFinished() { return bais.available() == 0; }
            public boolean isReady() { return true; }
            public void setReadListener(ReadListener l) {}
        };
    }

    @Override
    public BufferedReader getReader() {
        // 两者都走同一份数据，避免冲突
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    @Override
    public int getContentLength() { return body.length; }

    @Override
    public long getContentLengthLong() { return body.length; }
}