package com.hyundaiautoever.ccs.metering.requestResponseLogging;


import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@Component
public class RequestResponseLoggerFilter implements Filter {
    private final Logger log;
    public static final String TID_HEADER_NAME = "xtid";

    public RequestResponseLoggerFilter(@Qualifier("requestResponseLogger") Logger log) {
        this.log = log;
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpServletRequest multiReadRequest = new MultiReadHttpServletRequest(req);

        HttpHeaders headers = getHeaders(multiReadRequest);
        String tid = getTID(headers);

        logRequest(multiReadRequest, headers, tid);
        chain.doFilter(multiReadRequest, res);
        logResponse(res, tid);
    }

    private String getTID(HttpHeaders headers) {
        List<String> tidHeaders = headers.getOrEmpty(TID_HEADER_NAME);
        return !tidHeaders.isEmpty() ? tidHeaders.get(0) : "";
    }

    private void logRequest(HttpServletRequest req, Map<String, List<String>> headers, String tid) throws IOException {
        String reqBody = "";

        if (req.getHeader(CONTENT_TYPE) != null) {
            reqBody = req.getReader().lines().collect(Collectors.joining());
        }
        log.info(
                "Logging Request {}:: Method : {}, URI : {}, Headers: {}, Body: {}",
                tid,
                req.getMethod(),
                req.getRequestURI(),
                headers,
                reqBody
        );
    }

    private void logResponse(HttpServletResponse res, String tid) {
        boolean responseIs2XX = Integer.toString(res.getStatus()).startsWith("2");
        if (responseIs2XX) {
            return;
        }
        log.info(
                "Logging Response {}:: Status : {}",
                tid,
                res.getStatus());
    }

    private HttpHeaders getHeaders(HttpServletRequest req) {
        Enumeration<String> headerNames = req.getHeaderNames();
        HttpHeaders headers = new HttpHeaders();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, Collections.list(req.getHeaders(name)));
        }
        return headers;
    }

}
