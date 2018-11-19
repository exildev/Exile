package co.com.exile.exile.report

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import co.com.exile.exile.BaseActivity
import co.com.exile.exile.R
import co.com.exile.exile.network.VolleySingleton
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.liuguangqiang.ipicker.IPicker
import moe.feng.common.stepperview.VerticalStepperItemView
import net.gotev.uploadservice.MultipartUploadRequest
import net.gotev.uploadservice.ServerResponse
import net.gotev.uploadservice.UploadInfo
import net.gotev.uploadservice.UploadNotificationConfig
import net.gotev.uploadservice.UploadStatusDelegate
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.FileNotFoundException
import java.net.MalformedURLException
import java.util.ArrayList

class ReportActivity : BaseActivity(), GoogleApiClient.ConnectionCallbacks, LocationListener {
    private var typesString: MutableList<String> = mutableListOf()
    private var mTypes = JSONArray()
    private var placesString: MutableList<String> = mutableListOf()
    private var mPlaces = JSONArray()
    private var clientsString: MutableList<String> = mutableListOf()
    private var mClients = JSONArray()
    private lateinit var attaches: ArrayList<String>
    private val mSteppers = arrayOfNulls<VerticalStepperItemView>(5)
    private var mGoogleClient: GoogleApiClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var mLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        attaches = ArrayList()
        IPicker.setLimit(MAX_UPLOAD_FILES)
        IPicker.setOnSelectedListener { paths -> addAttaches(paths) }
        getTypes()
        getPlaces()
        getClients()

        mGoogleClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build()
        mGoogleClient!!.connect()

        setStepper()
    }

    private fun setStepper() {

        mSteppers[0] = findViewById(R.id.stepper_0)
        mSteppers[1] = findViewById(R.id.stepper_1)
        mSteppers[2] = findViewById(R.id.stepper_2)
        mSteppers[3] = findViewById(R.id.stepper_3)
        mSteppers[4] = findViewById(R.id.stepper_4)

        VerticalStepperItemView.bindSteppers(*mSteppers)

        mSteppers[0]?.setOnClickListener {
            mSteppers[0]?.state = VerticalStepperItemView.STATE_SELECTED
            mSteppers[1]?.state = VerticalStepperItemView.STATE_NORMAL
            mSteppers[2]?.state = VerticalStepperItemView.STATE_NORMAL
            mSteppers[3]?.state = VerticalStepperItemView.STATE_NORMAL
            mSteppers[4]?.state = VerticalStepperItemView.STATE_NORMAL
        }

        mSteppers[1]?.setOnClickListener {
            val nombre = findViewById<TextInputEditText>(R.id.name_et)
            if (mSteppers[0]?.state == VerticalStepperItemView.STATE_SELECTED && nombre.text.toString() == "") {
                return@setOnClickListener
            }
            mSteppers[0]?.state = VerticalStepperItemView.STATE_DONE
            mSteppers[1]?.state = VerticalStepperItemView.STATE_SELECTED
            mSteppers[2]?.state = VerticalStepperItemView.STATE_NORMAL
            mSteppers[3]?.state = VerticalStepperItemView.STATE_NORMAL
            mSteppers[4]?.state = VerticalStepperItemView.STATE_NORMAL
        }

        mSteppers[2]?.setOnClickListener {
            val nombre = findViewById<TextInputEditText>(R.id.name_et)
            if (mSteppers[0]?.state == VerticalStepperItemView.STATE_SELECTED && nombre.text.toString() == "") {
                return@setOnClickListener
            }
            mSteppers[0]?.state = VerticalStepperItemView.STATE_DONE
            mSteppers[1]?.state = VerticalStepperItemView.STATE_DONE
            mSteppers[2]?.state = VerticalStepperItemView.STATE_SELECTED
            mSteppers[3]?.state = VerticalStepperItemView.STATE_NORMAL
            mSteppers[4]?.state = VerticalStepperItemView.STATE_NORMAL
        }


        mSteppers[3]?.setOnClickListener {
            val nombre = findViewById<TextInputEditText>(R.id.name_et)
            if (mSteppers[0]?.state == VerticalStepperItemView.STATE_SELECTED && nombre.text.toString() == "") {
                return@setOnClickListener
            }
            mSteppers[0]?.state = VerticalStepperItemView.STATE_DONE
            mSteppers[1]?.state = VerticalStepperItemView.STATE_DONE
            mSteppers[2]?.state = VerticalStepperItemView.STATE_DONE
            mSteppers[3]?.state = VerticalStepperItemView.STATE_SELECTED
            mSteppers[4]?.state = VerticalStepperItemView.STATE_NORMAL
        }

        mSteppers[4]?.setOnClickListener {
            val nombre = findViewById<TextInputEditText>(R.id.name_et)
            if (mSteppers[0]?.state == VerticalStepperItemView.STATE_SELECTED && nombre.text.toString() == "") {
                return@setOnClickListener
            }
            mSteppers[0]?.state = VerticalStepperItemView.STATE_DONE
            mSteppers[1]?.state = VerticalStepperItemView.STATE_DONE
            mSteppers[2]?.state = VerticalStepperItemView.STATE_DONE
            mSteppers[3]?.state = VerticalStepperItemView.STATE_DONE
            mSteppers[4]?.state = VerticalStepperItemView.STATE_SELECTED
        }

        val mNextBtn0 = findViewById<Button>(R.id.button_next_0)
        mNextBtn0.setOnClickListener {
            val nombre = findViewById<TextInputEditText>(R.id.name_et)
            val layout = findViewById<TextInputLayout>(R.id.name_il)
            if (nombre.text.toString() != "") {
                mSteppers[0]?.nextStep()
                mSteppers[0]?.summary = nombre.text.toString()
                layout.error = null
            } else {
                layout.error = "Este campo es obligatorio"
            }
        }

        val mPrevBtn1 = findViewById<Button>(R.id.button_prev_1)
        mPrevBtn1.setOnClickListener { mSteppers[1]?.prevStep() }

        val mNextBtn1 = findViewById<Button>(R.id.button_next_1)
        mNextBtn1.setOnClickListener {
            val spinner = findViewById<Spinner>(R.id.type_spinner)
            if (spinner.selectedItemPosition > 0) {
                mSteppers[1]?.setSummary(spinner.selectedItem.toString())
            } else {
                mSteppers[1]?.setSummary(null)
            }
            mSteppers[1]?.nextStep()
        }

        val mPrevBtn2 = findViewById<Button>(R.id.button_prev_2)
        mPrevBtn2.setOnClickListener { mSteppers[2]?.prevStep() }

        val mNextBtn2 = findViewById<Button>(R.id.button_next_2)
        mNextBtn2.setOnClickListener {
            val spinner = findViewById<Spinner>(R.id.place_spinner)
            if (spinner.selectedItemPosition > 0) {
                mSteppers[2]?.setSummary(spinner.selectedItem.toString())
            } else {
                mSteppers[2]?.setSummary(null)
            }
            mSteppers[2]?.nextStep()
        }

        val mPrevBtn3 = findViewById<Button>(R.id.button_prev_3)
        mPrevBtn3.setOnClickListener { mSteppers[3]?.prevStep() }

        val mNextBtn3 = findViewById<Button>(R.id.button_next_3)
        mNextBtn3.setOnClickListener {
            val spinner = findViewById<Spinner>(R.id.client_spinner)
            if (spinner.selectedItemPosition > 0) {
                mSteppers[3]?.setSummary(spinner.selectedItem.toString())
            } else {
                mSteppers[3]?.setSummary(null)
            }
            mSteppers[3]?.nextStep()
        }

        val mPrevBtn4 = findViewById<Button>(R.id.button_prev_4)
        mPrevBtn4.setOnClickListener { mSteppers[4]?.prevStep() }

        val mNextBtn4 = findViewById<Button>(R.id.button_next_4)
        mNextBtn4.setOnClickListener {
            val desc = findViewById<TextInputEditText>(R.id.description_et)
            val layout = findViewById<TextInputLayout>(R.id.description_il)
            if (desc.text.toString() != "") {
                mSteppers[4]?.summary = desc.text.toString()
                layout.error = null
                mSteppers[4]?.state = VerticalStepperItemView.STATE_DONE
                try {
                    if (attaches.size > 0) {
                        sendWithFiles()
                    } else {
                        send()
                    }
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }

            } else {
                layout.error = "Este campo es obligatorio"
            }
            mSteppers[4]?.nextStep()
        }
    }

    public override fun onDestroy() {
        if (mGoogleClient!!.isConnected) {
            stopLocationUpdates()
        }
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.nav_attach) {
            IPicker.open(this, attaches)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION || requestCode == PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                validPermissions()
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(R.string.gps_permissions_message)
                        .setCancelable(false)
                        .setPositiveButton("Si") { _, _ -> validPermissions() }
                        .setNegativeButton("Cerrar") { _, _ -> }
                val alert = builder.create()
                alert.show()
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == REQUEST_LOCATION_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                startLocationUpdates()
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(R.string.activate_gps_message)
                        .setCancelable(false)
                        .setPositiveButton("Si") { _, _ -> createLocationRequest() }
                        .setNegativeButton("Cerrar") { _, _ -> }
                val alert = builder.create()
                alert.show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun getTypes() {
        val serviceUrl = getString(R.string.report_type_url)
        val url = getUrl(serviceUrl)
        getTypes(url, 0)
    }

    private fun getTypes(url: String, offset: Int) {
        val request = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener { response ->
                    Log.i("response", response.toString())
                    try {
                        val types = response.getJSONArray("object_list")
                        for (i in 0 until types.length()) {
                            val type = types.getJSONObject(i)
                            typesString.add(type.getString("nombre"))
                            mTypes.put(type)
                        }

                        if (response.has("next")) {
                            val serviceUrl = getString(R.string.report_type_url)
                            val url = getUrl(serviceUrl) + "?page=" + response.getInt("next")
                            getTypes(url, offset + response.getInt("num_rows"))
                        } else {
                            renderTypes()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener { error ->
                    if (error.networkResponse != null && (error.networkResponse.statusCode == 404 || error.networkResponse.statusCode == 400)) {
                        Snackbar.make(findViewById(R.id.toolbar), "usuario y/o contraseña incorrecta", 800).show()
                    } else {
                        Snackbar.make(findViewById(R.id.toolbar), "Error al hacer la consulta", 800).show()
                    }
                })
        VolleySingleton.getInstance(this).addToRequestQueue(request)
    }

    private fun getPlaces() {
        val serviceUrl = getString(R.string.report_places_url)
        val url = getUrl(serviceUrl)
        getPlaces(url, 0)

    }

    private fun getPlaces(url: String, offset: Int) {
        val request = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener { response ->
                    Log.i("response", response.toString())
                    try {
                        val types = response.getJSONArray("object_list")
                        for (i in 0 until types.length()) {
                            val type = types.getJSONObject(i)
                            placesString.add(type.getString("nombre"))
                            mPlaces.put(type)
                        }

                        if (response.has("next")) {
                            val serviceUrl = getString(R.string.report_places_url)
                            val url = getUrl(serviceUrl) + "?page=" + response.getInt("next")
                            getPlaces(url, offset + response.getInt("num_rows"))
                        } else {
                            renderPlaces()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener { error ->
                    if (error.networkResponse != null && (error.networkResponse.statusCode == 404 || error.networkResponse.statusCode == 400)) {
                        Snackbar.make(findViewById(R.id.toolbar), "usuario y/o contraseña incorrecta", 800).show()
                    } else {
                        Snackbar.make(findViewById(R.id.toolbar), "Error al hacer la consulta", 800).show()
                    }
                })
        VolleySingleton.getInstance(this).addToRequestQueue(request)
    }

    private fun getClients() {
        val serviceUrl = getString(R.string.report_clients_url)
        val url = getUrl(serviceUrl)
        getClients(url, 0)

    }

    private fun getClients(url: String, offset: Int) {
        val request = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener { response ->
                    Log.i("response", response.toString())
                    try {

                        val clients = response.getJSONArray("object_list")
                        for (i in 0 until clients.length()) {
                            val client = clients.getJSONObject(i)
                            clientsString.add(client.getString("nombre"))
                            mClients.put(client)
                        }

                        if (response.has("next")) {
                            val serviceUrl = getString(R.string.report_clients_url)
                            val url = getUrl(serviceUrl) + "?page=" + response.getInt("next")
                            getClients(url, offset + response.getInt("num_rows"))
                        } else {
                            renderClients()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener { error ->
                    if (error.networkResponse != null && (error.networkResponse.statusCode == 404 || error.networkResponse.statusCode == 400)) {
                        Snackbar.make(findViewById(R.id.toolbar), "usuario y/o contraseña incorrecta", 800).show()
                    } else {
                        Snackbar.make(findViewById(R.id.toolbar), "Error al hacer la consulta", 800).show()
                    }
                })
        VolleySingleton.getInstance(this).addToRequestQueue(request)
    }

    private fun renderTypes() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, typesString)
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        val spinner = findViewById<Spinner>(R.id.type_spinner)
        spinner.adapter = adapter
    }

    private fun renderPlaces() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, placesString)
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        val spinner = findViewById<Spinner>(R.id.place_spinner)
        spinner.adapter = adapter
    }

    private fun renderClients() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, clientsString)
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        val spinner = findViewById<Spinner>(R.id.client_spinner)
        spinner.adapter = adapter
    }

    private fun addAttaches(paths: List<String>) {
        attaches.clear()
        attaches.addAll(paths)
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 10000
        mLocationRequest!!.fastestInterval = 5000
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY


        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest!!)

        val result = LocationServices.SettingsApi.checkLocationSettings(mGoogleClient,
                builder.build())


        result.setResultCallback { result ->
            val status = result.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> {
                    // All location settings are satisfied. The client can
                    // initialize location requests here.
                    Log.i("settings", "si tal")
                    startLocationUpdates()
                }
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        status.startResolutionForResult(
                                this@ReportActivity,
                                REQUEST_LOCATION_SETTINGS)
                    } catch (e: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }

                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE ->
                    // Location settings are not satisfied. However, we have no way
                    // to fix the settings so we won't show the dialog.
                    Log.i("settings", "no tal")
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            validPermissions()
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleClient, mLocationRequest, this)
    }

    protected fun stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleClient, this)
    }

    private fun validPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED -> requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
                checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED -> requestPermissions(arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION)
                else -> createLocationRequest()
            }
        } else {
            createLocationRequest()
        }
    }

    private fun send() {
        val name = findViewById<EditText>(R.id.name_et)
        val type = findViewById<Spinner>(R.id.type_spinner)
        val place = findViewById<Spinner>(R.id.place_spinner)
        val client = findViewById<Spinner>(R.id.client_spinner)
        val description = findViewById<EditText>(R.id.description_et)

        Log.i("Location", mLocation!!.toString() + "")

        stopLocationUpdates()

        val serviceUrl = getString(R.string.report_url)
        val url = getUrl(serviceUrl)
        val request = object : StringRequest(Request.Method.POST, url,
                Response.Listener { response ->
                    val data = Intent()
                    data.putExtra("response", response)
                    data.putExtra("status", 200)
                    setResult(Activity.RESULT_OK, data)
                    finish()
                },
                Response.ErrorListener { error ->
                    val err = String(error.networkResponse.data)
                    for (r in err.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                        Log.e("solucion", r)
                    }
                    val data = Intent()
                    data.putExtra("response", err)
                    data.putExtra("status", error.networkResponse.statusCode)
                    setResult(Activity.RESULT_OK, data)
                    finish()
                }) {
            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                var type_id = 0
                var client_id = 0
                var place_id = 0
                try {
                    if (type.selectedItemPosition > 0)
                        type_id = mTypes.getJSONObject(type.selectedItemPosition - 1).getInt("id")
                    if (client.selectedItemPosition > 0)
                        client_id = mClients.getJSONObject(client.selectedItemPosition - 1).getInt("id")
                    if (place.selectedItemPosition > 0)
                        place_id = mPlaces.getJSONObject(place.selectedItemPosition - 1).getInt("id")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                val body = JSONObject()
                try {
                    body.put("nombre", name.text.toString())
                            .put("descripcion", description.text.toString())
                            .put("fotoreporte_set-TOTAL_FORMS", "0")
                            .put("fotoreporte_set-INITIAL_FORMS", "0")
                            .put("fotoreporte_set-MIN_NUM_FORMS", "0")
                            .put("fotoreporte_set-MAX_NUM_FORMS", "5")

                    if (type_id != 0) {
                        body.put("tipo", type_id.toString() + "")
                    }
                    if (client_id != 0) {
                        body.put("cliente", client_id.toString() + "")
                    }
                    if (place_id != 0) {
                        body.put("lugar", place_id.toString() + "")
                    }
                    if (mLocation != null) {
                        body.put("latitud", mLocation!!.latitude.toString() + "")
                                .put("longitud", mLocation!!.longitude.toString() + "")
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                return body.toString().toByteArray()
            }
        }
        VolleySingleton.getInstance(this).addToRequestQueue(request)
        showLoading()
    }

    @Throws(FileNotFoundException::class)
    private fun sendWithFiles() {
        val name = findViewById<EditText>(R.id.name_et)
        val type = findViewById<Spinner>(R.id.type_spinner)
        val place = findViewById<Spinner>(R.id.place_spinner)
        val client = findViewById<Spinner>(R.id.client_spinner)
        val description = findViewById<EditText>(R.id.description_et)

        var type_id = 0
        var client_id = 0
        var place_id = 0
        try {
            type_id = mTypes.getJSONObject(type.selectedItemPosition - 1).getInt("id")
            client_id = mClients.getJSONObject(client.selectedItemPosition - 1).getInt("id")
            place_id = mPlaces.getJSONObject(place.selectedItemPosition - 1).getInt("id")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        stopLocationUpdates()
        showLoading()
        val notificationConfig = UploadNotificationConfig()
                .setTitle("Subiendo reporte")
                .setInProgressMessage("Subiendo reporte a [[UPLOAD_RATE]] ([[PROGRESS]])")
                .setErrorMessage("Hubo un error al subir el reporte")
                .setCompletedMessage("Subida completada exitosamente en [[ELAPSED_TIME]]")
                .setAutoClearOnSuccess(true)


        val serviceUrl = getString(R.string.report_url)
        val url = getUrl(serviceUrl)
        val upload = MultipartUploadRequest(baseContext, url)
                .setNotificationConfig(notificationConfig)
                .setAutoDeleteFilesAfterSuccessfulUpload(false)
                .setMaxRetries(1)
                .addParameter("nombre", name.text.toString())
                .addParameter("descripcion", description.text.toString())
                .addParameter("fotoreporte_set-TOTAL_FORMS", attaches.size.toString())
                .addParameter("fotoreporte_set-INITIAL_FORMS", "0")
                .addParameter("fotoreporte_set-MIN_NUM_FORMS", "0")
                .addParameter("fotoreporte_set-MAX_NUM_FORMS", "5")
        if (type_id != 0) {
            upload.addParameter("tipo", type_id.toString() + "")
        }
        if (client_id != 0) {
            upload.addParameter("cliente", client_id.toString() + "")
        }
        if (place_id != 0) {
            upload.addParameter("lugar", place_id.toString() + "")
        }
        if (mLocation != null) {
            upload.addParameter("latitud", mLocation!!.latitude.toString() + "")
                    .addParameter("longitud", mLocation!!.longitude.toString() + "")
        }

        for (i in attaches.indices) {
            val image = attaches[i]
            upload.addFileToUpload(image, "fotoreporte_set-$i-foto")
        }
        try {
            upload.setDelegate(object : UploadStatusDelegate {
                override fun onProgress(uploadInfo: UploadInfo) {

                }

                override fun onError(uploadInfo: UploadInfo, exception: Exception) {
                    Log.e("tales5", "error:", exception)
                    val data = Intent()
                    data.putExtra("response", "")
                    data.putExtra("status", 0)
                    setResult(Activity.RESULT_OK, data)
                    finish()
                }

                override fun onCompleted(uploadInfo: UploadInfo, serverResponse: ServerResponse) {
                    Log.e("tales5", "response: " + String(serverResponse.body))
                    Log.e("tales5", "status: " + serverResponse.httpCode)
                    val data = Intent()
                    data.putExtra("response", String(serverResponse.body))
                    data.putExtra("status", serverResponse.httpCode)
                    setResult(Activity.RESULT_OK, data)
                    finish()
                }

                override fun onCancelled(uploadInfo: UploadInfo) {}
            })
                    .startUpload()
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }

    }

    private fun hideLoading() {
        findViewById<View>(R.id.loading).visibility = View.GONE
    }

    private fun showLoading() {
        findViewById<View>(R.id.loading).visibility = View.VISIBLE
    }

    override fun onConnected(bundle: Bundle?) {
        createLocationRequest()
    }

    override fun onConnectionSuspended(i: Int) {}

    override fun onLocationChanged(location: Location) {
        mLocation = location
    }

    companion object {
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private const val PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 3
        private const val REQUEST_LOCATION_SETTINGS = 12
        private const val MAX_UPLOAD_FILES = 5
    }
}
