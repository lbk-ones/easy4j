package easy4j.infra.rpc.serializable;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import easy4j.infra.rpc.exception.RpcException;
import easy4j.infra.rpc.serializable.hession.HessianSerializerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * hession序列化
 * 优点：二进制格式，跨语言（Java/Python/Go 等），体积小，基于 HTTP 传输；
 * 1、无参构造
 * 2、实现Serializable接口
 * 3、非final字段
 * 4、必须有setters/getters
 */
public class HessionSerializable implements ISerializable {

    @Override
    public byte[] serializable(Object object) {
        if (object == null) return null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2Output ho = new Hessian2Output(bos);
        try {
            ho.setSerializerFactory(HessianSerializerFactory.getInstance());
            ho.writeObject(object); // 核心序列化方法
        } catch (IOException e) {
            throw new RpcException(e);
        }
        return bos.toByteArray();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserializable(byte[] object, Class<T> tClass) {
        if (object == null) return null;
        ByteArrayInputStream bis = new ByteArrayInputStream(object);
        Hessian2Input hi = new Hessian2Input(bis);
        try {
            hi.setSerializerFactory(HessianSerializerFactory.getInstance());
            return (T) hi.readObject(tClass); // 核心反序列化方法
        } catch (IOException e) {
            throw new RpcException(e);
        }
    }
}
