package easy4j.module.seed.leaf;


public interface LeafGenIdService {
    Long get(String key);

    boolean init();

}
