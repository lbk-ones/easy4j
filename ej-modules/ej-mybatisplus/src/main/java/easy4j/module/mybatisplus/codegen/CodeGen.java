package easy4j.module.mybatisplus.codegen;

public interface CodeGen {

    void clear();

    /**
     * 返回生成文件详细路径
     */
    String gen(boolean isPreview, boolean isServer, ObjectValue objectValue);

}
