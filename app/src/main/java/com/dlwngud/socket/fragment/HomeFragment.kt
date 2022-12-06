package com.dlwngud.socket.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dlwngud.socket.MainActivity
import com.dlwngud.socket.databinding.FragmentHomeBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import noman.googleplaces.*
import java.io.IOException
import java.lang.IllegalArgumentException
import java.util.*

private lateinit var binding: FragmentHomeBinding

lateinit var mainActivity: MainActivity
lateinit var mContext: Context

private const val REQUEST_PERMISSION_LOCATION = 10

class HomeFragment : Fragment(), OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback,
    PlacesListener {

    private lateinit var mapView: MapView
    private lateinit var map: GoogleMap
    private var currentMarker: Marker? = null

    private val TAG = "googlemap_example"
    private val GPS_ENABLE_REQUEST_CODE = 2001
    private val UPDATE_INTERVAL_MS = 1000 // 1초

    private val FASTEST_UPDATE_INTERVAL_MS = 500 // 0.5초

    // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
    private val PERMISSIONS_REQUEST_CODE = 100
    var needRequest = false

    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    var REQUIRED_PERMISSIONS =
        arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION) // 외부 저장소

    var mCurrentLocatiion: Location? = null
    var currentPosition: LatLng? = null

    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private var location: Location? = null

    var previous_marker: MutableList<Marker>? = null

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        locationRequest = LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL_MS.toLong())
            .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS.toLong())

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest!!)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mainActivity)


        binding.btnNowLocation.setOnClickListener {
//            if (checkPermissionForLocation(mainActivity)) {
//
//            }
            if (binding.btnNowLocation.isSelected) {
                binding.btnNowLocation.isSelected = false
                map.isMyLocationEnabled = false
            } else {
                binding.btnNowLocation.isSelected = true
                startTracking()
                map.isMyLocationEnabled = true
                Toast.makeText(mainActivity, "현재 위치를 표시합니다.", Toast.LENGTH_SHORT).show()
                val markerTitle: String = getCurrentAddress(currentPosition!!).toString()
                setCurrentLocation(location!!, "현재 위치", markerTitle)
                mCurrentLocatiion = location
            }
        }

        binding.btnFindParking.setOnClickListener {
            if(!binding.btnNowLocation.isSelected){
                Toast.makeText(mainActivity, "현재 위치를 활성화시켜주세요.", Toast.LENGTH_SHORT).show()
            }else{
                if (binding.btnFindParking.isSelected) {
                    binding.btnFindParking.isSelected = false
                    map.clear() //지도 클리어
                    if (previous_marker != null) previous_marker?.clear() //지역정보 마커 클리어
                } else {
                    binding.btnFindParking.isSelected = true
                    showPlaceInformation(currentPosition!!)
                }
            }
        }

        return binding.root
    }

    // 위치 권한이 있는지 확인
    private fun checkPermissionForLocation(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                // 권한이 없으므로 권한 요청 알림 보내기
                ActivityCompat.requestPermissions(mainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSION_LOCATION)
                false
            }
        } else {
            true
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setDefaultLocation()

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.


        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(mainActivity,
            Manifest.permission.ACCESS_FINE_LOCATION)
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(mainActivity,
            Manifest.permission.ACCESS_COARSE_LOCATION)



        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
            hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED
        ) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
            startLocationUpdates() // 3. 위치 업데이트 시작
        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(mainActivity,
                    REQUIRED_PERMISSIONS[0])
            ) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Snackbar.make(binding.layoutHome, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction("확인") { // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                        ActivityCompat.requestPermissions(mainActivity, REQUIRED_PERMISSIONS,
                            PERMISSIONS_REQUEST_CODE)
                    }.show()
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(mainActivity, REQUIRED_PERMISSIONS,
                    PERMISSIONS_REQUEST_CODE)
            }
        }

        map.uiSettings.isMyLocationButtonEnabled = false
//        map.getUiSettings().setMyLocationButtonEnabled(true)
//        map.setOnMapClickListener(OnMapClickListener { Log.d(TAG, "onMapClick :") })
    }

    private fun setDefaultLocation() {
        //디폴트 위치, Seoul
        val DEFAULT_LOCATION = LatLng(37.56, 126.97)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15f))
    }

    var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val locationList = locationResult.locations
            if (locationList.size > 0) {
                location = locationList[locationList.size - 1]
                //location = locationList.get(0);
                currentPosition = LatLng(location!!.getLatitude(), location!!.getLongitude())
                val markerTitle: String = getCurrentAddress(currentPosition!!).toString()
                val markerSnippet =
                    "위도:" + location!!.getLatitude().toString() + " 경도:" + location!!.getLongitude()
                        .toString()
                Log.d(TAG, "onLocationResult : $markerSnippet")
            }
            if(binding.btnNowLocation.isSelected){
                startTracking()
            }
//            if (currentMarker != null) currentMarker!!.remove()
//            val currentLatLng = LatLng(location!!.latitude, location!!.longitude)
//            val cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng )
//            map.moveCamera(cameraUpdate)
        }
    }

    fun startTracking(){
        if (currentMarker != null) currentMarker!!.remove()
        val currentLatLng = LatLng(location!!.latitude, location!!.longitude)
        val cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng)
        map.moveCamera(cameraUpdate)
    }

    private fun startLocationUpdates() {
        if (!checkLocationServicesStatus()) {
            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting")
            showDialogForLocationServiceSetting()
        } else {
            val hasFineLocationPermission = ContextCompat.checkSelfPermission(mainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION)
            val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(mainActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION)
            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음")
                return
            }
            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates")
            mFusedLocationClient!!.requestLocationUpdates(locationRequest!!,
                locationCallback,
                Looper.myLooper())
//            if (checkPermission()) map.setMyLocationEnabled(true)
        }
    }

    fun getCurrentAddress(latlng: LatLng): String? {

        //지오코더... GPS를 주소로 변환
        val geocoder = Geocoder(mainActivity, Locale.getDefault())
        val addresses: List<Address>?
        addresses = try {
            geocoder.getFromLocation(
                latlng.latitude,
                latlng.longitude,
                1)
        } catch (ioException: IOException) {
            //네트워크 문제
            Toast.makeText(mainActivity, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show()
            return "지오코더 서비스 사용불가"
        } catch (illegalArgumentException: IllegalArgumentException) {
            Toast.makeText(mainActivity, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show()
            return "잘못된 GPS 좌표"
        }
        return if (addresses == null || addresses.size == 0) {
            Toast.makeText(mainActivity, "주소 미발견", Toast.LENGTH_LONG).show()
            "주소 미발견"
        } else {
            val address: Address = addresses[0]
            address.getAddressLine(0).toString()
        }
    }


    fun checkLocationServicesStatus(): Boolean {
        val locationManager = mainActivity.getSystemService(LOCATION_SERVICE) as LocationManager
        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }


    fun setCurrentLocation(location: Location, markerTitle: String?, markerSnippet: String?) {
        if (currentMarker != null) currentMarker!!.remove()
        val currentLatLng = LatLng(location.latitude, location.longitude)
//        val markerOptions = MarkerOptions()
//        markerOptions.position(currentLatLng)
//        markerOptions.title(markerTitle)
//        markerOptions.snippet(markerSnippet)
//        markerOptions.draggable(true)
//        currentMarker = map.addMarker(markerOptions)
        val cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng)
        map.moveCamera(cameraUpdate)
    }

    /*
    * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
    */
    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        permsRequestCode: Int,
        permissions: Array<String?>,
        grandResults: IntArray,
    ) {
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.size == REQUIRED_PERMISSIONS.size) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            var check_result = true


            // 모든 퍼미션을 허용했는지 체크합니다.
            for (result in grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false
                    break
                }
            }
            if (check_result) {

                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                startLocationUpdates()
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(mainActivity,
                        REQUIRED_PERMISSIONS[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(mainActivity,
                        REQUIRED_PERMISSIONS[1])
                ) {


                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(binding.layoutHome, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인"
                    ) {  }.show()
                } else {


                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(binding.layoutHome, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인"
                    ) {  }.show()
                }
            }
        }
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private fun showDialogForLocationServiceSetting() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(mainActivity)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage("""
    앱을 사용하기 위해서는 위치 서비스가 필요합니다.
    위치 설정을 수정하실래요?
    """.trimIndent())
        builder.setCancelable(true)
        builder.setPositiveButton("설정", DialogInterface.OnClickListener { dialog, id ->
            val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE)
        })
        builder.setNegativeButton("취소",
            DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
        builder.create().show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GPS_ENABLE_REQUEST_CODE ->
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d(TAG, "onActivityResult : GPS 활성화 되있음")
                        needRequest = true
                        return
                    }
                }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // 2. Context를 액티비티로 형변환해서 할당
        mainActivity = context as MainActivity
        mContext = context
    }

    override fun onPlacesFailure(e: PlacesException?) {

    }

    override fun onPlacesStart() {

    }

    override fun onPlacesSuccess(places: MutableList<Place>?) {
        (mContext as Activity).runOnUiThread {
            for (place in places!!) {
                val latLng = LatLng(place.latitude, place.longitude)
                val markerSnippet = getCurrentAddress(latLng)
                val markerOptions = MarkerOptions()
                markerOptions.position(latLng)
                markerOptions.title(place.name)
                markerOptions.snippet(markerSnippet)
                val item: Marker? = map.addMarker(markerOptions)
                previous_marker?.add(item!!)
            }

            //중복 마커 제거
            val hashSet = HashSet<Marker>()
            previous_marker?.let { hashSet.addAll(it) }
            previous_marker?.clear()
            previous_marker?.plus(hashSet)
        }
    }

    override fun onPlacesFinished() {

    }

    fun showPlaceInformation(location: LatLng) {
        map.clear() //지도 클리어
        if (previous_marker != null) previous_marker?.clear() //지역정보 마커 클리어
        NRPlaces.Builder()
            .listener(this)
            .key("AIzaSyAhjxdHs3ZTu8-uw4fgryDRNdSTpylrMZ4")
            .latlng(location.latitude, location.longitude) //현재 위치
            .radius(1000) //1000 미터 내에서 검색
            .type(PlaceType.PARKING) // 주차장
            .build()
            .execute()
        Toast.makeText(mainActivity, "1km내 주차장을 탐색합니다.", Toast.LENGTH_SHORT).show()
    }
}
