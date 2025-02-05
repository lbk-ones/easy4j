package easy4j.module.jpa.base;

import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

public class PageBean<T> {
	
	private long totalElements;
	
	private int size;
	
	private List<T> content = new ArrayList<T>();
	
	public PageBean(){}
	
	public PageBean(Page<?> page) {
		this.setTotalElements(page.getTotalElements());
		this.setSize(page.getSize());
	}
	
	public void addContent(T e) {
		if(content == null) {
			content = new ArrayList<T>();
		}
		content.add(e);
	}
	
	@SuppressWarnings("unchecked")
	public static <E,T> PageBean<E> getPageBean(Page<T> page, PageBeanTemplate<T> pageBeanTemplate) {
		PageBean<E> pageBean = new PageBean<E>();
		for(int i=0; i<page.getContent().size(); i++) {
			Object obj = pageBeanTemplate.execute(page.getContent().get(i));
			pageBean.addContent((E)obj);
		}
		
		pageBean.setTotalElements(page.getTotalElements());
		pageBean.setSize(page.getSize());
		return pageBean;
	}
	
	
	public interface PageBeanTemplate<E> {
		public Object execute(E e);
	}
	
	public long getTotalElements() {
		return totalElements;
	}

	public void setTotalElements(long totalElements) {
		this.totalElements = totalElements;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public List<T> getContent() {
		return content;
	}

	public void setContent(List<T> content) {
		this.content = content;
	}
	
}
