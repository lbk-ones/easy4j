/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.module.base.utils.xml;

import com.fasterxml.jackson.core.type.TypeReference;
import easy4j.module.base.utils.json.JacksonUtil;

import java.time.LocalDate;
import java.util.*;

/**
 * TestExampleXmlToEntity
 *
 * @author bokun.li
 * @date 2025-05
 */
public class TestExampleXmlToEntity {

    public static void main(String[] args) {
        ExampleEntity exampleEntity = new ExampleEntity();
        exampleEntity.setId(UUID.randomUUID().toString());
        exampleEntity.setDosage("23");
        exampleEntity.setEndDate(LocalDate.now());
        exampleEntity.setOrdClass("23");
        exampleEntity.setEnterDate(new Date());
        exampleEntity.setOrdTxt("注射用氯化钠");
        exampleEntity.setOutVisitId("R10023");
        exampleEntity.setPatId("P999001");
        exampleEntity.setPTreatment("1");
        exampleEntity.setPTreatmentUnit("周");
        List<ExampleEntity.OrderItem> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ExampleEntity.OrderItem orderItem = new ExampleEntity.OrderItem();
            orderItem.setAmount(i + 1);
            orderItem.setOrdPrice(String.valueOf(i + 10));
            orderItem.setOrdCode(null);
            list.add(orderItem);
        }
        exampleEntity.setOrderItemList(list);
        String xml = JacksonXmlUtil.toXml(exampleEntity);
        System.out.println(xml);
        Object o = JacksonXmlUtil.parseXmlToObject(xml, ExampleEntity.class);
        System.out.println(JacksonUtil.toJson(o));

        Map<String, Object> stringObjectMap = JacksonXmlUtil.parseXmlToObject(xml, new TypeReference<Map<String, Object>>() {
        });
        System.out.println(JacksonUtil.toJson(stringObjectMap));

        //JacksonXmlUtil.printXmlBean(ExampleEntity.class);
    }
}
