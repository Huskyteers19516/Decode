package org.firstinspires.ftc.teamcode

import android.content.Context
import com.qualcomm.ftccommon.FtcEventLoop
import org.firstinspires.ftc.ftccommon.external.OnCreateEventLoop
import org.psilynx.psikit.ftc.autolog.PsiKitAutoLogSettings


object PsiKitConfig {
    @JvmStatic
    @OnCreateEventLoop
    fun configure(context: Context, ftcEventLoop: FtcEventLoop) {
        PsiKitAutoLogSettings.enabledByDefault = true
        PsiKitAutoLogSettings.enableLinearByDefault = true
//        System.setProperty(PsiKitAutoLogSettings.PROPERTY_RLOG_PORT, "5900")
        System.setProperty(PsiKitAutoLogSettings.PROPERTY_RLOG_FOLDER, "/sdcard/FIRST/PsiKit/")
    }
}