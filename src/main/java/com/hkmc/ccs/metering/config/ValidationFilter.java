package com.hkmc.ccs.metering.config;

//import com.hkmc.filter.wrapper.MultiReadHttpServletRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.GenericFilterBean;

//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.ServletRequest;
//import jakarta.servlet.ServletResponse;
//import jakarta.servlet.http.HttpServletRequest;
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

//	@Override
//	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
//
//	}
}

