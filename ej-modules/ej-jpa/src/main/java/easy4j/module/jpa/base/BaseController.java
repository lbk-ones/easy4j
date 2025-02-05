package easy4j.module.jpa.base;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@RestController
public abstract class BaseController {

	public BaseController(){
	}
	
	private HttpServletRequest request;

	public HttpServletRequest getRequest() {
		RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        HttpServletRequest request = sra.getRequest();
        this.setRequest(request);
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

}
