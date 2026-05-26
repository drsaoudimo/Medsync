package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patient_profiles")
data class UserEntity(
    @PrimaryKey val email: String,
    val fullName: String,
    val role: String, // "Doctor", "Pharmacist", "Patient", "Nurse"
    val phoneNumber: String,
    val specialty: String = "",
    val emergencyContact: String = "",
    val preferredLanguage: String = "EN", // "EN", "AR", "FR"
    val profilePhotoUri: String = ""
)

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientName: String,
    val doctorName: String,
    val dateTimeStr: String, // e.g. "10:30 AM" Or Date
    val type: String, // "In-Person", "Telehealth"
    val status: String, // "Confirmed", "Pending", "Cancelled"
    val reason: String,
    val language: String = "EN"
)

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // e.g., "Cardiology", "Antibiotic"
    val stockQty: Int,
    val price: Double,
    val dosage: String,
    val expiryDate: String,
    val barcode: String
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
