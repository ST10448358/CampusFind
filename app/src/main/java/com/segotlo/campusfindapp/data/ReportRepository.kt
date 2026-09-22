package com.segotlo.campusfindapp.data

import kotlinx.coroutines.flow.StateFlow

interface ReportRepository {
    val allReports: StateFlow<List<Report>>
    
    suspend fun addReport(report: Report): Result<Unit>
    
    suspend fun updateReportStatus(reportId: String, status: ReportStatus, customLabel: String? = null): Result<Unit>
    
    suspend fun linkReports(reportId1: String, reportId2: String): Result<Unit>
    suspend fun unlinkReports(reportId1: String, reportId2: String): Result<Unit>
    
    fun getReportsByUser(userId: String): StateFlow<List<Report>>
}
