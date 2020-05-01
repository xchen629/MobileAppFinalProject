package com.example.finalProject

import android.app.Activity
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
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_button_and_photo.*
import kotlinx.android.synthetic.main.activity_register.*
import java.util.jar.Manifest


//This Class is used to do the prototype of the play sound button and camera from the emulator
class ButtonAndPhoto : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    var playSound : MediaPlayer? = null
    //CODE for opening the camera
    private val PERMISSION_CODE = 1000
    private val IMAGE_CAPTURE_CODE = 1001
    //CODE for opening the gallery
    companion object{
        //image pick code
        private val IMAGE_PICK_CODE = 1000
        //Permission code
        private val PERMISSION_FOR_CHOOSE = 1001
    }

    var image_uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_button_and_photo)

        val photoChoice = listOf("Choose an option", "Take Photo", "Upload from Gallary", "Complete without picture")


        // Create an adapter with 3 parameters: activity (this), layout, list
        val myAdopter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, photoChoice)

        // set the adapter to spinner
        photo_select_spinner.adapter = myAdopter

        // set the onItemSelectedListener as (this).  (this) refers to this activity that implements OnItemSelectedListener interface
        photo_select_spinner.onItemSelectedListener = this
    }

    // The following two methods are callback methods of OnItemSelectedListener interface
    override fun onNothingSelected(parent: AdapterView<*>?) {
        //Callback method to be invoked when the selection disappears from this view. For example,
        // if the list becomes empty and the ArrayAdapter is notified, this callback will be invoked
        //Toast.makeText(this, "Nothing is selected!", Toast.LENGTH_SHORT).show()
    }

    //This allows user to select the picture choice from spinner
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        // Based on the index of position selected, set the corresponding image
        val imageId = when(position){
            0 -> null
            1 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(android.Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED) {

                    //Permission was not enabled
                    val permission = arrayOf( android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

                    //show popup to request permission
                    requestPermissions(permission, PERMISSION_CODE)
                }
                else {
                    //permission already granted
                    openCamera()
                }
            }
            else{
                openCamera()
            }

            2 -> if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_DENIED){
                    //Permission denied
                    val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)

                    //show popup to request runtime permission
                    requestPermissions(permissions, PERMISSION_FOR_CHOOSE)
                }
                else{
                    //permission already granted
                    pickImageFromGallery()
                }
            }
            else{
                //system os is marshmallow or below
                pickImageFromGallery();
            }

            else -> playSound()
        }
        image_view.setImageURI(image_uri)
    }

    private fun pickImageFromGallery(){
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }


    //Function that opens the camera app
    private fun openCamera(){
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        //camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    //function that shows the permission popup
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        //called when user presses ALLOW or  DENY from permission Request Popup
        when(requestCode){
            PERMISSION_CODE -> {
                if(grantResults.size > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED){
                    //permission from popup was granted
                    openCamera()
                    pickImageFromGallery()
                }
                else{
                    //permission from popup was denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //this sets the image to the image view
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //called when image was captured from camera intent
        if(resultCode == Activity.RESULT_OK){
            //set image captured to image view
            image_view.setImageURI(image_uri)
        }

        if(resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            image_view.setImageURI(data?.data)
        }
    }

    //function that plays the sound
    private fun playSound() {
        if (playSound == null){
            playSound = MediaPlayer.create(this, R.raw.song)
        }
        playSound?.start()
    }
}
