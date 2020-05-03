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

    private val CAMERA_REQUEST = 1000
    private val PERMISSION_PICK_IMAGE = 1001
    internal var filePath: Uri? = null

    //Dialog
    lateinit var dialog: AlertDialog

    //Firebase
    lateinit var storage: FirebaseStorage
    lateinit var storageReference: StorageReference

    var playSound : MediaPlayer? = null

    //CODE for opening the gallery

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_button_and_photo)
        //Firebase reference
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference
        dialog = SpotsDialog.Builder().setCancelable(false).setContext(this).build()

        val clickable_imageview = findViewById<ImageView>(R.id.image_view)

        clickable_imageview.setOnClickListener{
            showDialog()
        }

        btnUpload.setOnClickListener { v ->
            uploadImage()
        }
    }

    //Upload image to the firebase storage
    private fun uploadImage() {
        if (filePath != null){
            dialog.show()
            val reference = storageReference.child("images/" + UUID.randomUUID().toString())
            reference.putFile(filePath!!).addOnSuccessListener { taskSnapshot ->
                dialog.dismiss()
                Toast.makeText(this@ButtonAndPhoto, "Uploaded!", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener{e ->
                dialog.dismiss()
                Toast.makeText(this@ButtonAndPhoto, "Failed!", Toast.LENGTH_SHORT).show()
            }.addOnProgressListener {taskSnapshot ->
                val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                dialog.setMessage("Uploaded $progress")
            }
        }
    }

    //Choose image from the gallery
    private fun chooseImage() {
        Dexter.withActivity(this).withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if(report!!.areAllPermissionsGranted()){
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "image/*"
                    startActivityForResult(Intent.createChooser(intent, "Select Image"), PERMISSION_PICK_IMAGE)
                }
                else{
                    Toast.makeText(this@ButtonAndPhoto, "Permission Denied!", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                token!!.continuePermissionRequest()
            }
        }).check()
    }

    //Opens the camera
    private fun openCamera() {
        Dexter.withActivity(this).withPermissions(android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if(report!!.areAllPermissionsGranted()){
                    val values = ContentValues()
                    values.put(MediaStore.Images.Media.TITLE, "New Picture")
                    values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
                    filePath = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, filePath)
                    startActivityForResult(cameraIntent, CAMERA_REQUEST)
                }
                else{
                    Toast.makeText(this@ButtonAndPhoto, "Permission Denied!", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                token!!.continuePermissionRequest()
            }
        }).check()
    }

    //this sets the image to the image view
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PERMISSION_PICK_IMAGE) {
                if (data != null) {
                    if (data.data != null) {
                        filePath = data.data
                        try {
                            val bitmap =
                                MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                            image_view.setImageBitmap(bitmap)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            if (requestCode == CAMERA_REQUEST) {
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                    image_view.setImageBitmap(bitmap)
                }
                catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun showDialog() {
        // Create an alertdialog builder object,
        // then set attributes that you want the dialog to have
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("What would you like to do?")
        builder.setMessage("Choose an option below to complete with the task!")

        // Open the camera if user choose this option
        builder.setPositiveButton("Picture from Camera"){ dialog, which ->
            openCamera()
        }
        //Open up the gallery if user choose this option
        builder.setNegativeButton("Upload from gallery"){dialog, which ->
            chooseImage()
        }
        //Play the sound if user choose this button
        builder.setNeutralButton("No picture"){dialog, which ->
            playSound()
        }
        // create the dialog and show it
        val dialog = builder.create()
        dialog.show()
    }

    //function that plays the sound
    private fun playSound() {
        if (playSound == null){
            playSound = MediaPlayer.create(this, R.raw.song)
        }
        playSound?.start()
    }
}

