package com.github.ltprc.gamepal.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class ContentUtil {

    public static JSONObject request2JSONObject(HttpServletRequest request) throws IOException {
        BufferedReader br = request.getReader();
        String str = "";
        String listString = "";
        while ((str = br.readLine()) != null) {
            listString += str;
        }
        JSONObject req = JSON.parseObject(listString);
        return req.getJSONObject("body");
    }

    public static JSONObject generateRst() {
        JSONObject rst = new JSONObject();
        rst.put("code", ErrorUtil.CODE_SUCCESS);
        return rst;
    }

    public static JSONObject jsonFile2JSONObject(String filePath) {
        try {
            File jsonFile = new File(filePath);
            Reader reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            reader.close();
            return (JSONObject) JSONObject.parse(sb.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static JSONArray jsonFile2JSONArray(String filePath) {
        try {
            File jsonFile = new File(filePath);
            Reader reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            reader.close();
            return (JSONArray) JSONArray.parse(sb.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^-?\\d+$");
        return pattern.matcher(str).matches();
    }
}
