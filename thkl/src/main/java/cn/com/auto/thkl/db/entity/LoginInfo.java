package cn.com.auto.thkl.db.entity;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class LoginInfo {
    @Id(autoincrement = true)
    private Long id;
    private String account;/* login name*/
    private Long time; /*update time*/

    @Generated(hash = 1399828035)
    public LoginInfo(Long id, String account, Long time) {
        this.id = id;
        this.account = account;
        this.time = time;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @Generated(hash = 1911824992)
    public LoginInfo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }
}
