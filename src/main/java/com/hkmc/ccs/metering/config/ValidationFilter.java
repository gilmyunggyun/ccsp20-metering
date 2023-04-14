package com.hkmc.ccs.metering.config;

//import com.hkmc.filter.wrapper.MultiReadHttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
public class ValidationFilter extends GenericFilterBean {

	private String validationCheck;

	public ValidationFilter(final String validationCheck) {
		this.validationCheck = validationCheck;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
//		HttpServletRequest httpRequest = ((HttpServletRequest) request);
//		MultiReadHttpServletRequest wrapper = new MultiReadHttpServletRequest(httpRequest);
		chain.doFilter(request, response);
	}

}

