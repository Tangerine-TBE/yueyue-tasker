package cn.com.auto.thkl.db.entity;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class LoginInfo {
    @Id(autoincrement = true)
    private Long id;
    private String account;

    @Generated(hash = 865373609)
    public LoginInfo(Long id, String account) {
        this.id = id;
        this.account = account;
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
