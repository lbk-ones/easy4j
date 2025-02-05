package easy4j.module.jpa.base;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.module.base.exception.EasyException;
import easy4j.module.base.utils.ListTs;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 接口层面的通用dto 区别于统一消息头 但是可以直接把它拿来当消息头问题不大 baseDto继承于它
 * @author bokun.li
 * @date 2023/6/2
 */
@Data
@Slf4j
public class ControllerDto {
    // 批量删除
    private List<String> deleteIds;
    // 批量添加
    private List<Map<String,Object>> listSave;
    // 批量起停用
    private List<String> listEnable;
    // 批量更新
    private List<Map<String,Object>> listUpdate;

    // 一个通用的集合对象
    private List<Map<String,Object>> list;

    private Map<String,Object> _params = new HashMap<>();

    public List<String> castListString(String name) throws EasyException {
        try{
            Object o = _params.get(name);
            return (List<String>)o;
        }catch (Exception e){
            throw new EasyException(name+" 抓换异常，请使用字符串集合");
        }
    }
    public List<Map<String, Object>> castListMap(String name) throws EasyException {
        try{
            Object o = _params.get(name);
            return (List<Map<String, Object>>)o;
        }catch (Exception e){
            throw new EasyException(name+" 抓换异常，请使用对象集合");

        }
    }

    public List<Map<String,Object>> checkListUpdate() throws EasyException {
        if(CollUtil.isNotEmpty(getListUpdate())){
            return getListUpdate();
        }else{
            throw new EasyException("请传入listUpdate");
        }
    }

    public  List<Map<String,Object>> checkListSave() throws EasyException {
        if(CollUtil.isNotEmpty(getListSave())){
            return getListSave();
        }else{
            throw new EasyException("listUpdate不能为空");
        }
    }

    public List<String> checkListEnable() throws EasyException {
        if(CollUtil.isNotEmpty(getListEnable())){
            return getListEnable();
        }else{
            throw new EasyException("listEnable不能为空");
        }
    }

    public List<Map<String,Object>> checkList() throws EasyException {
        if(CollUtil.isNotEmpty(getList())){
            return getList();
        }else{
            throw new EasyException("list不能为空");
        }
    }

    public List<String> checkDeleteList() throws EasyException {
        if(CollUtil.isNotEmpty(getDeleteIds())){
            return getDeleteIds();
        }else{
            throw new EasyException("deleteIds不能为空");
        }
    }
    /**
     * 自定义检查key异常
     * @author bokun.li
     * @date 2023/6/2
     */
    public void checkKey(String ... keys) throws EasyException {
        List<String> w = ListTs.newArrayList();
        for (String key : keys) {
            // 可以解析 这种 list-id 检查集合每一个元素 是否缺少 如果缺少就提示
            if(key.contains("-")){
                String[] split = key.split("-");
                String listName = split[0];
                String newKey = split[1];
                if(StrUtil.isAllNotBlank(listName,newKey)){
                    Object o = _params.get(listName);
                    List<Map<String, Object>> maps = castListMap(listName);
                    for (Map<String, Object> map : maps) {
                        for (Map.Entry<String, Object> entry : map.entrySet()) {
                            String key1 = entry.getKey();
                            if(newKey.equals(key1)){
                                Object value = entry.getValue();
                                if(value == null || StrUtil.isBlank(value.toString())){
                                    if(!w.contains(key)){
                                        w.add(key);
                                    }
                                }
                            }
                        }
                    }
                }

            }
            Object o = _params.get(key);
            if(Objects.nonNull(o)){
                String key1 = getKey(key, o);
                if(Objects.nonNull(key1)){
                    w.add(key1);
                }
            }else{
                Object fieldValue = ReflectUtil.getFieldValue(this, key);
                String key1 = getKey(key, fieldValue);
                if(Objects.nonNull(key1)){
                    w.add(key);
                }
            }
        }
        if(CollUtil.isNotEmpty(w)){
            String join = String.join(",", w);
            throw new EasyException(join+"---不能为空");
        }
    }
    private String getKey(String key,Object fieldValue){
        String tem = null;
        if(Objects.isNull(fieldValue)){
            tem = key;
            return tem;
        }
        if(fieldValue instanceof List){
            List fieldValue1 = (List) fieldValue;
            if (fieldValue1.isEmpty()) {
                tem = key;
            }
        }else if(fieldValue instanceof Map){
            Map fieldValue1 = (Map) fieldValue;
            if (fieldValue1.isEmpty()) {
                tem = key;
            }
        }else if(fieldValue instanceof String){
            String s = fieldValue.toString();
            String trim = s.trim();
            if("".equals(trim)){
                tem = key;
            }
        }
        return tem;
    }

    /**
     * 将上面那几个集合对象解析成想要的对象
     * @author bokun.li
     * @date 2023/6/2
     */
    public <T> List<T> parseList(List<Map<String, Object>> objt, Class<T> clazz){
        List<T> objects = ListTs.newArrayList();
        try{
            if(ListTs.isNotEmpty(objt)){
                List<T> ts = BeanUtil.copyToList(objt, clazz);
                objects.addAll(ts);
            }
        }catch (Exception e){
            log.error("parseList 解析出现异常 ",e);
        }
        return objects;
    }

}
