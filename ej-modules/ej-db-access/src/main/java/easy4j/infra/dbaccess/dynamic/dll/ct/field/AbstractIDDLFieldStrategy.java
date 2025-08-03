package easy4j.infra.dbaccess.dynamic.dll.ct.field;

import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;


public abstract class AbstractIDDLFieldStrategy implements IDDLFieldStrategy {


    @Override
    public String getResColumn(DDLFieldInfo ddlFieldInfo) {
        return null;
    }


    public int getStrDefaultLength() {
        return 255;
    }

    public int getNumLengthDefaultLength() {
        return 6;
    }

    public int getNumDecimalDefaultLength() {
        return 4;
    }

}
