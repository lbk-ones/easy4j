package easy4j.module.base.plugin.gen;

import lombok.Data;

import java.util.Objects;

@Data
public class JavaBaseMethod {

    private String methodName;

    private String returnTypeName;

    private String params;

    @Override
    public String toString() {
        return "JavaBaseMethod{" +
                "methodName='" + methodName + '\'' +
                ", returnTypeName='" + returnTypeName + '\'' +
                ", params='" + params + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        JavaBaseMethod that = (JavaBaseMethod) object;
        return Objects.equals(methodName, that.methodName) && Objects.equals(returnTypeName, that.returnTypeName) && Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodName, returnTypeName, params);
    }
}
