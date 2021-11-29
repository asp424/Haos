package com.asp424.haos



import android.annotation.SuppressLint
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    lateinit var tekst: TextView
    lateinit var listView: ListView
    lateinit var on: Button
    lateinit var off: Button
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var textView: TextView
    lateinit var progressBar: ProgressBar
    lateinit var imageView: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tekst = findViewById(R.id.textView)
        on = findViewById(R.id.button)
        off = findViewById(R.id.button3)
        textView = findViewById(R.id.textView3)
        progressBar = findViewById(R.id.progressBar)
        imageView = findViewById(R.id.imageView)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        listView = findViewById<ListView>(R.id.list)
        imageView.visibility = View.GONE
        testbt()
        disconnect()
        swipeRefreshLayout.setOnRefreshListener {
            disconnect()
            swipeRefreshLayout.isRefreshing = false
        }
        on.setOnClickListener {
            scheduleJob()
        }
        off.setOnClickListener {
            cancelJob()
        }
    }

    @Suppress("DEPRECATION")
    fun scheduleJob() {
        val componentName = ComponentName(this, MyJobService::class.java)
        val info = JobInfo.Builder(123, componentName)
            .setPersisted(true)
            .setPeriodic((180 * 60 * 1000).toLong())
            .build()
        val scheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        val resultCode = scheduler.schedule(info)
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            textView.text = "Уведомление включено"
            textView.setTextColor(resources.getColor(R.color.green))
            Log.d(TAG, "Job scheduled")
        } else {
            Log.d(TAG, "Job scheduling failed")
        }
    }

    @Suppress("DEPRECATION")
    fun cancelJob() {
        val scheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.cancel(123)
        Log.d(TAG, "Job cancelled")
        textView.text = "Уведомление отключено"
        textView.setTextColor(resources.getColor(R.color.red))
    }
    private fun disconnect()
    {
        imageView.visibility = View.GONE
        val runnable = Runnable { get_info() }
        val startThread = Thread(runnable)
        startThread.start()
    }
    @SuppressLint("SetTextI18n")
    private fun get_info() = try {
        val buses_rasp = ArrayList<String>()
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1, buses_rasp
        )
        runOnUiThread(){
            progressBar.visibility = View.VISIBLE
            listView.setAdapter(adapter)
        }
        val doc:Document = Jsoup.connect("https://www.nesk.ru/otklyuchenie-elektroenergii/krymsk/").get()
        val phone:Elements = doc.select("tr")
        val streets:Elements = doc.getElementsByTag("p")
        val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)
        val date: String = formatter.format(Calendar.getInstance().time)
        val runnable = Runnable {
            var i = 0
            var d = 0
            do {
                val ass: Element = phone[i]
                val ass1:String = ass.text()
                val ass2:String = phone[i].child(1).text()
                if (ass1.contains("Офицерская"))
                {
                    runOnUiThread {
                        val time_up = formatter.parse(ass2)
                        val ass3 = formatter.parse(date)
                        Log.d("MyLog", time_up.toString())
                        Log.d("MyLog", ass3.toString())
                        if(time_up >= ass3)
                        {

                            adapter.add(
                                "жжжжжжжжжжжжжжжжжжж" + "\n" + "\n" + "Дата: " + phone[i].child(1)
                                    .text() + "\n" + "\n" + "Место: " + phone[i].child(3).text() + "\n" + "\n" + "Время: " + phone[i].child(6)
                                    .text() + "\n" + "\n" + "жжжжжжжжжжжжжжжжжжж"
                            )
                            d = 1
                        }}}
                try {
                    Thread.sleep(300)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                i++
            }
            while (i < 40)
            if (d == 0){
                runOnUiThread(){

                    imageView.visibility = View.VISIBLE
                }}
            runOnUiThread(){
                progressBar.visibility = View.GONE
            }
        }
        val startThread1 = Thread(runnable)
        startThread1.start()
        runOnUiThread {
            tekst.text = streets[2].text() + ". Телефоны контакт-центра: +7(861) 944-77-40, +7(903) 411-77-40, 8-800-600-02-20"

        }
    } catch (e: NumberFormatException){}

    @Suppress("DEPRECATION")
    private fun testbt (){
        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val allPendingJobs = jobScheduler.allPendingJobs
        if (allPendingJobs.size > 0) {
            textView.text = "Уведомление включено"
            textView.setTextColor(resources.getColor(R.color.green))
        } else {
            textView.text = "Уведомление отключено"
            textView.setTextColor(resources.getColor(R.color.red))
        }
    }

}
