package easy4j.module.jpa.gen;



public class GenTest {
    public static void test(String[] args) throws Exception {
        ConfigJpaGen build = new ConfigJpaGen()
//                .javaBaseUrl("D:\\\\IdeaProjects\\\\wd-server\\\\build-server\\\\src\\\\main")
                .scanPackage("domain");
//                .mainClassPackage("club.likunkun.buildserver")
//                .springMainClass(App.class);
        JpaGen.Gen(build);
    }




}
