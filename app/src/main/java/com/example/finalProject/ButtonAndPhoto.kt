package com.example.finalProject

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import dmax.dialog.SpotsDialog
import io.grpc.Context
import kotlinx.android.synthetic.main.activity_button_and_photo.*
import kotlinx.android.synthetic.main.activity_register.*
import java.io.IOException
import java.util.*

//This Class is used to do the prototype of the play sound button and camera from the emulator
class ButtonAndPhoto : AppCompatActivity() {



    var playSound : MediaPlayer? = null

    //CODE for opening the gallery

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_button_and_photo)
        //Firebase reference
        //storage = FirebaseStorage.getInstance()
       // storageReference = storage.reference
       // dialog = SpotsDialog.Builder().setCancelable(false).setContext(this).build()

        val clickable_imageview = findViewById<ImageView>(R.id.image_view)

        clickable_imageview.setOnClickListener{
            //showDialog()
        }

        btnUpload.setOnClickListener { v ->
            //uploadImage()
        }
    }




}

