package org.fnppl.opensdx.keyserverfe;


import java.io.File;
import javax.servlet.http.*;

//import com.oreilly.servlet.*;

/**
 *
 * @author Henning Thieß <ht@fnppl.org>
 */
public class MultiTypeRequest implements HttpServletRequest {
//    public MultipartRequest mpr = null;
    HttpServletRequest request = null;
    
    public static File tmpdir = new File("/tmp/"); //TODO über config!!!
    public static File filesdir = null;
    static {
    	try {
    		filesdir = BinaryServlet.getFilesDir();
    	}catch(Exception ex) {
    		ex.printStackTrace();
    	}
    }
    
    /** Creates a new instance of MultiTypeRequest */
    public MultiTypeRequest(HttpServletRequest request) {
        this.request = request;
        
        if(request.getHeader("content-type") != null && request.getHeader("content-type").indexOf("multipart/form-data")==0) {
        	throw new RuntimeException("Not implemented yet");
//            try {
//                if(!tmpdir.exists()) {
//                    tmpdir.mkdirs();
//                }
//                
////                mpr = new MultipartRequest(request, tmpdir.getPath(), 10000000, "utf-8");                                
//            }
//            catch (java.io.IOException e) {
//                e.printStackTrace();
//            }
        }
    }
    public boolean isMultiPart() {
//    	return mpr!=null;
    	return false;
    }
    
//    public String getDataDir() {
//        return this.datadir;
//    }
    
    public Object getAttribute(String str) {
        return request.getAttribute(str);
    }
    
    @SuppressWarnings("unchecked")
	public java.util.Enumeration getAttributeNames() {
        return request.getAttributeNames();
    }
    
    public String getAuthType() {
        return request.getAuthType();
    }
    
    public String getCharacterEncoding() {
        return request.getCharacterEncoding();
    }
    
    public int getContentLength() {
        return request.getContentLength();
    }
    
    public String getContentType() {
        return request.getContentType();
    }
    
    public String getContextPath() {
        return request.getContextPath();
    }
    
    public javax.servlet.http.Cookie[] getCookies() {
        return request.getCookies();  
    }
    
    public long getDateHeader(String str) {
        return request.getDateHeader(str);
    }
    
    public String getHeader(String str) {
        return request.getHeader(str);
    }
    
    @SuppressWarnings("unchecked")
	public java.util.Enumeration getHeaderNames() {
        return request.getHeaderNames();
    }
    
    @SuppressWarnings("unchecked")
	public java.util.Enumeration getHeaders(String str) {
        return request.getHeaders(str);
    }
    
    public javax.servlet.ServletInputStream getInputStream() throws java.io.IOException {
        return request.getInputStream();
    }
    
    public int getIntHeader(String str) {
        return request.getIntHeader(str);
    }
    
    public java.util.Locale getLocale() {
        return request.getLocale();
    }
    
    public int getLocalPort() {
        return request.getLocalPort();
    }
    public int getRemotePort() {
        return request.getRemotePort();
    }
    
    public String getLocalAddr() {
        return request.getLocalAddr();
    }
    public String getLocalName() {
        return request.getLocalName();
    }
    
    @SuppressWarnings("unchecked")
	public java.util.Enumeration getLocales() {
        return request.getLocales();
    }
    
    public String getMethod() {
        return request.getMethod();
    }
    
    public String getParameter(String str) {
//        if(mpr!=null) {
//            return mpr.getParameter(str);
//        }
//        else {
            return request.getParameter(str);
//        }
    }
    
    @SuppressWarnings("unchecked")
	public java.util.Map getParameterMap() {
        return request.getParameterMap();
    }
    
    @SuppressWarnings("unchecked")
	public java.util.Enumeration getParameterNames() {
//        if(mpr!=null) {
//            return mpr.getParameterNames();
//        }
//        else {
            return request.getParameterNames();
//        }
    }
    
    public String[] getParameterValues(String str) {
//        if(mpr!=null) {
//            return mpr.getParameterValues(str);
//        }
//        else {
            return request.getParameterValues(str);
//        }
    }
    
    public String getPathInfo() {
        return request.getPathInfo();
    }
    
    public String getPathTranslated() {
        return request.getPathTranslated();
    }
    
    public String getProtocol() {
        return request.getProtocol();
    }
    
    public String getQueryString() {
        return request.getQueryString();
    }
    
    public java.io.BufferedReader getReader() throws java.io.IOException {
        return request.getReader();
    }
    
    @SuppressWarnings("deprecation")
	public String getRealPath(String str) {
        return request.getRealPath(str);
    }
    
    public String getRemoteAddr() {
        return request.getRemoteAddr();
    }
    
    public String getRemoteHost() {
        return request.getRemoteHost();
    }
    
    public String getRemoteUser() {
        return request.getRemoteUser();
    }
    
    public javax.servlet.RequestDispatcher getRequestDispatcher(String str) {
        return request.getRequestDispatcher(str);
    }
    
    public String getRequestURI() {
        return request.getRequestURI();
    }
    
    public StringBuffer getRequestURL() {
        return request.getRequestURL();
    }
    
    public String getRequestedSessionId() {
        return request.getRequestedSessionId();
    }
    
    public String getScheme() {
        return request.getScheme();
    }
    
    public String getServerName() {
        return request.getServerName();
    }
    
    public int getServerPort() {
        return request.getServerPort();
    }
    
    public String getServletPath() {
        return request.getServletPath();
    }
    
    public javax.servlet.http.HttpSession getSession() {
        return request.getSession();
    }
    
    public javax.servlet.http.HttpSession getSession(boolean param) {
        return request.getSession(param);
    }
    
    public java.security.Principal getUserPrincipal() {
        return request.getUserPrincipal();
    }
    
    public boolean isRequestedSessionIdFromCookie() {
        return request.isRequestedSessionIdFromCookie();
    }
    
    public boolean isRequestedSessionIdFromURL() {
        return request.isRequestedSessionIdFromURL();
    }
    
    @SuppressWarnings("deprecation")
	public boolean isRequestedSessionIdFromUrl() {
        return request.isRequestedSessionIdFromUrl();
    }
    
    public boolean isRequestedSessionIdValid() {
        return request.isRequestedSessionIdValid();
    }
    
    public boolean isSecure() {
        return request.isSecure();
    }
    
    public boolean isUserInRole(String str) {
        return request.isUserInRole(str);
    }
    
    public void removeAttribute(String str) {
        request.removeAttribute(str);
    }
    
    public void setAttribute(String str, Object obj) {
        request.setAttribute(str, obj);
    }
    
    public void setCharacterEncoding(String str) throws java.io.UnsupportedEncodingException {
        request.setCharacterEncoding(str);
    }
}
