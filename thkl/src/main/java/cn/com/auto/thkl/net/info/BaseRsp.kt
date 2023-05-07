package cn.com.auto.thkl.net.info

data class BaseRsp(val model:String, val msg:String, val msgDetail:String, val success:Boolean, val obj:Any){
    override fun toString(): String {
        return "BaseRsp(model='$model', msg='$msg', msgDetail='$msgDetail', success=$success, obj=$obj)"
    }
}