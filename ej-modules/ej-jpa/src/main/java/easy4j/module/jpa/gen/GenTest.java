package easy4j.module.jpa.gen;


import easy4j.module.base.plugin.gen.BaseConfigCodeGen;

/**
 * GenTest
 *
 * @author bokun.li
 * @date 2025-05
 */
public class GenTest {
    public static void test(String[] args) throws Exception {
        ConfigJpaGen build = new ConfigJpaGen.Builder()
                .setOutAbsoluteUrl("")
                .setGenNote(true)
                .setScanPackageName("domain")
                .setTmplClassPath("tmpl")
                .setSpringMainClass(GenTest.class)
                .build();
//                .mainClassPackage("club.likunkun.buildserver")
//                .springMainClass(App.class);
        JpaGen.build(build).gen();
    }




}