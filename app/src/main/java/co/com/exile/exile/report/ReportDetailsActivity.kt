package co.com.exile.exile.report

import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import co.com.exile.exile.BaseActivity
import co.com.exile.exile.R
import co.com.exile.exile.network.VolleySingleton
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.picasso.Picasso
import com.stfalcon.frescoimageviewer.ImageViewer
import kotlinx.android.synthetic.main.content_report_details.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

class ReportDetailsActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var fotoAdapter: AttachAdapter
    private lateinit var mMap: GoogleMap
    private var report: JSONObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_details)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        loadReport()
    }

    private fun loadReport() {
        if (intent.hasExtra("report")) {
            try {
                val report = JSONObject(intent.getStringExtra("report"))
                this.report = report
                Log.i("report", report.toString())

                val reportTitle = findViewById<TextView>(R.id.report_title)
                reportTitle.text = report.getString("nombre")

                val reportDescription = findViewById<TextView>(R.id.report_description)
                reportDescription.text = report.getString("descripcion")

                val creator = report.getJSONObject("creator")

                val creatorFullName = findViewById<TextView>(R.id.creator_full_name)
                creatorFullName.text = "${creator.getString("first_name")} ${creator.getString("last_name")}"

                val creatorEmail = findViewById<TextView>(R.id.creator_email)
                creatorEmail.text = creator.getString("email")

                val dateString = report.getString("fecha")
                val parser = SimpleDateFormat("MM/dd/yyyy K:mm a", Locale.getDefault())
                val dateParsed = parser.parse(dateString)
                val formatter = SimpleDateFormat("dd MMM. yyyy", Locale.getDefault())
                val dateFormmated = formatter.format(dateParsed)

                val date = findViewById<TextView>(R.id.date)
                date.text = dateFormmated

                val creatorAvatar = findViewById<ImageView>(R.id.creator_avatar)
                val avatarURl = getUrl(creator.getString("avatar"))
                Picasso.with(this)
                        .load(avatarURl)
                        .into(creatorAvatar)

                if (report.has("latitud") && report.has("longitud")) {
                    val mapFragment = supportFragmentManager
                            .findFragmentById(R.id.map) as SupportMapFragment
                    mapFragment.getMapAsync(this)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            finish()
        }
    }

    private fun showFotos(position: Int) {
        ImageViewer.Builder(this, fotoAdapter.attaches)
                .setStartPosition(position)
                .show()
    }

    private fun loadPhotos(id: Int) {
        val serviceUrl = getString(R.string.report_fotos, id)

        val url = getUrl(serviceUrl)
        val request = JsonObjectRequest(Request.Method.GET, url, null, Response.Listener { response ->
            Log.i("response", response.toString())
            try {
                val fotos = response.getJSONArray("object_list")
                putInRV(fotos)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }, Response.ErrorListener { error -> Log.i("response", error.toString()) })
        VolleySingleton.getInstance(this).addToRequestQueue(request)
    }

    @Throws(JSONException::class)
    private fun putInRV(fotos: JSONArray) {
        val photoList = arrayOfNulls<String>(fotos.length())
        for (i in 0 until fotos.length()) {
            val foto = fotos.getJSONObject(i)
            photoList[i] = foto.getString("url")
        }
        fotoAdapter.attaches = photoList
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        report?.let { report ->
            val sydney = LatLng(report.getDouble("latitud"), report.getDouble("longitud"))
            mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15f))
        }
    }

}
