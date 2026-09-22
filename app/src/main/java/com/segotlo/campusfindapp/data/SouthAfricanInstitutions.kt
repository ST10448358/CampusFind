package com.segotlo.campusfindapp.data

object SouthAfricanInstitutions {
    val list: List<String> = listOf(
        "University of Pretoria (UP)",
        "University of Cape Town (UCT)",
        "University of the Witwatersrand (Wits)",
        "University of Johannesburg (UJ)",
        "Stellenbosch University (SU)",
        "University of KwaZulu-Natal (UKZN)",
        "Rhodes University (RU)",
        "University of South Africa (UNISA)",
        "Tshwane University of Technology (TUT)",
        "Cape Peninsula University of Technology (CPUT)",
        "Durban University of Technology (DUT)",
        "Central University of Technology (CUT)",
        "Vaal University of Technology (VUT)",
        "North-West University (NWU)",
        "University of the Free State (UFS)",
        "University of the Western Cape (UWC)",
        "Nelson Mandela University (NMU)",
        "University of Fort Hare (UFH)",
        "University of Limpopo (UL)",
        "University of Venda (Univen)",
        "Walter Sisulu University (WSU)",
        "Sefako Makgatho Health Sciences University (SMU)",
        "University of Mpumalanga (UMP)",
        "Sol Plaatje University (SPU)",
        "Mangosuthu University of Technology (MUT)",
        "Rosebank College",
        "Varsity College",
        "Boston City Campus",
        "Damelin College",
        "Richfield Graduate Institute of Technology",
        "Berea Technical College",
        "Tshwane South TVET College",
        "Tshwane North TVET College",
        "Ekurhuleni East TVET College",
        "Ekurhuleni West TVET College",
        "Coastal KZN TVET College",
        "False Bay TVET College",
        "Lovedale TVET College",
        "Majuba TVET College",
        "Motheo TVET College",
        "Orbit TVET College",
        "Vhembe TVET College",
        "Capricorn TVET College",
        "College of Cape Town",
        "Esayidi TVET College",
        "Flavius Mareka TVET College",
        "Goldfields TVET College",
        "Mopani TVET College",
        "Northlink College",
        "Sedibeng TVET College",
        "Umfolozi TVET College",
        "Waterberg TVET College"
    )

    fun search(query: String): List<String> {
        if (query.isBlank()) return list
        val cleanQuery = query.trim()
        return list.filter { institution ->
            institution.contains(cleanQuery, ignoreCase = true) ||
            institution.split(" ").any { word -> word.startsWith(cleanQuery, ignoreCase = true) }
        }
    }

    fun getCoordinatesForInstitution(name: String): com.google.android.gms.maps.model.LatLng {
        val clean = name.lowercase()
        return when {
            clean.contains("cput") || clean.contains("cape peninsula") -> com.google.android.gms.maps.model.LatLng(-33.9312, 18.4241)
            clean.contains("uct") || clean.contains("cape town") -> com.google.android.gms.maps.model.LatLng(-33.9576, 18.4608)
            clean.contains("wits") || clean.contains("witwatersrand") -> com.google.android.gms.maps.model.LatLng(-26.1929, 28.0305)
            clean.contains("uj") || clean.contains("johannesburg") -> com.google.android.gms.maps.model.LatLng(-26.1825, 28.0003)
            clean.contains("up") || clean.contains("pretoria") -> com.google.android.gms.maps.model.LatLng(-25.7545, 28.2315)
            clean.contains("tut") || clean.contains("tshwane university") -> com.google.android.gms.maps.model.LatLng(-25.7323, 28.1620)
            clean.contains("stellenbosch") || clean.contains("su") -> com.google.android.gms.maps.model.LatLng(-33.9322, 18.8644)
            clean.contains("nwu") || clean.contains("north-west") -> com.google.android.gms.maps.model.LatLng(-26.6888, 27.0945)
            clean.contains("ufs") || clean.contains("free state") -> com.google.android.gms.maps.model.LatLng(-29.1107, 26.1824)
            clean.contains("ukzn") || clean.contains("kwazulu") -> com.google.android.gms.maps.model.LatLng(-29.8671, 30.9808)
            clean.contains("nmu") || clean.contains("mandela") -> com.google.android.gms.maps.model.LatLng(-34.0022, 25.6702)
            clean.contains("dut") || clean.contains("durban university") -> com.google.android.gms.maps.model.LatLng(-29.8522, 31.0084)
            clean.contains("uwc") || clean.contains("western cape") -> com.google.android.gms.maps.model.LatLng(-33.9332, 18.6272)
            clean.contains("rhodes") -> com.google.android.gms.maps.model.LatLng(-33.3117, 26.5207)
            clean.contains("vut") || clean.contains("vaal") -> com.google.android.gms.maps.model.LatLng(-26.7118, 27.8631)
            clean.contains("limpopo") -> com.google.android.gms.maps.model.LatLng(-23.8872, 29.7397)
            clean.contains("mpumalanga") -> com.google.android.gms.maps.model.LatLng(-25.4371, 30.9818)
            clean.contains("sol plaatje") -> com.google.android.gms.maps.model.LatLng(-28.7428, 24.7644)
            else -> com.google.android.gms.maps.model.LatLng(-25.7545, 28.2315)
        }
    }
}
