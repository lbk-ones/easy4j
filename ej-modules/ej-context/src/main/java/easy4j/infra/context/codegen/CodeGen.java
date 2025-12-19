package easy4j.infra.context.codegen;

public interface CodeGen {

    void clear();

    /**
     * 返回生成文件详细路径
     */
    String gen();

}
