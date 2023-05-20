package cn.com.auto.thkl.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import cn.com.auto.thkl.App;
import cn.com.auto.thkl.db.entity.AppCacheInfo;
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
        long time = System.currentTimeMillis() / 1000;
        String sql = "Insert INTO LOGIN_INFO(ACCOUNT,TIME) values (?,?)";
        sDaoSession.getDatabase().execSQL(sql, new String[]{account, String.valueOf(time)});
    }
    public static void removeAccount(String account){
        LoginInfo loginInfo = sDaoSession.getLoginInfoDao().queryBuilder().where(LoginInfoDao.Properties.Account.eq(account)).unique();
        if (loginInfo != null){
            sDaoSession.getLoginInfoDao().delete(loginInfo);
        }
    }

    public static void insertAppCacheInfo(String json, String appName) {
        AppCacheInfo appCacheInfo = sDaoSession.getAppCacheInfoDao().queryBuilder().where(AppCacheInfoDao.Properties.AppName.eq(appName)).unique();
        if (appCacheInfo != null) {
            sDaoSession.getAppCacheInfoDao().delete(appCacheInfo);
        }
        appCacheInfo = new AppCacheInfo();
        appCacheInfo.setAppInfoValue(json);
        appCacheInfo.setAppName(appName);
        sDaoSession.getAppCacheInfoDao().insert(appCacheInfo);
    }

    public static void removeAppCacheInfo(String appName) {
        AppCacheInfo appCacheInfo = sDaoSession.getAppCacheInfoDao().queryBuilder().where(AppCacheInfoDao.Properties.AppName.eq(appName)).unique();
        if (appCacheInfo != null) {
            sDaoSession.getAppCacheInfoDao().delete(appCacheInfo);
        }
    }

    public static List<AppCacheInfo> findAllAppCacheInfo() {
        return sDaoSession.getAppCacheInfoDao().queryBuilder().list();
    }

    public static int findLastLoginInfo() {
        List<LoginInfo> list = sDaoSession.getLoginInfoDao().queryBuilder().orderDesc(LoginInfoDao.Properties.Time).build().list();
        if (list.isEmpty()) {
            return 0;
        }
        if (list.size() < 2) {
            return 0;
        }
        LoginInfo latest = list.get(0);
        LoginInfo last = list.get(1);
        if (latest.getAccount().equals(last.getAccount())) {
            return 1;
        } else {
            return 2;
        }
    }
}
