package com.segotlo.campusfindapp.ui.xml

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
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
import java.util.Date

class ReportItemFragment : Fragment() {

    private var _binding: FragmentReportItemBinding? = null
    private val binding get() = _binding!!

    private var reportType = ReportType.LOST
    private var selectedLatLng = LatLng(-25.7545, 28.2314)
    private var uploadedPhotoUri: String? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            uploadedPhotoUri = it.toString()
            binding.imgUploadPreview.setImageURI(it)
            binding.imgUploadPreview.visibility = View.VISIBLE
            binding.llUploadPrompt.visibility = View.GONE
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

        val locAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown_white_text, CampusLocation.values().map { it.name })
        binding.actvCampusLocation.setAdapter(locAdapter)

        binding.cardUploadPhoto.setOnClickListener {
            getContent.launch("image/*")
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
                try {
                    googleMap.uiSettings.isZoomControlsEnabled = true
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 15f))
                    googleMap.addMarker(MarkerOptions().position(selectedLatLng).title("Report Location"))

                    googleMap.setOnMapClickListener { latLng ->
                        selectedLatLng = latLng
                        googleMap.clear()
                        googleMap.addMarker(MarkerOptions().position(latLng).title("Selected Pin"))
                        binding.tvMapCoords.text = String.format("%.4f, %.4f", latLng.latitude, latLng.longitude)
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