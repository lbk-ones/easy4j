package easy4j.module.mybatisplus.codegen.servlet;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PackageRes implements Serializable {

    private List<String> allDtos;
    private List<String> allEntitys;
}
