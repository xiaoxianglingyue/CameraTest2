package com.example.cameratest;/**
 * Created by fengyin on 16/8/25.
 */

import com.adasplus.data.AdasConfig;
import com.adasplus.data.FcwInfo;
import com.adasplus.data.LdwInfo;

/**
 * @author fengyin(email:594601408@qq.com)
 * @date 2016-08-25 09:29
 * @package com.example.cameratest
 * @description DrawInfo contains LdwInfo, FcwInfo and AdasConfig object and uses these attributes
 *              to draw in surfaceview.
 * @params
 */
public class DrawInfo {
    private LdwInfo ldwResults;
    private FcwInfo fcwResults;
    private AdasConfig config;

    public LdwInfo getLdwResults() {
        return ldwResults;
    }

    public void setLdwResults(LdwInfo ldwResults) {
        this.ldwResults = ldwResults;
    }

    public FcwInfo getFcwResults() {
        return fcwResults;
    }

    public void setFcwResults(FcwInfo fcwResults) {
        this.fcwResults = fcwResults;
    }

    public AdasConfig getConfig() {
        return config;
    }

    public void setConfig(AdasConfig config) {
        this.config = config;
    }
}
