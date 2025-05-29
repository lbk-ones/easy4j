package easy4j.module.seed.leaf;


/**
 * LeafGenIdService
 *
 * @author bokun.li
 * @date 2025-05
 */
public interface LeafGenIdService {
    Long get(String key);

    boolean init();

}