package cn.com.auto.thkl.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.stardust.autojs.runtime.api.Device;

import java.util.List;

import cn.com.auto.thkl.db.entity.DeviceInfo;
import cn.com.auto.thkl.db.entity.LoginInfo;

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



    public static void removeAccount(String account) {
        LoginInfo loginInfo = sDaoSession.getLoginInfoDao().queryBuilder().where(LoginInfoDao.Properties.Account.eq(account)).unique();
        if (loginInfo != null) {
            sDaoSession.getLoginInfoDao().delete(loginInfo);
        }
    }

    //    public static boolean findStatusWithLogin(String account){
//        List<DeviceInfo> list = sDaoSession.getDeviceInfoDao().queryBuilder().where(DeviceInfoDao.Properties.Account.eq(account)).build().list();
//        if (list.isEmpty()){
//            return true;
//        }else{
//
//        }
//
//
//    }
    public static void addStatusWithLogin(String account) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setAccount(account);
        sDaoSession.getDeviceInfoDao().insert(deviceInfo);
    }

    public static boolean findStatusWithLogin(String account) {
        List<DeviceInfo> list = sDaoSession.getDeviceInfoDao().queryBuilder().where(DeviceInfoDao.Properties.Account.eq(account)).build().list();
        return !list.isEmpty();
    }

    public static void addAccount(String account) {
        long time = System.currentTimeMillis() / 1000;
        String sql = "Insert INTO LOGIN_INFO(ACCOUNT,TIME) values (?,?)";
        sDaoSession.getDatabase().execSQL(sql, new String[]{account, String.valueOf(time)});
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
