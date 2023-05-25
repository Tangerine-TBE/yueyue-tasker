package cn.com.auto.thkl.db.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class DeviceInfo {
    private String account; /*login name*/

    @Id(autoincrement = true)
    private Long id;

    @Generated(hash = 1431204482)
    public DeviceInfo(String account, Long id) {
        this.account = account;
        this.id = id;
    }

    @Generated(hash = 2125166935)
    public DeviceInfo() {
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


}
