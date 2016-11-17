/*
 * #%L
 * BroadleafCommerce Common Libraries
 * %%
 * Copyright (C) 2009 - 2016 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 *
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.common.web;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.RequestDTO;
import org.broadleafcommerce.common.classloader.release.ThreadLocalManager;
import org.broadleafcommerce.common.exception.ExceptionHelper;
import org.springframework.context.MessageSource;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Convenient holder class for various objects to be automatically available on thread local without invoking the various
 * services yourself
 *
 */
public class CommonRequestContext {

    protected static final Log LOG = LogFactory.getLog(CommonRequestContext.class);

    private static final ThreadLocal<CommonRequestContext> COMMON_REQUEST_CONTEXT = ThreadLocalManager.createThreadLocal(CommonRequestContext.class);

    public static CommonRequestContext getCommonRequestContext() {
        return COMMON_REQUEST_CONTEXT.get();
    }

    public static void setCommonRequestContext(CommonRequestContext commonRequestContext) {
        COMMON_REQUEST_CONTEXT.set(commonRequestContext);
    }

//TODO: microservices - deal with locale
//    public static boolean hasLocale(){
//        if (getCommonRequestContext() != null) {
//            if(getCommonRequestContext().getLocale() != null){
//                return true;
//            }
//        }
//        return false;
//    }

    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected WebRequest webRequest;

    //TODO: microservices - deal with locale
    //protected Locale locale;

    protected TimeZone timeZone;
    protected java.util.Locale javaLocale;
    protected Map<String, Object> additionalProperties = new HashMap<String, Object>();
    protected MessageSource messageSource;
    protected RequestDTO requestDTO;

    /**
     * Gets the current request on the context
     * @return
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * Provide easy access to Request Attributes without introducing a tight dependency on the HttpRequest.
     * @return
     */
    public Object getRequestAttribute(String name) {
        Object param = null;
        if (getRequest() != null) {
            param = getRequest().getAttribute(name);
        }
        return param;
    }

    /**
     * Sets the current request on the context. Note that this also invokes {@link #setWebRequest(WebRequest)} by wrapping
     * <b>request</b> in a {@link ServletWebRequest}.
     *
     * @param request
     */
    public void setRequest(HttpServletRequest request) {
        this.request = request;
        this.webRequest = new ServletWebRequest(request);
    }

    /**
     * Returns the response for the context
     *
     * @return
     */
    public HttpServletResponse getResponse() {
        return response;
    }

    /**
     * Sets the response on the context
     *
     * @param response
     */
    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    /**
     * Sets the generic request on the context. This is available to be used in non-Servlet environments (like Portlets).
     * Note that if <b>webRequest</b> is an instance of {@link ServletWebRequest} then
     * {@link #setRequest(HttpServletRequest)} will be invoked as well with the native underlying {@link HttpServletRequest}
     * passed as a parameter.
     * <br />
     * <br />
     * Also, if <b>webRequest</b> is an instance of {@link ServletWebRequest} then an attempt is made to set the response
     * (note that this could be null if the ServletWebRequest was not instantiated with both the {@link HttpServletRequest}
     * and {@link HttpServletResponse}
     * @param webRequest
     */
    public void setWebRequest(WebRequest webRequest) {
        this.webRequest = webRequest;
        if (webRequest instanceof ServletWebRequest) {
            this.request = ((ServletWebRequest) webRequest).getRequest();
            setResponse(((ServletWebRequest) webRequest).getResponse());
        }
    }

    /**
     * Returns the generic request for use outside of servlets (like in Portlets). This will be automatically set
     * by invoking {@link #setRequest(HttpServletRequest)}
     *
     * @return the generic request
     * @see {@link #setWebRequest(WebRequest)}
     */
    public WebRequest getWebRequest() {
        return webRequest;
    }

//TODO: microservices - deal with locale
//    public Locale getLocale() {
//        return locale;
//    }
//
//    /**
//     * Returns the java.util.Locale constructed from the org.broadleafcommerce.common.locale.domain.Locale.
//     * @return
//     */
//    public java.util.Locale getJavaLocale() {
//        if (this.javaLocale == null) {
//            this.javaLocale = convertLocaleToJavaLocale();
//        }
//        return this.javaLocale;
//    }
//
//    public void setLocale(Locale locale) {
//        this.locale = locale;
//        this.javaLocale = convertLocaleToJavaLocale();
//    }

    public String getRequestURIWithoutContext() {
        String requestURIWithoutContext = null;

        if (request != null && request.getRequestURI() != null) {
            if (request.getContextPath() != null) {
                requestURIWithoutContext = request.getRequestURI().substring(request.getContextPath().length());
            } else {
                requestURIWithoutContext = request.getRequestURI();
            }

            // Remove JSESSION-ID or other modifiers
            int pos = requestURIWithoutContext.indexOf(";");
            if (pos >= 0) {
                requestURIWithoutContext = requestURIWithoutContext.substring(0,pos);
            }
        }

        return requestURIWithoutContext;
    }

//TODO: microservices - deal with locale
//    protected java.util.Locale convertLocaleToJavaLocale() {
//        if (locale == null || locale.getLocaleCode() == null) {
//            return java.util.Locale.getDefault();
//        } else {
//            return CommonRequestContext.convertLocaleToJavaLocale(locale);
//        }
//    }
//
//    public static java.util.Locale convertLocaleToJavaLocale(Locale broadleafLocale) {
//        if (broadleafLocale != null) {
//            return broadleafLocale.getJavaLocale();
//        }
//        return null;
//    }

    public boolean isSecure() {
        boolean secure = false;
        if (request != null) {
            secure = ("HTTPS".equalsIgnoreCase(request.getScheme()) || request.isSecure());
        }
        return secure;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String[]> getRequestParameterMap() {
        return getCommonRequestContext().getRequest().getParameterMap();
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public RequestDTO getRequestDTO() {
        return requestDTO;
    }

    public void setRequestDTO(RequestDTO requestDTO) {
        this.requestDTO = requestDTO;
    }

    /**
     * In some cases, it is useful to utilize a clone of the context that does not include the actual container request
     * and response information. Such a case would be when executing an asynchronous operation on a new thread from
     * an existing request thread. That new thread may still require context information, in which case this lightweight
     * context is useful.
     *
     * @return The instance without the container request and response
     */
    public CommonRequestContext createLightWeightClone() {
        CommonRequestContext context = new CommonRequestContext();

        //TODO: microservices - deal with locale
        //context.setLocale(locale);

        context.setMessageSource(messageSource);
        context.setTimeZone(timeZone);
        //purposefully excluding additionalProperties - this contains state that can mess with SandBoxFilterEnabler (for one)

        return context;
    }

    /**
     * In some cases, it is useful to create a JSON representation of the context that does not include the actual container
     * request and response information. This can be used subsequently to resurrect the CommonRequestContext state, presumably
     * on a new thread.
     *
     * @return
     */
    public String createLightWeightCloneJson() {
        StringBuilder sb = new StringBuilder();

//TODO: microservices - deal with locale
//        sb.append("\",\"locale\":\"");
//        sb.append(locale==null?null:locale.getLocaleCode());

        sb.append("\",\"timeZone\":\"");
        sb.append(timeZone==null?null:timeZone.getID());
        sb.append("\"}");
        return sb.toString();
    }

    /**
     * Resurrect the CommonRequestContext state based on a JSON representation.
     *
     * @param Json
     * @param em
     * @return
     */
    public static CommonRequestContext createLightWeightCloneFromJson(String Json, EntityManager em) {
        CommonRequestContext context = new CommonRequestContext();
        JsonFactory factory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper(factory);
        TypeReference<HashMap<String,String>> typeRef = new TypeReference<HashMap<String,String>>() {};
        HashMap<String,String> json;
        try {
            json = mapper.readValue(Json, typeRef);
        } catch (IOException e) {
            throw ExceptionHelper.refineException(e);
        }

//TODO: microservices - deal with local
//        if (!json.get("locale").equals("null")) {
//            context.setLocale(em.find(LocaleImpl.class, json.get("locale")));
//        }

        if (!json.get("timeZone").equals("null")) {
            context.setTimeZone(TimeZone.getTimeZone(json.get("timeZone")));
        }

        return context;
    }
}