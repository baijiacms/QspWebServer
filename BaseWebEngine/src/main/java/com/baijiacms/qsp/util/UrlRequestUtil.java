package com.baijiacms.qsp.util;

import com.baijiacms.qsp.common.QspConstants;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cxy
 */
public class UrlRequestUtil {
    private String query;
    Map<String, String> param = null;

    public UrlRequestUtil(URL url) {
        this.query = url.getQuery();
        param = parse(query);
    }

    public String getParameter(String key) {
        return param.get(key);
    }

    public Map<String, String> parse(String url) {
        if (url == null || StringUtils.isEmpty(url)) {
            return new HashMap<>();
        }

        Map<String, String> entity = new HashMap<>();
        url = url.trim();
        try {
            url = URLDecoder.decode(url, QspConstants.CHARSET_STR);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //有参数
        String[] params = url.split("&");

        for (String param : params) {
            String[] keyValue = param.split("=");
            entity.put(keyValue[0], keyValue[1]);
        }

        return entity;
    }
}
