package easy4j.module.base.log;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

public class DefLog {

    public static final List<String> infoLine = Collections.synchronizedList(Lists.newLinkedList());
    public static final List<String> warnLine = Collections.synchronizedList(Lists.newLinkedList());
    public static final List<String> errorLine = Collections.synchronizedList(Lists.newLinkedList());
    public static final List<String> debugLine = Collections.synchronizedList(Lists.newLinkedList());
    public static final List<String> traceLine = Collections.synchronizedList(Lists.newLinkedList());


    public void info(String e){
        infoLine.add(e);
    }

    public  void debug(String e){
        debugLine.add(e);
    }

    public  void trace(String e){
        traceLine.add(e);
    }

    public void error(String e){
        errorLine.add(e);
    }
}
