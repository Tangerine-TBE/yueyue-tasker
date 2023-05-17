package cn.com.auto.thkl.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import cn.com.auto.thkl.db.entity.LoginInfo;
import cn.com.auto.thkl.utils.L;

public class DaoTool {
    public static DaoSession sDaoSession;
    private static DaoMaster daoMaster;

    public static void init(Context ctx) {
        THklOpenHelper helper = new THklOpenHelper(ctx, "thkl.db");
        SQLiteDatabase db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        sDaoSession = daoMaster.newSession();
    }

    public static boolean findStr(String account) {
        List<LoginInfo> loginInfoList = sDaoSession.getLoginInfoDao().queryBuilder().where(LoginInfoDao.Properties.Account.eq(account)).build().list();
        return !loginInfoList.isEmpty();
    }
    public static void addAccount(String account) {
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setAccount(account);
        sDaoSession.getLoginInfoDao().insertOrReplace(loginInfo);
    }
}
