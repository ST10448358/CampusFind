package com.segotlo.campusfindapp.data




object ReportSearchFilter {

    fun filter(
        reports: List<Report>,
        query: String,
        type: ReportType? = null,
        category: ItemCategory? = null,
        location: CampusLocation? = null
    ): List<Report> {

        val searchQuery = query.trim()

        return reports.filter { report ->
            (type == null || report.type == type) &&
                    (category == null || report.category == category) &&
                    (location == null || report.location == location) &&
                    (
                            searchQuery.isEmpty() ||
                                    report.title.contains(searchQuery, ignoreCase = true) ||
                                    report.description.contains(searchQuery, ignoreCase = true) ||
                                    report.institution.contains(searchQuery, ignoreCase = true)
                            )
        }
    }
}
