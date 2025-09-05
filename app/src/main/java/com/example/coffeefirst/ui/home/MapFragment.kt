package com.example.coffeefirst.ui.home

import android.Manifest
import android.app.AlertDialog
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

import com.example.coffeefirst.databinding.FragmentMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: LatLng? = null
    @Inject
    lateinit var sharedPref: SharedPreferences



    private val coffeeShops = listOf(
        CoffeeShop("Surf Coffee x Science", LatLng(55.703502, 37.511101)),
        CoffeeShop("Surf Coffee x Erudit", LatLng(55.692140508992225, 37.53137049016307)),
        CoffeeShop("Surf Coffee x Ramenki", LatLng(55.70223418079328, 37.49337941399919))
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        binding.btnMyLocation.setOnClickListener {
            enableMyLocation()
        }

        binding.btnZoomIn.setOnClickListener {
            if (::googleMap.isInitialized) {
                val currentZoom = googleMap.cameraPosition.zoom
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(currentZoom + 1))
            }
        }
        binding.btnZoomOut.setOnClickListener {
            if (::googleMap.isInitialized) {
                val currentZoom = googleMap.cameraPosition.zoom
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(currentZoom - 1))
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isMyLocationButtonEnabled = false
        addCoffeeShopMarkers()

        googleMap.setOnMarkerClickListener { marker ->
            showCoffeeShopInfo(marker)
            true
        }

        enableMyLocation()
    }

    private fun addCoffeeShopMarkers() {
        coffeeShops.forEach { shop ->
            googleMap.addMarker(
                MarkerOptions()
                    .position(shop.location)
                    .title(shop.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
        }
    }

    private fun showCoffeeShopInfo(marker: Marker) {
        val coffeeShop = coffeeShops.find { it.location == marker.position }
        coffeeShop?.let {
            AlertDialog.Builder(requireContext())
                .setTitle(it.name)
                .setMessage("Выбрать эту кофейню?")
                .setPositiveButton("Выбрать") { _, _ ->
                    navigateBackWithSelectedLocation(it)
                }
                .setNegativeButton("Отмена", null)
                .show()
        }
    }

    private fun navigateBackWithSelectedLocation(shop: CoffeeShop) {
        sharedPref.edit {
            putString("last_selected_shop_name", shop.name)
            putString("last_selected_shop_lat", shop.location.latitude.toString())
            putString("last_selected_shop_lng", shop.location.longitude.toString())
        }
        findNavController().previousBackStackEntry?.savedStateHandle?.set(
            "selected_coffee_shop",
            shop
        )
        findNavController().popBackStack()
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            binding.progressBar.visibility = View.VISIBLE
            googleMap.isMyLocationEnabled = true
            getCurrentLocation()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }


    private fun getCurrentLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    binding.progressBar.visibility = View.GONE
                    currentLocation = LatLng(location.latitude, location.longitude)
                    currentLocation?.let { latLng ->
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                        addCurrentLocationMarker(latLng)
                    }
                } else {
                    requestSingleUpdate()
                }
            }.addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                showLocationError()
            }
        } catch (e: SecurityException) {
            Log.e("MapFragment", "Error getting location", e)
        }
    }

    private fun requestSingleUpdate() {
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    binding.progressBar.visibility = View.GONE
                    if (location != null) {
                        currentLocation = LatLng(location.latitude, location.longitude)
                        currentLocation?.let { latLng ->
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                            addCurrentLocationMarker(latLng)
                        }
                    } else {
                        showLocationError()
                    }
                }
                .addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    showLocationError()
                }
        } catch (e: SecurityException) {
            Log.e("MapFragment", "Error requesting single update", e)
        }
    }

    private fun addCurrentLocationMarker(location: LatLng) {
        googleMap.addMarker(
            MarkerOptions()
                .position(location)
                .title("Ваше местоположение")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )
    }

    private fun showLocationError() {
        Toast.makeText(
            requireContext(),
            "Не удалось определить местоположение",
            Toast.LENGTH_SHORT
        ).show()

        coffeeShops.firstOrNull()?.let {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it.location, 12f))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onDestroyView() {
        binding.mapView.onDestroy()
        _binding = null
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    data class CoffeeShop(
        val name: String,
        val location: LatLng
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readParcelable(LatLng::class.java.classLoader)!!
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
            parcel.writeParcelable(location, flags)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<CoffeeShop> {
            override fun createFromParcel(parcel: Parcel): CoffeeShop = CoffeeShop(parcel)
            override fun newArray(size: Int): Array<CoffeeShop?> = arrayOfNulls(size)
        }
    }


}
