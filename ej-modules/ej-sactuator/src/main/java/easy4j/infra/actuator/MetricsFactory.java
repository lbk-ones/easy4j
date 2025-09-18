package easy4j.infra.actuator;

public class MetricsFactory {
    private static final MetricsBuilder metricsBuilder = new MetricsBuilderImpl();

    public static MetricsBuilder getInstance(){
        return metricsBuilder;
    }

}
