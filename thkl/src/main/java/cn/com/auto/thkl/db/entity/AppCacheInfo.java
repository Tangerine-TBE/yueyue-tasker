package cn.com.auto.thkl.db.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class AppCacheInfo {
    private String appInfoValue;
    private String appName;
    @Id(autoincrement = true)
    private Long id;

    @Generated(hash = 1204450796)
    public AppCacheInfo(String appInfoValue, String appName, Long id) {
        this.appInfoValue = appInfoValue;
        this.appName = appName;
        this.id = id;
    }

    @Generated(hash = 1175300001)
    public AppCacheInfo() {
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppInfoValue() {
        return appInfoValue;
    }

    public void setAppInfoValue(String appInfoValue) {
        this.appInfoValue = appInfoValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
