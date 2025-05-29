package easy4j.module.base.exception;

import java.util.ArrayList;

/**
 * ExceptionList
 *
 * @author bokun.li
 * @date 2025-05
 */
public class ExceptionList<E> extends ArrayList<E>{

	private static final long serialVersionUID = -694749170467810026L;

	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		for(E e:this){
			sb.append(e.toString()).append(",");
		}
		if(sb.length()>0){
			sb.delete(sb.lastIndexOf(","), sb.length());
		}
		return sb.toString();
	}
	
}