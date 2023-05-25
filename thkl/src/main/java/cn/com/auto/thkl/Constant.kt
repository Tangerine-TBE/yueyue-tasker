package cn.com.auto.thkl

object Constant {
    const val BOOT_TEST =
        "function main(){\n" + "    let  testBtn =  text(\"测试\").findOne()\n" + "    if(testBtn != null){\n" + "        testBtn.click()\n" + "    }\n" + "}\n" + "main();"
    const val BOOT_START_SCRIPT = "main();" + "function main(){" + "}\n"

    const val Exception =
        "main();\n" + "function main(){\n" + "\n" + "try{\n" + "\tthrow \"THKLException\"\t\n" + "}catch(e){\n" + "\t\t\n" + "}\n" + "\n" + "console.log(\"jixu\")\n" + "\n" + "}"
    const val BASE_URL_DEBUG =
//        "http://192.168.31.24:8060/"
        "http://192.168.31.52:8060/"
    const val BASE_URL_RELEASE =
        "http://szthkj.f3322.net:20960/"

    /*would share to javaScript*/
    const val TOKEN = "token"

    /*would share to javaScript*/
    const val USER_ID = "user_id"

    /*would share to javaScript*/
    const val USER_NAME = "user_name"

    /*would share to javaScript*/
    const val APP_ID = "app_id"

    /*would share to javaScript*/
    const val DEVICE_ID = "device_id"

    /*would share to javaScript*/
    const val SCRIPT_ID = "script_id"

    const val EXPIRY_TIME = "expiryTime"

    const val LOGIN_NAME = "login_name"

    /*would share to javaScript*/
    const val account = "account"
    const val cipher = "cipher"

    const val FIRST_LOGIN = "first_login"
    const val PERMISSION = "permission"
    const val AUTO_START = "auto_start"

    const val SCRIPT_APP = "scriptApp"//维护
    const val UPTIME = "up_time"
    const val EXIT = "exit"
    const val IP = "ip"
    const val FIRST_INSTALL = "first_install"


}