package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Criteria
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    private val TAG = SelectLocationFragment::class.java.simpleName
    private val REQUEST_LOCATION_PERMISSION = 1

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map: GoogleMap

    private var marker: Marker? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnSaveMarker.setOnClickListener { onLocationSelected() }

        return binding.root
    }

    private fun onLocationSelected() {
        if (_viewModel.longitude.value != null
                && _viewModel.latitude.value != null
                && _viewModel.reminderSelectedLocationStr.value != null) { // If a position is selected navigate back
            _viewModel.navigationCommand.value = NavigationCommand.Back
        } else {
            Toast.makeText(requireContext(), getString(R.string.select_location), Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        enableMyLocation()
        setMapClick(map)
        setPoiClick(map)
        setMapStyle(map)
    }

    // Called when user makes a long press gesture on the map.
    private fun setMapClick(map: GoogleMap) {
        map.setOnMapClickListener { latLng ->
            // A Snippet is Additional text that's displayed below the title.
            val title = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            if (marker != null) {
                marker!!.position = latLng
                marker!!.title = title
            } else {
                marker = map.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(title)
                )
            }
            _viewModel.selectedPOI.value = null
            _viewModel.latitude.value = latLng.latitude
            _viewModel.longitude.value = latLng.longitude
            _viewModel.reminderSelectedLocationStr.value = title
        }
    }

    // Places a marker on the map and displays an info window that contains POI name.
    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->

            if (marker != null) {
                marker!!.position = poi.latLng
                marker!!.title = poi.name
            } else {
                marker = map.addMarker(
                    MarkerOptions()
                        .position(poi.latLng)
                        .title(poi.name)
                )
            }
            marker?.showInfoWindow()

            _viewModel.selectedPOI.value = poi
            _viewModel.latitude.value = poi.latLng.latitude
            _viewModel.longitude.value = poi.latLng.longitude
            _viewModel.reminderSelectedLocationStr.value = poi.name
        }
    }

    private fun enableMyLocation() {

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION
                //, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ),
                REQUEST_LOCATION_PERMISSION)

        } else {
            map.isMyLocationEnabled = true
            zoomToUsersLocation()
        }
    }

    // Method is only called after permission check
    // Got code for zooming to users position from https://stackoverflow.com/questions/37904746/how-to-move-camera-to-user-current-location-as-map-loads-in-google-maps-v2
    @SuppressLint("MissingPermission")
    private fun zoomToUsersLocation() {
        val manager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val bestProvider = java.lang.String.valueOf(manager.getBestProvider(Criteria(), true))

        val location = manager.getLastKnownLocation(bestProvider)
        if (location != null) {
            Log.e("TAG", "GPS is on")
            val currentLatitude: Double = location.latitude
            val currentLongitude: Double = location.longitude
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        currentLatitude,
                        currentLongitude
                    ), 15f
                )
            )
            map.animateCamera(CameraUpdateFactory.zoomTo(10f), 2000, null)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }

        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }



}
