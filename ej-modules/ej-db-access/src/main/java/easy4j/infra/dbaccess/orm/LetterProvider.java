package easy4j.infra.dbaccess.orm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LetterProvider {
    private List<String> letters;
    private int currentIndex;

    /**
     * 构造函数
     * @param excluded 排除的字母集合，可以为null或empty
     */
    public LetterProvider(Set<String> excluded) {
        letters = new ArrayList<>();
        currentIndex = 0;

        // 初始化排除集合
        Set<String> excludedSet = excluded != null ? excluded : new HashSet<>();

        // 添加小写字母 a-z
        for (char c = 'a'; c <= 'z'; c++) {
            if (!excludedSet.contains(c)) {
                letters.add(String.valueOf(c));
            }
        }

        // 添加大写字母 A-Z
        for (char c = 'a'; c <= 'z'; c++) {
            String c2 = c+"1";
            if (!excludedSet.contains(c2)) {
                letters.add(c2);
            }
        }
    }

    /**
     * 获取下一个字母
     * @return 下一个字母，如果已遍历完所有字母则返回null
     */
    public String next() {
        if (currentIndex >= letters.size()) {
            return null;
        }
        return letters.get(currentIndex++);
    }

    /**
     * 重置索引，从第一个字母开始
     */
    public void reset() {
        currentIndex = 0;
    }

    /**
     * 判断是否还有下一个字母
     */
    public boolean hasNext() {
        return currentIndex < letters.size();
    }

    /**
     * 获取剩余字母个数
     */
    public int remainingCount() {
        return letters.size() - currentIndex;
    }

    /**
     * 测试示例
     */
    public static void main(String[] args) {
        // 示例1：不排除任何字母
        System.out.println("示例1：不排除任何字母");
        LetterProvider provider1 = new LetterProvider(null);
        for (int i = 0; i < 5; i++) {
            System.out.println(provider1.next());
        }

        // 示例2：排除某些字母
        System.out.println("\n示例2：排除 a, B, z");
        Set<String> excluded = new HashSet<>();
        excluded.add("a");
        excluded.add("B");
        excluded.add("z");
        LetterProvider provider2 = new LetterProvider(excluded);
        
        while (provider2.hasNext()) {
            System.out.print(provider2.next() + " ");
        }
        System.out.println();

        // 示例3：重置测试
        System.out.println("\n示例3：重置后重新遍历");
        provider2.reset();
        for (int i = 0; i < 5; i++) {
            System.out.println(provider2.next());
        }
    }
}
