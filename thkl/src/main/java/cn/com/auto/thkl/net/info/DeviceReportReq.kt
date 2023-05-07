package cn.com.auto.thkl.net.info

data class DeviceReportReq(
    val deviceId: Int,//设备Id
    val electricity: Double,//设备的电量
    val queryExecute: Boolean,//是否设备执行命令（true->查询）
    val remark: String,//备注
    val signal: String,//设备信号
    val state: Boolean
)//设备的状态（true->在线，false离线）
{

    override fun toString(): String {
        return "DeviceReportReq(deviceId=$deviceId, electricity=$electricity, queryExecute=$queryExecute, remark='$remark', signal='$signal', state=$state)"
    }
}
