package com.example.studentassembly.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

// Student Data Class
data class Student(
    val id: Int = 0,
    val studentId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val course: String,
    val year: Int,
    val gpa: Double,
    val enrollmentDate: String = SimpleDateFormat("yyyy-MM-dd").format(Date()),
    val status: String = "Active"
)

// Database Helper Class
class StudentDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_NAME = "student_assembly.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_STUDENTS = "students"

        // Column names
        private const val COLUMN_ID = "id"
        private const val COLUMN_STUDENT_ID = "student_id"
        private const val COLUMN_FIRST_NAME = "first_name"
        private const val COLUMN_LAST_NAME = "last_name"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_COURSE = "course"
        private const val COLUMN_YEAR = "year"
        private const val COLUMN_GPA = "gpa"
        private const val COLUMN_ENROLLMENT_DATE = "enrollment_date"
        private const val COLUMN_STATUS = "status"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_STUDENTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_STUDENT_ID TEXT UNIQUE NOT NULL,
                $COLUMN_FIRST_NAME TEXT NOT NULL,
                $COLUMN_LAST_NAME TEXT NOT NULL,
                $COLUMN_EMAIL TEXT NOT NULL,
                $COLUMN_COURSE TEXT NOT NULL,
                $COLUMN_YEAR INTEGER NOT NULL,
                $COLUMN_GPA REAL NOT NULL,
                $COLUMN_ENROLLMENT_DATE TEXT NOT NULL,
                $COLUMN_STATUS TEXT DEFAULT 'Active'
            )
        """.trimIndent()

        db.execSQL(createTableQuery)
        insertSampleData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STUDENTS")
        onCreate(db)
    }

    // CRUD Operations
    fun addStudent(student: Student): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_STUDENT_ID, student.studentId)
            put(COLUMN_FIRST_NAME, student.firstName)
            put(COLUMN_LAST_NAME, student.lastName)
            put(COLUMN_EMAIL, student.email)
            put(COLUMN_COURSE, student.course)
            put(COLUMN_YEAR, student.year)
            put(COLUMN_GPA, student.gpa)
            put(COLUMN_ENROLLMENT_DATE, student.enrollmentDate)
            put(COLUMN_STATUS, student.status)
        }

        val result = db.insert(TABLE_STUDENTS, null, values)
        db.close()
        return result
    }

    fun getAllStudents(): List<Student> {
        val studentList = mutableListOf<Student>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_STUDENTS ORDER BY $COLUMN_ID DESC"
        val cursor: Cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val student = Student(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    studentId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_ID)),
                    firstName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME)),
                    lastName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    course = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURSE)),
                    year = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_YEAR)),
                    gpa = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_GPA)),
                    enrollmentDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENROLLMENT_DATE)),
                    status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS))
                )
                studentList.add(student)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return studentList
    }

    fun getStudentById(id: Int): Student? {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_STUDENTS WHERE $COLUMN_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(id.toString()))

        return if (cursor.moveToFirst()) {
            Student(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                studentId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_ID)),
                firstName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME)),
                lastName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                course = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURSE)),
                year = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_YEAR)),
                gpa = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_GPA)),
                enrollmentDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENROLLMENT_DATE)),
                status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS))
            )
        } else {
            null
        }.also {
            cursor.close()
            db.close()
        }
    }

    fun updateStudent(student: Student): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_STUDENT_ID, student.studentId)
            put(COLUMN_FIRST_NAME, student.firstName)
            put(COLUMN_LAST_NAME, student.lastName)
            put(COLUMN_EMAIL, student.email)
            put(COLUMN_COURSE, student.course)
            put(COLUMN_YEAR, student.year)
            put(COLUMN_GPA, student.gpa)
            put(COLUMN_ENROLLMENT_DATE, student.enrollmentDate)
            put(COLUMN_STATUS, student.status)
        }

        val result = db.update(
            TABLE_STUDENTS,
            values,
            "$COLUMN_ID = ?",
            arrayOf(student.id.toString())
        )
        db.close()
        return result
    }

    fun deleteStudent(id: Int): Int {
        val db = this.writableDatabase
        val result = db.delete(
            TABLE_STUDENTS,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
        db.close()
        return result
    }

    fun searchStudents(query: String): List<Student> {
        val studentList = mutableListOf<Student>()
        val db = this.readableDatabase

        val searchQuery = """
            SELECT * FROM $TABLE_STUDENTS 
            WHERE $COLUMN_FIRST_NAME LIKE ? 
            OR $COLUMN_LAST_NAME LIKE ? 
            OR $COLUMN_STUDENT_ID LIKE ? 
            OR $COLUMN_COURSE LIKE ?
            ORDER BY $COLUMN_LAST_NAME
        """.trimIndent()

        val cursor = db.rawQuery(searchQuery,
            arrayOf("%$query%", "%$query%", "%$query%", "%$query%"))

        if (cursor.moveToFirst()) {
            do {
                val student = Student(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    studentId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_ID)),
                    firstName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME)),
                    lastName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    course = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURSE)),
                    year = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_YEAR)),
                    gpa = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_GPA)),
                    enrollmentDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENROLLMENT_DATE)),
                    status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS))
                )
                studentList.add(student)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return studentList
    }

    fun getTotalStudents(): Int {
        val db = this.readableDatabase
        val query = "SELECT COUNT(*) FROM $TABLE_STUDENTS"
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count
    }

    fun getAverageGPA(): Double {
        val db = this.readableDatabase
        val query = "SELECT AVG($COLUMN_GPA) FROM $TABLE_STUDENTS"
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        val avg = cursor.getDouble(0)
        cursor.close()
        db.close()
        return avg
    }

    private fun insertSampleData(db: SQLiteDatabase) {
        val sampleStudents = listOf(
            Student(
                studentId = "STU001",
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@university.edu",
                course = "Computer Science",
                year = 2,
                gpa = 3.75,
                status = "Active"
            ),
            Student(
                studentId = "STU002",
                firstName = "Jane",
                lastName = "Smith",
                email = "jane.smith@university.edu",
                course = "Engineering",
                year = 3,
                gpa = 3.92,
                status = "Active"
            ),
            Student(
                studentId = "STU003",
                firstName = "Michael",
                lastName = "Johnson",
                email = "michael.j@university.edu",
                course = "Business",
                year = 1,
                gpa = 3.45,
                status = "Active"
            )
        )

        for (student in sampleStudents) {
            val values = ContentValues().apply {
                put(COLUMN_STUDENT_ID, student.studentId)
                put(COLUMN_FIRST_NAME, student.firstName)
                put(COLUMN_LAST_NAME, student.lastName)
                put(COLUMN_EMAIL, student.email)
                put(COLUMN_COURSE, student.course)
                put(COLUMN_YEAR, student.year)
                put(COLUMN_GPA, student.gpa)
                put(COLUMN_ENROLLMENT_DATE, student.enrollmentDate)
                put(COLUMN_STATUS, student.status)
            }
            db.insert(TABLE_STUDENTS, null, values)
        }
    }
}