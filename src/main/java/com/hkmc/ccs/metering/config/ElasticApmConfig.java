package com.hkmc.ccs.metering.config;



import co.elastic.apm.attach.ElasticApmAttacher;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

//@Setter
//@Configuration
//@ConfigurationProperties(prefix = "elastic.apm")
////@ConditionalOnProperty(value = "elastic.apm.enabled", havingValue = "true")
//public class ElasticApmConfig {
//
//
//
//    private static final String USE_PATH_AS_TRANSACTION_NAME = "use_path_as_transaction_name";
//    private String usePathAsTransactionName;
//
//    private static final String CAPTURE_BODY_CONTENT_TYPES = "capture_body_content_types";
//    private String captureBodyContentTypes;
//
//    private static final String CAPTURE_BODY = "capture_body";
//    private String captureBody;
//
//    private static final String CAPTURE_HEADERS = "capture_headers";
//    private String captureHeaders;
//
//    private static final String ENABLE_EXPERIMENTAL_INSTRUMENTATIONS = "enable_experimental_instrumentations";
//    private String enableExperimentalInstrumentations;
//
//    private static final String SERVER_URL_KEY = "server_urls";
//    private String serverUrls;
//
//    private static final String SERVICE_NAME_KEY = "service_name";
//    private String serviceName;
//
////    private static final String SECRET_TOKEN_KEY = "secret_token";
////    private String secretToken;
//
//    private static final String ENVIRONMENT_KEY = "environment";
//    private String environment;
//
//    private static final String APPLICATION_PACKAGES_KEY = "application_packages";
//    private String applicationPackages;
//
//    private static final String LOG_LEVEL_KEY = "log_level";
//    private String logLevel;
//
//    @PostConstruct
//    public void init() {
//
//        Map<String, String> apmProps = new HashMap<>();
//        apmProps.put(SERVER_URL_KEY, serverUrls);
//        apmProps.put(SERVICE_NAME_KEY, serviceName);
//        apmProps.put(USE_PATH_AS_TRANSACTION_NAME, usePathAsTransactionName);
//        apmProps.put(CAPTURE_BODY_CONTENT_TYPES, captureBodyContentTypes);
//        apmProps.put(CAPTURE_BODY, captureBody);
//        apmProps.put(CAPTURE_HEADERS, captureHeaders);
//
////        apmProps.put(ENVIRONMENT_KEY, environment);
//        apmProps.put(APPLICATION_PACKAGES_KEY, applicationPackages);
//        apmProps.put(ENABLE_EXPERIMENTAL_INSTRUMENTATIONS, enableExperimentalInstrumentations);
//
//        ElasticApmAttacher.attach(apmProps);
//    }
//}
