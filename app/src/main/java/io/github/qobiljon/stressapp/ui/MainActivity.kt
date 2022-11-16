package io.github.qobiljon.stressapp.ui

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.github.qobiljon.stressapp.R
import io.github.qobiljon.stressapp.databinding.ActivityMainBinding
import io.github.qobiljon.stressapp.services.DataSubmissionService
import io.github.qobiljon.stressapp.services.OffBodyService
import io.github.qobiljon.stressapp.utils.Api
import io.github.qobiljon.stressapp.utils.Storage
import io.github.qobiljon.stressapp.utils.Utils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "StressApp"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var llAuthentication: LinearLayout
    private lateinit var llDateTime: LinearLayout
    private var isRunning = false

    private val offBodyEventReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val isOffBody = intent.getBooleanExtra("isOffBody", true)
            val tvOffBody = findViewById<TextView>(R.id.tvOffBody)

            if (isOffBody) {
                tvOffBody.text = getString(R.string.off_body)
                binding.root.background = AppCompatResources.getDrawable(applicationContext, R.drawable.orange_circle)
            } else {
                tvOffBody.text = getString(R.string.on_body)
                binding.root.background = AppCompatResources.getDrawable(applicationContext, R.drawable.green_circle)
            }
        }
    }

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        llAuthentication = findViewById(R.id.llAuthentication)
        llDateTime = findViewById(R.id.llDateTime)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnSignIn = findViewById<Button>(R.id.btnAuthenticate)

        btnSignIn.setOnClickListener {
            btnSignIn.isEnabled = false
            lifecycleScope.launch {
                val success = Api.signIn(
                    applicationContext,
                    email = etEmail.text.toString(),
                    password = etPassword.text.toString(),
                )
                if (success) {
                    llAuthentication.visibility = View.GONE
                    llDateTime.visibility = View.VISIBLE
                    btnSignIn.isEnabled = true

                    isRunning = true
                    runServices()

                    Utils.toast(applicationContext, getString(R.string.sign_in_success))
                } else {
                    btnSignIn.isEnabled = true
                    Utils.toast(applicationContext, getString(R.string.sign_in_failure))
                }
            }
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val intentAlarm = Intent(baseContext, MainActivity::class.java)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60 * 1000, PendingIntent.getBroadcast(this, 1, intentAlarm, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
    }

    override fun onResume() {
        super.onResume()

        if (Storage.isAuthenticated(applicationContext)) {
            llAuthentication.visibility = View.GONE
            llDateTime.visibility = View.VISIBLE

            if (!isRunning) {
                isRunning = true
                runServices()
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun runServices() {
        // off-body service
        startForegroundService(Intent(applicationContext, OffBodyService::class.java))
        startForegroundService(Intent(applicationContext, DataSubmissionService::class.java))
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(offBodyEventReceiver, IntentFilter("off-body-event"))

        GlobalScope.launch {
            val tvDate = findViewById<TextView>(R.id.tvDate)
            val tvTime = findViewById<TextView>(R.id.tvTime)

            while (true) {
                runOnUiThread {
                    val dateTime = DateTimeFormatter.ofPattern("MM.dd (EE), hh:mm a").format(LocalDateTime.now()).split(", ")
                    tvDate.text = dateTime[0]
                    tvTime.text = dateTime[1]
                }
                delay(1000)
            }
        }
    }
}