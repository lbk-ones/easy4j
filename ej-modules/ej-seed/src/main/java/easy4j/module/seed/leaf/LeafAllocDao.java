package easy4j.module.seed.leaf;



import java.util.List;

public interface LeafAllocDao {

    List<String> getAllTags();

    LeafAllocDomain updateMaxIdAndGetLeafAlloc(String key);

    LeafAllocDomain updateMaxIdByCustomStepAndGetLeafAlloc(LeafAllocDomain temp);
}
