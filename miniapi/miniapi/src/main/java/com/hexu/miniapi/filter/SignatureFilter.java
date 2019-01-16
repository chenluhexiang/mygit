package com.hexu.miniapi.filter;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

//@Component
public class SignatureFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String signature = request.getHeader("signature");
        if (!testSignature(signature, request)) {
            response.sendError(403, "signature error!!!");
            return;
        }

        chain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }

    private boolean testSignature(String signature, HttpServletRequest request) {
        List<String> mdList = getSignatureList(request);
        for (String s : mdList) {
            if (s.equals(signature)) {
                return true;
            }
        }
        return false;
    }

    private List<String> getSignatureList(HttpServletRequest request) {
        List<String> list = new ArrayList<>();
        Map<String, String[]> params = request.getParameterMap();
        StringBuffer sb = new StringBuffer("");
        for (String s : params.keySet()) {
            sb.append(s);
        }

        sb.append("5L33P");

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, -4);
        list.add(MD5(sb.toString() + sdf.format(calendar.getTime())).toLowerCase());
        for (int i = 0; i < 30; i++) {
            calendar.add(Calendar.SECOND, 1);
            list.add(MD5(sb.toString() + sdf.format(calendar.getTime())).toLowerCase());
        }
        return list;
    }

    private String MD5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(s.getBytes("utf-8"));
            return toHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String toHex(byte[] bytes) {

        final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
        StringBuilder ret = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            ret.append(HEX_DIGITS[(bytes[i] >> 4) & 0x0f]);
            ret.append(HEX_DIGITS[bytes[i] & 0x0f]);
        }
        return ret.toString();
    }


}
