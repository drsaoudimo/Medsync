package com.example.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppointmentEntity
import com.example.data.db.MedicationEntity
import com.example.data.db.UserEntity
import com.example.data.repository.MedicalRepository
import com.example.utils.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: MedicalRepository
) : ViewModel() {

    // Localization / Language Settings
    private val _currentLanguage = MutableStateFlow(Language.EN)
    val currentLanguage: StateFlow<Language> = _currentLanguage.asStateFlow()

    // Authentication & Role session
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    private val _selectedRole = MutableStateFlow("Patient")
    val selectedRole: StateFlow<String> = _selectedRole.asStateFlow()

    private val _adminPassword = MutableStateFlow("000000")
    val adminPassword: StateFlow<String> = _adminPassword.asStateFlow()

    private val _adminUsersList = MutableStateFlow(
        listOf(
            AdminUser("smohamed.stf@gmail.com", "Mohamed Soliman", "Admin", "+20 1021435213", "Lead Admin Manager", true),
            AdminUser("sarah.ahmed@medsync.org", "Dr. Sarah Ahmed", "Doctor", "+1 202-555-0143", "Cardiology Specialist", true),
            AdminUser("nour.reception@medsync.org", "Nour Al-Sabie", "Receptionist", "+20 1145628192", "Front Desk Lead", true),
            AdminUser("elena.pharmacist@medsync.org", "Elena Rostova", "Pharmacist", "+7 911-300-4422", "Operations Chemist", true),
            AdminUser("marcus.nurse@medsync.org", "Marcus Vance", "Nurse", "+1 312-555-0291", "Emergency Ward Staff", true),
            AdminUser("patient.ali@yahoo.com", "Ali Hassan", "Patient", "+20 1205562134", "Outpatient Cardiology", false),
            AdminUser("patient.youssef@gmail.com", "Youssef Adel", "Patient", "+20 1555621342", "Inpatient Ward B", false, "Suspended"),
            AdminUser("amr.pediatrics@gmail.com", "Dr. Amr Salem", "Doctor", "+20 1098453216", "Pediatric Surgeon Candidate", false)
        )
    )
    val adminUsersList: StateFlow<List<AdminUser>> = _adminUsersList.asStateFlow()

    // Expose repository flows safely
    val appointments = repository.appointments
    val medications = repository.medications
    val syncStatus = repository.syncStatus

    init {
        // Start logged out so user gets Splash and Login screen first
    }

    fun setLanguage(lang: Language) {
        _currentLanguage.value = lang
    }

    fun setRole(role: String) {
        _selectedRole.value = role
        _currentUser.value = _currentUser.value?.copy(role = role)
    }

    fun simulateAuthLogin(email: String, fullName: String, role: String) {
        _currentUser.value = UserEntity(
            email = email,
            fullName = fullName,
            role = role,
            phoneNumber = "+1 202-555-0143",
            specialty = "Chief Cardiologist",
            emergencyContact = "911",
            preferredLanguage = "EN"
        )
        _selectedRole.value = role
        _isUserLoggedIn.value = true
    }

    fun simulateLogout() {
        _currentUser.value = null
        _isUserLoggedIn.value = false
    }

    fun addAppointment(patientName: String, dateTimeStr: String, type: String, reason: String) {
        viewModelScope.launch {
            repository.addAppointment(
                AppointmentEntity(
                    patientName = patientName,
                    doctorName = _currentUser.value?.fullName ?: "Dr. Sarah Ahmed",
                    dateTimeStr = dateTimeStr,
                    type = type,
                    status = "Confirmed",
                    reason = reason
                )
            )
        }
    }

    fun deleteAppointment(appointment: AppointmentEntity) {
        viewModelScope.launch {
            repository.deleteAppointment(appointment)
        }
    }

    suspend fun addMedication(name: String, cat: String, stock: Int, price: Double, dosage: String, exp: String, barcode: String) {
        repository.addMedication(
            MedicationEntity(
                name = name,
                category = cat,
                stockQty = stock,
                price = price,
                dosage = dosage,
                expiryDate = exp,
                barcode = barcode
            )
        )
    }

    fun updateMedicationStock(id: Int, newQty: Int) {
        viewModelScope.launch {
            repository.updateStock(id, newQty)
        }
    }

    fun changeAdminPassword(newPass: String) {
        _adminPassword.value = newPass
    }

    fun suspendUser(email: String) {
        _adminUsersList.value = _adminUsersList.value.map {
            if (it.email == email) it.copy(status = if (it.status == "Suspended") "Active" else "Suspended") else it
        }
    }

    fun deleteUser(email: String) {
        _adminUsersList.value = _adminUsersList.value.filter { it.email != email }
    }

    fun verifyDoctor(email: String) {
        _adminUsersList.value = _adminUsersList.value.map {
            if (it.email == email) it.copy(isVerified = !it.isVerified) else it
        }
    }

    fun updateUserRole(email: String, newRole: String) {
        _adminUsersList.value = _adminUsersList.value.map {
            if (it.email == email) it.copy(role = newRole) else it
        }
    }

    fun addAdminUser(user: AdminUser) {
        _adminUsersList.value = _adminUsersList.value + user
    }
}

data class AdminUser(
    val email: String,
    val name: String,
    val role: String, // "Admin", "Doctor", "Pharmacist", "Nurse", "Patient", "Receptionist"
    val phoneNumber: String,
    val info: String = "",
    val isVerified: Boolean = false,
    val status: String = "Active", // "Active" or "Suspended"
    val registeredDate: String = "May 2026"
)

