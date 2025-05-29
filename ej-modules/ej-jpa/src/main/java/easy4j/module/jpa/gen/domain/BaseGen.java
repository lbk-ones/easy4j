package easy4j.module.jpa.gen.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * BaseGen
 *
 * @author bokun.li
 * @date 2025-05
 */
@Data
public class BaseGen {

    // src/main/java/club/likunkun/buildserver/service
    private String baseUrl;

    private String packageName;

    private String genDomainName;

    public List<String> importList = new ArrayList<>();

    public List<String> annotationList = new ArrayList<>();

    public List<String> lineList = new ArrayList<>();

}