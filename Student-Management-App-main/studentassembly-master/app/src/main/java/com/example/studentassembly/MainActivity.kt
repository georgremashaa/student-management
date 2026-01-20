package com.example.studentassembly

import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.studentassembly.database.Student
import com.example.studentassembly.database.StudentDatabaseHelper
import com.example.studentassembly.ui.theme.StudentassemblyTheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use applicationContext to avoid leaks
        val dbHelper = StudentDatabaseHelper(applicationContext)

        setContent {
            StudentassemblyTheme {
                // Pass dbHelper into a ViewModel or repository ideally
                StudentAssemblyApp(dbHelper)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAssemblyApp(dbHelper: StudentDatabaseHelper) {
    // State variables
    var students by remember { mutableStateOf(dbHelper.getAllStudents()) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedStudent by remember { mutableStateOf<Student?>(null) }

    // Form fields
    var studentId by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var year by remember { mutableStateOf(1) }
    var gpa by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Active") }

    // Function to refresh student list
    fun refreshStudents() {
        students = dbHelper.getAllStudents()
    }

    // Function to clear form
    fun clearForm() {
        studentId = ""
        firstName = ""
        lastName = ""
        email = ""
        course = ""
        year = 1
        gpa = ""
        status = "Active"
        selectedStudent = null
    }

    // Function to fill form for editing
    fun fillFormForEdit(student: Student) {
        selectedStudent = student
        studentId = student.studentId
        firstName = student.firstName
        lastName = student.lastName
        email = student.email
        course = student.course
        year = student.year
        gpa = student.gpa.toString()
        status = student.status
    }

    // Function to save student
    fun saveStudent() {
        if (studentId.isBlank() || firstName.isBlank() || lastName.isBlank() ||
            email.isBlank() || course.isBlank() || gpa.isBlank()) {
            return
        }

        val gpaValue = gpa.toDoubleOrNull() ?: 0.0
        val student = Student(
            id = selectedStudent?.id ?: 0,
            studentId = studentId,
            firstName = firstName,
            lastName = lastName,
            email = email,
            course = course,
            year = year,
            gpa = gpaValue,
            status = status
        )

        if (selectedStudent == null) {
            // Add new student
            dbHelper.addStudent(student)
        } else {
            // Update existing student
            dbHelper.updateStudent(student)
        }

        refreshStudents()
        clearForm()
        showAddDialog = false
        showEditDialog = false
    }

    // Main Scaffold
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Assembly") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    clearForm()
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Add Student")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    if (it.isEmpty()) {
                        refreshStudents()
                    } else {
                        students = dbHelper.searchStudents(it)
                    }
                },
                label = { Text("Search students...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Statistics Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Total Students Card
                Card(
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Total Students",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            dbHelper.getTotalStudents().toString(),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }

                // Average GPA Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Average GPA",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            String.format("%.2f", dbHelper.getAverageGPA()),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Students List
            if (students.isEmpty()) {
                // Empty State
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "No Students",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No students found",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Add your first student using the + button",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(students) { student ->
                        StudentCard(
                            student = student,
                            onEditClick = {
                                fillFormForEdit(student)
                                showEditDialog = true
                            },
                            onDeleteClick = {
                                selectedStudent = student
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }

        // Add Student Dialog
        if (showAddDialog) {
            StudentFormDialog(
                title = "Add Student",
                studentId = studentId,
                firstName = firstName,
                lastName = lastName,
                email = email,
                course = course,
                year = year,
                gpa = gpa,
                status = status,
                onStudentIdChange = { studentId = it },
                onFirstNameChange = { firstName = it },
                onLastNameChange = { lastName = it },
                onEmailChange = { email = it },
                onCourseChange = { course = it },
                onYearChange = { year = it },
                onGpaChange = { gpa = it },
                onStatusChange = { status = it },
                onSave = { saveStudent() },
                onDismiss = {
                    clearForm()
                    showAddDialog = false
                }
            )
        }

        // Edit Student Dialog
        if (showEditDialog) {
            StudentFormDialog(
                title = "Edit Student",
                studentId = studentId,
                firstName = firstName,
                lastName = lastName,
                email = email,
                course = course,
                year = year,
                gpa = gpa,
                status = status,
                onStudentIdChange = { studentId = it },
                onFirstNameChange = { firstName = it },
                onLastNameChange = { lastName = it },
                onEmailChange = { email = it },
                onCourseChange = { course = it },
                onYearChange = { year = it },
                onGpaChange = { gpa = it },
                onStatusChange = { status = it },
                onSave = { saveStudent() },
                onDismiss = {
                    clearForm()
                    showEditDialog = false
                },
                showDeleteButton = true,
                onDelete = {
                    selectedStudent?.let {
                        dbHelper.deleteStudent(it.id)
                        refreshStudents()
                        clearForm()
                        showEditDialog = false
                    }
                }
            )
        }

        // Delete Confirmation Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Student") },
                text = {
                    Text("Are you sure you want to delete ${selectedStudent?.firstName} ${selectedStudent?.lastName}?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedStudent?.let {
                                dbHelper.deleteStudent(it.id)
                                refreshStudents()
                                showDeleteDialog = false
                                selectedStudent = null
                            }
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCard(
    student: Student,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = onEditClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row with ID and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    student.studentId,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Row {
                    // Status badge
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(
                                color = when (student.status) {
                                    "Active" -> Color.Green.copy(alpha = 0.2f)
                                    "Graduated" -> Color.Blue.copy(alpha = 0.2f)
                                    "Inactive" -> Color.Gray.copy(alpha = 0.2f)
                                    else -> Color.Red.copy(alpha = 0.2f)
                                },
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            student.status,
                            style = MaterialTheme.typography.labelSmall,
                            color = when (student.status) {
                                "Active" -> Color.Green
                                "Graduated" -> Color.Blue
                                "Inactive" -> Color.Gray
                                else -> Color.Red
                            }
                        )
                    }

                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Student Name
            Text(
                "${student.firstName} ${student.lastName}",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Course and Year
            Text(
                "${student.course} • Year ${student.year}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Email
            Text(
                student.email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Footer with GPA and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "GPA: ${String.format("%.2f", student.gpa)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

            Text(
                    student.enrollmentDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentFormDialog(
    title: String,
    studentId: String,
    firstName: String,
    lastName: String,
    email: String,
    course: String,
    year: Int,
    gpa: String,
    status: String,
    onStudentIdChange: (String) -> Unit,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onCourseChange: (String) -> Unit,
    onYearChange: (Int) -> Unit,
    onGpaChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    showDeleteButton: Boolean = false,
    onDelete: (() -> Unit)? = null
) {
    var expandedYear by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }
    val years = listOf(1, 2, 3, 4)
    val statuses = listOf("Active", "Inactive", "Graduated", "Suspended")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Student ID
                OutlinedTextField(
                    value = studentId,
                    onValueChange = onStudentIdChange,
                    label = { Text("Student ID *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = studentId.isBlank()
                )

                // First Name
                OutlinedTextField(
                    value = firstName,
                    onValueChange = onFirstNameChange,
                    label = { Text("First Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = firstName.isBlank()
                )

                // Last Name
                OutlinedTextField(
                    value = lastName,
                    onValueChange = onLastNameChange,
                    label = { Text("Last Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = lastName.isBlank()
                )

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()
                )

                // Course
                OutlinedTextField(
                    value = course,
                    onValueChange = onCourseChange,
                    label = { Text("Course *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = course.isBlank()
                )

                // Year Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedYear,
                    onExpandedChange = { expandedYear = !expandedYear }
                ) {
                    OutlinedTextField(
                        value = "Year $year",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Year") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedYear)
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = expandedYear,
                        onDismissRequest = { expandedYear = false }
                    ) {
                        years.forEach { y ->
                            DropdownMenuItem(
                                text = { Text("Year $y") },
                                onClick = {
                                    onYearChange(y)
                                    expandedYear = false
                                }
                            )
                        }
                    }
                }



                OutlinedTextField(
                    value = gpa,
                    onValueChange = onGpaChange,
                    label = { Text("GPA * (0.0 - 4.0)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = gpa.isBlank() || (gpa.toDoubleOrNull()?.let { it !in 0.0..4.0 } ?: true)
                )

                // Status Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedStatus,
                    onExpandedChange = { expandedStatus = !expandedStatus }
                ) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus)
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = expandedStatus,
                        onDismissRequest = { expandedStatus = false }
                    ) {
                        statuses.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = {
                                    onStatusChange(s)
                                    expandedStatus = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = studentId.isNotBlank() &&
                        firstName.isNotBlank() &&
                        lastName.isNotBlank() &&
                        email.isNotBlank() &&
                        Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                        course.isNotBlank() &&
                        gpa.isNotBlank() &&
                        (gpa.toDoubleOrNull()?.let { it in 0.0..4.0 } ?: false)

            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                if (showDeleteButton && onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
