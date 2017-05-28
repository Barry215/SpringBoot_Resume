package com.frank.service.impl;

import com.frank.service.QiniuUpService;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.springframework.stereotype.Service;

/**
 * Created by frank on 17/5/28.
 */
@Service
public class QiniuUpServiceImpl implements QiniuUpService {
    private static final String ACCESSKey = "AQyqTUgn06_Z-b-WF_5QwQqJTIRIg0dlzxWhNn35";
    private static final String SECRETKey = "Hy_yD1ETkM_wHqNi1Vdmmp4AH28MfzbR_Qh4Hn-3";
    private static final String BUCKET = "blog";
    private static final String URL = "http://oqnj2q3j3.bkt.clouddn.com/";


    @Override
    public String getUpToken() {
        Auth auth = Auth.create(ACCESSKey, SECRETKey);
        StringMap putPolicy = new StringMap();
        putPolicy.put("returnBody", "{\"key\":\""+URL+"$(key)\",\"bucket\":\"$(bucket)\",\"fsize\":$(fsize)}");
        long expireSeconds = 3600;
        return auth.uploadToken(BUCKET, null, expireSeconds, putPolicy);
    }
}
