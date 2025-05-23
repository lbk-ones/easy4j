package easy4j.module.base.utils.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import easy4j.module.base.annotations.Desc;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * 测试实体
 */
@Data
@JacksonXmlRootElement(localName = "Msg")
public class ExampleEntity {

    @JacksonXmlProperty(localName = "ORD_TXT")
    public String ordTxt;

    @JacksonXmlProperty(localName = "ORD_CLASS")
    public String ordClass;

    @JacksonXmlProperty(localName = "PAT_ID")
    public String patId;

    @JacksonXmlProperty(localName = "OUT_VISIT_ID")
    public String outVisitId;

    @JacksonXmlProperty(localName = "DOSAGE")
    public String dosage;

    @JacksonXmlProperty(localName = "P_TREATMENT")
    public String pTreatment;

    @JacksonXmlProperty(localName = "P_TREATMENT_UNIT")
    public String pTreatmentUnit;

    @JacksonXmlProperty(localName = "ENTER_DATE")
    public Date enterDate;

    @JacksonXmlProperty(localName = "END_DATE")
    public LocalDate endDate;

    @Desc("这种会把这个标签下面的 属性给解析出来")
    @JacksonXmlProperty(isAttribute = true, localName = "msgId")
    public String id;

    @Desc(" useWrapping = false 则不会把ITEMS算进去")
    @JacksonXmlElementWrapper(localName = "ITEMS", useWrapping = true)
    @JacksonXmlProperty(localName = "ITEM")
    private List<OrderItem> orderItemList;

    @Data
    public static class OrderItem {

        @JacksonXmlProperty(localName = "ORD_CODE")
        public String ordCode;
        @JacksonXmlProperty(localName = "ORD_PRICE")
        public String ordPrice;
        @JacksonXmlProperty(localName = "AMOUNT")
        public Integer amount;

    }


}
