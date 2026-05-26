package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        AppointmentEntity::class,
        MedicationEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun medicationDao(): MedicationDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medsync_database"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database)
                }
            }
        }

        suspend fun populateDatabase(db: AppDatabase) {
            // Seed sample medications (Pharmacy inventory)
            db.medicationDao().insertMedication(
                MedicationEntity(
                    name = "Amoxicillin 500mg",
                    category = "Antibiotic",
                    stockQty = 120,
                    price = 14.99,
                    dosage = "1 tablet 3 times a day",
                    expiryDate = "2027-11-20",
                    barcode = "88019902031"
                )
            )
            db.medicationDao().insertMedication(
                MedicationEntity(
                    name = "Atorvastatin 20mg",
                    category = "Cardiology",
                    stockQty = 85,
                    price = 29.50,
                    dosage = "1 tablet at bedtime",
                    expiryDate = "2028-04-12",
                    barcode = "88019902032"
                )
            )
            db.medicationDao().insertMedication(
                MedicationEntity(
                    name = "Metformin 850mg",
                    category = "Diabetes",
                    stockQty = 250,
                    price = 12.00,
                    dosage = "1 tablet twice daily with meals",
                    expiryDate = "2027-08-15",
                    barcode = "88019902033"
                )
            )
            db.medicationDao().insertMedication(
                MedicationEntity(
                    name = "Ibuprofen 400mg",
                    category = "Analgesic",
                    stockQty = 400,
                    price = 5.95,
                    dosage = "1 tablet every 6 hours as needed",
                    expiryDate = "2029-01-30",
                    barcode = "88019902034"
                )
            )
            db.medicationDao().insertMedication(
                MedicationEntity(
                    name = "Lisinopril 10mg",
                    category = "Hypertension",
                    stockQty = 15, // Low stock alert!
                    price = 18.25,
                    dosage = "1 tablet daily inside morning",
                    expiryDate = "2027-04-01",
                    barcode = "88019902035"
                )
            )

            // Seed sample appointments
            db.appointmentDao().insertAppointment(
                AppointmentEntity(
                    patientName = "Mohammed Al-Fayed",
                    doctorName = "Dr. Sarah Ahmed",
                    dateTimeStr = "10:30 AM",
                    type = "Telehealth",
                    status = "Confirmed",
                    reason = "Hypertension routine checkup"
                )
            )
            db.appointmentDao().insertAppointment(
                AppointmentEntity(
                    patientName = "Isabelle Dubois",
                    doctorName = "Dr. Sarah Ahmed",
                    dateTimeStr = "11:15 AM",
                    type = "In-Person",
                    status = "Pending",
                    reason = "Lab results review"
                )
            )
            db.appointmentDao().insertAppointment(
                AppointmentEntity(
                    patientName = "Jean Dupont",
                    doctorName = "Dr. Sarah Ahmed",
                    dateTimeStr = "02:00 PM",
                    type = "In-Person",
                    status = "Confirmed",
                    reason = "Cardiovascular initial diagnostic"
                )
            )

            // Seed sample AI welcome message
            db.chatDao().insertMessage(
                ChatMessageEntity(
                    sender = "ai",
                    text = "Welcome to MedSync AI Medical Assistant. I am here to assist with symptom analysis, drug checking, lab report interpretation, and clinical information based on general medical data. How can I help you today?"
                )
            )
        }
    }
}
