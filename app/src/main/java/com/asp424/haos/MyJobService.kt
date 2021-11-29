package com.asp424.haos




import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.text.SimpleDateFormat
import java.util.*


@Suppress("DEPRECATION")
class MyJobService : JobService() {
    private var jobCancelled = false
    override fun onStartJob(params: JobParameters): Boolean {
        Log.d(TAG, "Job started")
        doBackgroundWork(params)
        return true
    }

    private fun doBackgroundWork(params: JobParameters) {
        Thread(Runnable {
            disconnect()
            for (i in 0..9) {
                Log.d(TAG, "run: $i")
                if (jobCancelled) {
                    return@Runnable
                }
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            Log.d(TAG, "Job finished")
            jobFinished(params, false)
        }).start()
    }
    override fun onStopJob(params: JobParameters): Boolean {
        Log.d(TAG, "Job cancelled before completion")
        jobCancelled = true
        return true
    }
    companion object {
        private const val TAG = "ExampleJobService"
    }
    private fun addNotification() {
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val builder = NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.ic_baseline_electrical_services_24)
            .setContentTitle("Внимание!")
            .setContentText("Будет отключение электричества!")
            .setSound(defaultSoundUri)
        val notificationIntent = Intent(this, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder.setContentIntent(contentIntent)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(0, builder.build())
    }
    private fun disconnect()
    {
        val runnable = Runnable { get_info() }
        val startThread = Thread(runnable)
        startThread.start()
    }
    private fun get_info(){
        try {
            val doc: Document = Jsoup.connect("https://www.nesk.ru/otklyuchenie-elektroenergii/krymsk/").get()
            val phone: Elements = doc.getElementsByTag("tr")
            val streets: Elements = doc.getElementsByTag("p")
            val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)
            val date: String = formatter.format(Calendar.getInstance().time)
            val runnable = Runnable {
                var i = 0
                var d = 0
                do {
                    val ass: Element = phone.get(i)
                    val ass1:String = ass.text()
                    val ass2:String = phone.get(i).child(1).text()
                    if (ass1.contains("Офицерская"))
                    {
                        val time_up = formatter.parse(ass2)
                        val ass3 = formatter.parse(date)
                        Log.d("MyLog", time_up.toString())
                        Log.d("MyLog", ass3.toString())
                        if( time_up.compareTo(ass3) >= 0 ){
                            d++
                        }}
                    try {
                        Thread.sleep(300)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                    if (i == 40) {
                        break
                    }
                    i++
                }
                while (i < 40)
                if (d == 0){}
                else{addNotification()}
            }
            val startThread1 = Thread(runnable)
            startThread1.start()

        } catch (e: NumberFormatException){}
    }
}

