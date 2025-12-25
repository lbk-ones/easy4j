package easy4j.module.mybatisplus.codegen.servlet;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
public class PreviewRes {

    public List<PInfo> infoList = new ArrayList<>();

    @Data
    @NoArgsConstructor
    public static class PInfo {
        public String tagName;
        List<PItem> itemList = new ArrayList<>();

        public PInfo(String tagName) {
            this.tagName = tagName;
        }

        public void add(String fileName, String preview) {
            PItem pItem = new PItem();
            pItem.setFileName(fileName);
            pItem.setPreview(preview);
            itemList.add(pItem);
        }
    }

    @Data
    public static class PItem {
        public String fileName;
        // 显示结果
        public String preview;
    }

    public void add(PInfo pInfo) {
        if (null != pInfo && !pInfo.getItemList().isEmpty()) infoList.add(pInfo);
    }

}
