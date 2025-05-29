package easy4j.module.seed.leaf;



import java.util.List;

/**
 * LeafAllocDao
 *
 * @author bokun.li
 * @date 2025-05
 */
public interface LeafAllocDao {

    List<String> getAllTags();

    LeafAllocDomain updateMaxIdAndGetLeafAlloc(String key);

    LeafAllocDomain updateMaxIdByCustomStepAndGetLeafAlloc(LeafAllocDomain temp);
}