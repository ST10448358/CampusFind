package com.segotlo.campusfindapp.ui.xml

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.segotlo.campusfindapp.R
import com.segotlo.campusfindapp.data.CampusLocation
import com.segotlo.campusfindapp.data.ItemCategory
import com.segotlo.campusfindapp.data.MockRepository
import com.segotlo.campusfindapp.data.Report
import com.segotlo.campusfindapp.data.ReportType
import com.segotlo.campusfindapp.data.SouthAfricanInstitutions
import com.segotlo.campusfindapp.data.getCoordinates
import com.segotlo.campusfindapp.databinding.FragmentReportItemBinding
import com.segotlo.campusfindapp.util.UriUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale

class ReportItemFragment : Fragment() {

    private var _binding: FragmentReportItemBinding? = null
    private val binding get() = _binding!!

    private var reportType = ReportType.LOST
    private var selectedLatLng = LatLng(-25.7545, 28.2314)
    private var uploadedPhotoUri: String? = null
    private var googleMapInstance: com.google.android.gms.maps.GoogleMap? = null
    private var isAutoFilledLocation = true

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                val safeUri = UriUtils.copyUriToInternalFile(requireContext(), it, "report_photo")
                if (safeUri != null) {
                    uploadedPhotoUri = safeUri.toString()
                    binding.imgUploadPreview.setImageURI(safeUri)
                    binding.imgUploadPreview.visibility = View.VISIBLE
                    binding.llUploadPrompt.visibility = View.GONE
                } else {
                    uploadedPhotoUri = null
                    Toast.makeText(requireContext(), "Failed to process photo", Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                uploadedPhotoUri = null
                Toast.makeText(requireContext(), "Failed to process photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val typeArg = arguments?.getString("report_type")
        if (typeArg == "FOUND") {
            reportType = ReportType.FOUND
            binding.tvReportScreenTitle.text = "Report Found Item"
        } else {
            reportType = ReportType.LOST
            binding.tvReportScreenTitle.text = "Report Lost Item"
        }

        binding.btnCloseReport.setOnClickListener {
            findNavController().popBackStack()
        }

        val catAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown_white_text, ItemCategory.values().map { it.name })
        binding.actvCategory.setAdapter(catAdapter)

        val instAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown_white_text, SouthAfricanInstitutions.list)
        binding.actvReportInstitution.setAdapter(instAdapter)
        binding.actvReportInstitution.setOnItemClickListener { parent, _, position, _ ->
            val selectedInst = parent.getItemAtPosition(position).toString()
            val coords = SouthAfricanInstitutions.getCoordinatesForInstitution(selectedInst)
            updateMapMarkerAndCamera(coords)
        }

        val locAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown_white_text, CampusLocation.values().map { it.name })
        binding.actvCampusLocation.setAdapter(locAdapter)

        binding.etLocationDetail.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.etLocationDetail.hasFocus()) {
                    isAutoFilledLocation = false
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        binding.cardUploadPhoto.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.btnEnlargeMap.setOnClickListener {
            showFullscreenMapDialog()
        }

        setupMap()

        binding.btnSubmitReport.setOnClickListener {
            submitReport()
        }
    }

    private fun setupMap() {
        try {
            val availability = try {
                com.google.android.gms.common.GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(requireContext())
            } catch (_: SecurityException) {
                com.google.android.gms.common.ConnectionResult.SERVICE_INVALID
            } catch (_: Throwable) {
                com.google.android.gms.common.ConnectionResult.SERVICE_INVALID
            }

            if (availability != com.google.android.gms.common.ConnectionResult.SUCCESS) {
                return
            }

            val mapFragment = childFragmentManager.findFragmentById(R.id.map_report_item) as? SupportMapFragment
            mapFragment?.getMapAsync { googleMap ->
                googleMapInstance = googleMap
                try {
                    googleMap.uiSettings.isZoomControlsEnabled = true
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 16f))

                    googleMap.setOnCameraIdleListener {
                        val center = googleMap.cameraPosition.target
                        onLocationPinMoved(center)
                    }

                    googleMap.setOnMapClickListener { latLng ->
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, googleMap.cameraPosition.zoom))
                    }
                } catch (_: SecurityException) {
                    // Suppress SecurityException from GoogleApiManager
                } catch (_: Throwable) {
                    // Suppress GMS / SecurityException
                }
            }
        } catch (_: SecurityException) {
            // Suppress SecurityException from GoogleApiManager
        } catch (_: Throwable) {
            // Suppress GMS / SecurityException
        }
    }

    private fun updateMapMarkerAndCamera(latLng: LatLng) {
        onLocationPinMoved(latLng)
        val map = googleMapInstance ?: return
        try {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
        } catch (_: Throwable) {}
    }

    private fun onLocationPinMoved(latLng: LatLng) {
        selectedLatLng = latLng
        binding.tvMapCoords.text = String.format(Locale.getDefault(), "%.4f, %.4f", latLng.latitude, latLng.longitude)

        // Calculate proximity to known CampusLocation landmarks
        val (closestLocation, landmarkName) = detectCampusLandmark(latLng)

        // Auto-select dropdown item
        binding.actvCampusLocation.setText(closestLocation.name, false)

        // Auto-fill location detail field if not manually overridden
        if (binding.etLocationDetail.text.isNullOrEmpty() || isAutoFilledLocation) {
            isAutoFilledLocation = true
            binding.etLocationDetail.setText(landmarkName)
        }

        // Asynchronously query Geocoder
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (android.location.Geocoder.isPresent()) {
                    val geocoder = android.location.Geocoder(requireContext(), Locale.getDefault())
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                    val address = addresses?.firstOrNull()
                    if (address != null) {
                        val featureName = address.featureName ?: address.thoroughfare ?: address.subThoroughfare
                        if (!featureName.isNullOrEmpty() && !featureName.matches(Regex("^[0-9.-]+$"))) {
                            val refinedDetail = "$landmarkName ($featureName)"
                            withContext(Dispatchers.Main) {
                                if (isAutoFilledLocation) {
                                    binding.etLocationDetail.setText(refinedDetail)
                                }
                            }
                        }
                    }
                }
            } catch (_: Throwable) {}
        }
    }

    private fun detectCampusLandmark(latLng: LatLng): Pair<CampusLocation, String> {
        val landmarks = listOf(
            Triple(CampusLocation.MAIN_LIBRARY, Pair(-25.7551, 28.2302), "Main Campus Library & Study Commons"),
            Triple(CampusLocation.SCIENCE_BUILDING, Pair(-25.7529, 28.2335), "Science & Research Complex"),
            Triple(CampusLocation.STUDENT_UNION, Pair(-25.7538, 28.2321), "Student Union & Services Hub"),
            Triple(CampusLocation.ENGINEERING_COMPLEX, Pair(-25.7548, 28.2285), "Engineering & Technology Building"),
            Triple(CampusLocation.SPORTS_CENTRE, Pair(-25.7512, 28.2370), "Campus Sports Complex & Gymnasium"),
            Triple(CampusLocation.CAMPUS_SECURITY_OFFICE, Pair(-25.7562, 28.2290), "Campus Security Office & Main Gate"),
            Triple(CampusLocation.DINING_HALL, Pair(-25.7560, 28.2340), "Main Campus Dining Hall & Cafeteria"),
            Triple(CampusLocation.RESIDENTIAL_DORMS, Pair(-25.7575, 28.2355), "Student Residence / Dormitory Block")
        )

        var closest = CampusLocation.OTHER
        var closestName = "Campus Main Grounds"
        var minDistance = Double.MAX_VALUE

        for ((location, coords, name) in landmarks) {
            val dist = calculateDistance(latLng.latitude, latLng.longitude, coords.first, coords.second)
            if (dist < minDistance) {
                minDistance = dist
                closest = location
                closestName = name
            }
        }

        return if (minDistance < 800.0) { // within 800m
            Pair(closest, closestName)
        } else {
            Pair(CampusLocation.OTHER, "Campus Main Grounds")
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val latDiff = Math.toRadians(lat2 - lat1)
        val lonDiff = Math.toRadians(lon2 - lon1)
        val a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(lonDiff / 2) * Math.sin(lonDiff / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return 6371000.0 * c // meters
    }

    private fun showFullscreenMapDialog() {
        val dialogBinding = com.segotlo.campusfindapp.databinding.DialogFullscreenMapBinding.inflate(layoutInflater)
        val dialog = android.app.Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(dialogBinding.root)

        var tempSelectedLatLng = selectedLatLng
        dialogBinding.tvFullscreenCoords.text = String.format("%.4f, %.4f", tempSelectedLatLng.latitude, tempSelectedLatLng.longitude)

        dialogBinding.btnCloseFullscreenMap.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnConfirmFullscreenLocation.setOnClickListener {
            updateMapMarkerAndCamera(tempSelectedLatLng)
            dialog.dismiss()
        }

        dialog.show()

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fullscreen) as? SupportMapFragment
        mapFragment?.getMapAsync { fullMap ->
            try {
                fullMap.uiSettings.isZoomControlsEnabled = true
                fullMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tempSelectedLatLng, 17f))

                fullMap.setOnCameraIdleListener {
                    val center = fullMap.cameraPosition.target
                    tempSelectedLatLng = center
                    dialogBinding.tvFullscreenCoords.text = String.format("%.4f, %.4f", center.latitude, center.longitude)
                }

                fullMap.setOnMapClickListener { latLng ->
                    fullMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, fullMap.cameraPosition.zoom))
                }
            } catch (_: Throwable) {}
        }
    }

    private fun submitReport() {
        val title = binding.etItemTitle.text.toString().trim()
        val catText = binding.actvCategory.text.toString().trim()
        val instText = binding.actvReportInstitution.text.toString().trim()
        val locText = binding.actvCampusLocation.text.toString().trim()
        val specLoc = binding.etLocationDetail.text.toString().trim()
        val desc = binding.etDescription.text.toString().trim()
        val secretQ = binding.etSecretQuestion.text.toString().trim()

        if (title.isEmpty() || catText.isEmpty() || locText.isEmpty() || desc.isEmpty()) {
            Toast.makeText(requireContext(), "Please complete all required (*) fields", Toast.LENGTH_SHORT).show()
            return
        }

        val user = MockRepository.currentUser.value
        val category = ItemCategory.values().find { it.name == catText } ?: ItemCategory.OTHER
        val location = CampusLocation.values().find { it.name == locText } ?: CampusLocation.OTHER

        val report = Report(
            userId = user?.id ?: "guest",
            userDisplayName = if (user != null) "${user.firstName} ${user.lastName}" else "Anonymous Student",
            userEmail = user?.email ?: "student@campus.ac.za",
            type = reportType,
            title = title,
            description = desc,
            category = category,
            location = location,
            specificLocationDetail = specLoc,
            latitude = selectedLatLng.latitude,
            longitude = selectedLatLng.longitude,
            institution = if (instText.isNotEmpty()) instText else "University of Pretoria (UP)",
            dateOccurred = Date(),
            imageUrl = uploadedPhotoUri,
            secretIdentifierQuestion = if (secretQ.isNotEmpty()) secretQ else null
        )

        MockRepository.addReport(report)
        Toast.makeText(requireContext(), "Report submitted successfully!", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}