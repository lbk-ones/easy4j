package easy4j.module.jpa.gen.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BaseGen {

    // src/main/java/club/likunkun/buildserver/service
    private String baseUrl;

    private String packageName;

    public List<String> importList = new ArrayList<>();

    public List<String> annotationList = new ArrayList<>();

    public List<String> lineList = new ArrayList<>();

}
