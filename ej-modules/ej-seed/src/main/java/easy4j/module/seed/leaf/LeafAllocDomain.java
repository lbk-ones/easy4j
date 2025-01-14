package easy4j.module.seed.leaf;

import java.util.Date;

public class LeafAllocDomain {

    private String BIZ_TAG;

    private Long MAX_ID;

    private Long STEP;

    private String DESCRIPTION;
    private Date UPDATE_TIME;

    public String getBIZ_TAG() {
        return BIZ_TAG;
    }

    public void setBIZ_TAG(String BIZ_TAG) {
        this.BIZ_TAG = BIZ_TAG;
    }

    public Long getMAX_ID() {
        return MAX_ID;
    }

    public void setMAX_ID(Long MAX_ID) {
        this.MAX_ID = MAX_ID;
    }

    public Long getSTEP() {
        return STEP;
    }

    public void setSTEP(Long STEP) {
        this.STEP = STEP;
    }

    public String getDESCRIPTION() {
        return DESCRIPTION;
    }

    public void setDESCRIPTION(String DESCRIPTION) {
        this.DESCRIPTION = DESCRIPTION;
    }

    public Date getUPDATE_TIME() {
        return UPDATE_TIME;
    }

    public void setUPDATE_TIME(Date UPDATE_TIME) {
        this.UPDATE_TIME = UPDATE_TIME;
    }
}
