package com.example.mytaskagora


// for messaging
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.SurfaceView
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.mytaskagora.databinding.ActivityMainBinding
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas


class MainActivity : AppCompatActivity() {
//    enables the view binding
    private lateinit var binding: ActivityMainBinding

//    a4341a0a606b4b268b185288e9f06fac
    private val appId = "a4341a0a606b4b268b185288e9f06fac"
    private val channelName = "rakesh"


    private val tokenName = "007eJxTYHhc/8PH4zunx+YfTlWtd74+4klsOnHBsJ9HNC+iQKf+yh0FhkQTYxPDRINEMwOzJJMkIzOLJEMLUyMLi1TLNAOztMTkN84bUxoCGRl4nu9kZmSAQBCfjaEoMTu1OIOBAQD+7SG3"

//  007eJxTYLBSqP0/4Yp9/SyF9afOC9z3uPb7zOI55/eeOOCbseK31GN7BYZEE2MTw0SDRDMDsySTJCMziyRDC1MjC4tUyzQDs7TEZCXLtSkNgYwM9l/KWBkZIBDEZ2MoSsxOLc5gYAAAaW8ixQ==
    private val uId = 0

    private var isJoined = false
    private var agoraEngine : RtcEngine? = null
    private var localSurfaceView: SurfaceView? = null
    private var remoteSurfaceView: SurfaceView? = null






//    this line add by me
fun showIncomingCallNotification() {
    val channelId = "incoming_call_channel"
    val notificationId = 1

    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)
    }

    val intent = Intent(applicationContext, channelId::class.java)
    val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

    val notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
        .setSmallIcon(R.drawable.baseline_call_24)
        .setContentTitle("Incoming Call")
        .setContentText("You have an incoming call")
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)

    notificationManager.notify(notificationId, notificationBuilder.build())
}








    private val PERMISSION_ID = 12
    private val REQUESTED_PERMISSION =
        arrayOf(
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.CAMERA
        )


    private fun checkSelfPermission(): Boolean{
        return !(ContextCompat.checkSelfPermission(
            this,REQUESTED_PERMISSION[0]
        ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,REQUESTED_PERMISSION[1]
                ) != PackageManager.PERMISSION_GRANTED)
    }

    private fun showMessage(msg: String)
    {
        runOnUiThread{
            Toast.makeText(this,msg,Toast.LENGTH_SHORT).show()
        }
    }





    private fun setUpVideoSdkEngine(){
        try {
            val config = RtcEngineConfig()
            config.mContext = baseContext
            config.mAppId = appId
            config.mEventHandler = mRtcEventHandler
            agoraEngine = RtcEngine.create(config)
            agoraEngine!!.enableVideo()

        }catch (e:Exception)
        {
            e.message?.let { showMessage(it) }
        }

    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        if (!checkSelfPermission())
        {
             ActivityCompat
                 .requestPermissions(
                     this,REQUESTED_PERMISSION, PERMISSION_ID

                 )

        }





//        setup videoSdkEngine
        setUpVideoSdkEngine()
        binding.joinButton.setOnClickListener {
            joinCall()
            showIncomingCallNotification()

        }


        binding.leaveButton.setOnClickListener {
            leaveCall()
        }

    }



    override fun onDestroy() {
        super.onDestroy()
        agoraEngine!!.stopPreview()
        agoraEngine!!.leaveChannel()

        Thread{
            RtcEngine.destroy()
            agoraEngine = null
        }.start()


    }
    private fun leaveCall() {
        if (!isJoined) {
            showMessage("Please join a channel first")
        }else {
            agoraEngine!!.leaveChannel()
            showMessage("You left the channel")
            if (remoteSurfaceView!=null) remoteSurfaceView!!.visibility = GONE
            if (localSurfaceView!=null) localSurfaceView!!.visibility = GONE
            isJoined = false
        }

    }

    private fun joinCall() {
        if (checkSelfPermission())
        {
            val option = ChannelMediaOptions()
            option.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            option.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            setupLocalVideo()
            localSurfaceView!!.visibility = VISIBLE
            agoraEngine!!.startPreview()
            agoraEngine!!.joinChannel(tokenName,channelName,uId,option)
        }
        else
        {
            showMessage("permission not granted")
        }

    }
    private val mRtcEventHandler: IRtcEngineEventHandler  =
        object: IRtcEngineEventHandler() {
            override fun onUserJoined(uid: Int, elapsed: Int) {
                showMessage("Remote user joined $uid")
                runOnUiThread{setupRemoteVideo(uid)}
            }

            override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                isJoined = true
                showMessage("Joined channel $channel")

            }

            override fun onUserOffline(uid: Int, reason: Int) {
                showMessage("User offline")
                runOnUiThread{
                    remoteSurfaceView!!.visibility = GONE
                }
            }



            private fun setupRemoteVideo(uid: Int){
                remoteSurfaceView = SurfaceView(baseContext)
                remoteSurfaceView!!.setZOrderMediaOverlay(true)
                binding.remoteUser.addView(remoteSurfaceView)

                agoraEngine!!.setupRemoteVideo(
                    VideoCanvas(
                        remoteSurfaceView,
                        VideoCanvas.RENDER_MODE_FIT,
                        uid
                    )
                )
            }

    }



    private fun setupLocalVideo(){
        localSurfaceView = SurfaceView(baseContext)
        binding.localUser.addView(localSurfaceView)

        agoraEngine!!.setupLocalVideo(
            VideoCanvas(
                localSurfaceView,
                VideoCanvas.RENDER_MODE_FIT,
                0
            )
        )
    }
}

