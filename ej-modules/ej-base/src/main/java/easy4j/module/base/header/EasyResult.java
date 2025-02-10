package easy4j.module.base.header;

import cn.hutool.core.util.StrUtil;
import easy4j.module.base.exception.EasyException;
import easy4j.module.base.utils.BusCode;
import easy4j.module.base.utils.ListTs;
import easy4j.module.base.utils.SysConstant;
import easy4j.module.base.utils.SysLog;
import easy4j.module.base.plugin.i18n.I18nBean;
import jodd.util.StringPool;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 一个好的信息返回实体 应该包括 开始时间 返回时间 是否错误（如果是业务异常 或者系统异常 或者未知异常 都应该是1，其他情况都是0） 业务状态码 主要用来
 * @author bokun.li
 * @param <T>
 */
@Setter
@Getter
public class EasyResult<T> implements Serializable {

	private static final long serialVersionUID = 6095433538316185020L;
	// 1 代表错误 0 代表正常返回
	private int error;

	// 开始时间
	private Long startTime;
	// 结束时间
	private Long endTime;
	// 业务状态码
	private String code;

	private String rpcMethod;

	// 提示消息
	private String message;
	// 错误堆栈信息
	private String errorInfo;
	// 返回对象
	private T data;


	public static <T> EasyResult<T> ok(T data){

		EasyResult<T> easyResult = new EasyResult<T>();

		easyResult.setData(data);
		easyResult.setMessage(I18nBean.getOperateSuccessStr());
		return easyResult;
	}

	public static <T> EasyResult<T> ok(Date startDate, T data){

		EasyResult<T> easyResult = new EasyResult<T>();

		easyResult.setData(data);
		easyResult.setStartTime(startDate!=null?startDate.getTime(): easyResult.getStartTime());
		easyResult.setMessage(I18nBean.getOperateSuccessStr());
		return easyResult;
	}

	public static <T> EasyResult<T> okCode(String code){
		EasyResult<T> easyResult = new EasyResult<T>();
		easyResult.setData(null);
		easyResult.setCode(code);
		String message1 = I18nBean.getMessage(code);
		if(StrUtil.isNotBlank(message1)){
			easyResult.setMessage(message1);
		}else{
			easyResult.setMessage(I18nBean.getOperateSuccessStr());
		}
		return easyResult;
	}
	public static <T> EasyResult<T> ok(Date startDate, T data, String code){

		EasyResult<T> easyResult = new EasyResult<T>();

		easyResult.setData(data);
		easyResult.setCode(code);
		easyResult.setStartTime(startDate!=null?startDate.getTime(): easyResult.getStartTime());
		easyResult.setMessage(I18nBean.getOperateSuccessStr());
		return easyResult;
	}

	public static <T> EasyResult<T> errorInfo(String message){

		EasyResult<T> easyResult = new EasyResult<T>();
		easyResult.setError(SysConstant.ERRORCODE);
		easyResult.setMessage(message);
		easyResult.setData(null);
		return easyResult;
	}

	public static <T> EasyResult<T> errorInfo(Throwable e){
		EasyResult<T> easyResult = new EasyResult<T>();
		easyResult.setCode(BusCode.A00003);
		easyResult.setError(SysConstant.ERRORCODE);
		easyResult.setMessage(I18nBean.getOperateErrorStr());
		if(!(e instanceof EasyException)){
			easyResult.setErrorInfo(SysLog.getStackTraceInfo(e));
		}
		easyResult.setData(null);
		return easyResult;
	}

	/**
	 * 接收rpc的报错异常
	 * @param e
	 * @return
	 * @param <T>
	 */
	public static <T> EasyResult<T> rpcErrorInfo(Exception e) {
		EasyResult<T> easyResult = new EasyResult<>();
		easyResult.setCode("A00003");
		String message1 = e.getMessage();
		easyResult.setMessage(message1);
		easyResult.setError(SysConstant.ERRORCODE);
		easyResult.setErrorInfo("");
		easyResult.setData(null);
		return easyResult;
	}

	/**
	 * <p>转 i18n</p>
	 * <p>可以直接抛出类似这种异常 throw EasyException("A0001,参数1,参数2") 然后参数自动填充到占位符里面去</p>
	 * @author bokun.li
	 * @param e 异常信息
	 * @return 返回异常结果
	 * @param <T>
	 */
	public static <T> EasyResult<T> toI18n(Throwable e){
		return toI18n(e, null);
	}

	/**
	 * 根据传入的local转i18n
	 * @author bokun.li
	 * @param e
	 * @param local
	 * @return
	 * @param <T>
	 */
	public static <T> EasyResult<T> toI18n(Throwable e, Locale local){
		String msg = "";
		boolean isEasy4j = false;
		String msgKey = null;
		if(e instanceof EasyException){
			String message1 = e.getMessage();
			if(StrUtil.isNotEmpty(message1)){
				isEasy4j = true;
				int i = message1.indexOf(",");
				msgKey = message1.substring(0, i>0?i:message1.length());
				if(i>0){
					String argStr = message1.substring(i+1);
					if (StrUtil.isNotEmpty(argStr)) {
						List<String> list = ListTs.asList(argStr.split(StringPool.COMMA));
						msg = I18nBean.getMessage(msgKey,local, list.toArray(new String[]{}));
					}
				}else{
					msg = I18nBean.getMessage(msgKey);
				}
			}
		}
		String code = "A00003";
		// 不允许使用自己定义的内容发布异常
		if(msg.isEmpty()){
			msg = isEasy4j?e.getMessage():I18nBean.getMessage(code,local);
		}else{
			code = msgKey;
		}
		EasyResult<T> easyResult = new EasyResult<T>();
		easyResult.setError(SysConstant.ERRORCODE);
		easyResult.setMessage(msg);
		easyResult.setCode(code);
		if(!(e instanceof EasyException)){
			easyResult.setErrorInfo(SysLog.getStackTraceInfo(e));
		}
		easyResult.setData(null);
		return easyResult;

	}

	public EasyResult() {
		setError(SysConstant.SUCCESSCODE);
		long time = new Date().getTime();
		setStartTime(time);
		setEndTime(time);
		setCode("A00001");
		//setMessage(I18nBean.getOperateSuccessStr());
	}



	public boolean isSuccess(){
		return error == 0;
	}

	public EasyResult(int error, String code, String message, T data) {
		this.error = error;
		this.code = code;
		this.message = message;
		this.data = data;
	}

	public EasyResult(int error) {
		this.error = error;
	}

	public EasyResult(int error, String code) {
		this.error = error;
		this.code = code;
	}

	public EasyResult(int error, String code, String message) {
		this.error = error;
		this.code = code;
		this.message = message;
	}
}