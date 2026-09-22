
# CampusFind API Documentation

## Overview

CampusFind uses several APIs and Android development components to provide an interactive, reliable, and user-friendly lost-and-found experience for students and staff. These APIs support campus map functionality, cloud database storage, user authentication, and notification management.

The application uses Google Maps for campus navigation and location-based item markers, Firebase Cloud Firestore for real-time data storage, Firebase Authentication for user account management, and Android's Notification Manager API for displaying notifications.

---

# 1. Google Maps Android SDK & Maps Compose API

## Primary Use Case

The Google Maps Android SDK and Maps Compose API are used to provide interactive campus map views within the CampusFind application.

The map allows users to view campus locations, display lost-and-found item markers, expand the map into a full-screen view, position the camera, and search for campus locations.

The map also helps users identify where an item was reported lost or found.

## Key Components

### GoogleMap

The `GoogleMap` composable displays interactive Google Maps road and satellite tiles inside the Jetpack Compose interface.

It allows users to interact with the map, zoom in and out, and view campus locations.

### Marker and rememberMarkerState

`Marker` displays a pin on the map to represent a reported lost or found item.

The application uses different marker colours to distinguish between item types:

- Red: Lost item.
- Teal/Azure: Found item.

`rememberMarkerState` stores the position of each marker and allows the marker to be associated with a specific report.

### rememberCameraPositionState and CameraUpdateFactory

These components manage the position of the map camera.

They allow the application to move or animate the map camera to a selected campus, searched location, or item location.

### android.location.Geocoder

The Android `Geocoder` class converts a campus name or location query into geographical coordinates.

These coordinates consist of latitude and longitude values, which can then be used to position the map camera and display markers.

If the geocoding process does not return a location, the application uses a fallback coordinate database called `SouthAfricanInstitutions`.

## Code Example: Map Search and Geocoding

**File:** `CampusMapComponents.kt`

```kotlin
// Geocodes a campus query, such as "CPUT" or "Wits",
// into LatLng coordinates.

coroutineScope.launch(Dispatchers.IO) {

    var coords: LatLng? = null

    try {
        val geocoder = android.location.Geocoder(context)

        val addrs = geocoder.getFromLocationName(
            "$campusName, South Africa",
            1
        )

        if (!addrs.isNullOrEmpty()) {
            coords = LatLng(
                addrs[0].latitude,
                addrs[0].longitude
            )
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }

    if (coords == null) {
        // Fallback to coordinates from the
        // SouthAfricanInstitutions database.
        coords =
            SouthAfricanInstitutions
                .getCoordinatesForInstitution(campusName)
    }

    withContext(Dispatchers.Main) {
        // Animates the map camera and moves the pin.
        onLocationFound(coords)
    }
}
```

## Code Example: Interactive Google Map with Markers

```kotlin
GoogleMap(
    modifier = Modifier.fillMaxSize(),
    cameraPositionState = dialogCameraState,
    uiSettings = MapUiSettings(
        zoomControlsEnabled = true,
        compassEnabled = true
    )
) {

    reports.forEach { report ->

        val latLng = LatLng(
            report.effectiveLatitude,
            report.effectiveLongitude
        )

        val isLost = report.type == ReportType.LOST

        val markerHue =
            if (isLost) {
                BitmapDescriptorFactory.HUE_RED
            } else {
                BitmapDescriptorFactory.HUE_AZURE
            }

        Marker(
            state = rememberMarkerState(
                key = "dialog_${report.id}",
                position = latLng
            ),

            title = report.title,

            snippet =
                "${report.type.name} • " +
                report.location.name.replace("_", " "),

            icon =
                BitmapDescriptorFactory
                    .defaultMarker(markerHue),

            onClick = {
                onReportClick(report)
                true
            }
        )
    }
}
```

## Role in CampusFind

The Google Maps API improves the application's location-based functionality by allowing users to:

1. Search for a campus or location.
2. View reported lost and found items on a map.
3. Identify the approximate location where an item was reported.
4. Select an item marker to view its report.
5. Navigate between different campus locations.

---

# 2. Google Firebase Cloud Firestore API

## Primary Use Case

Firebase Cloud Firestore is used as the cloud database for storing and managing CampusFind application data.

It stores information such as:

- Lost-and-found reports.
- User profile information.
- Report statuses.
- Possible item matches.
- Notification-related data.

Firestore supports real-time synchronisation, allowing changes made to the database to be reflected in the application without requiring the user to manually refresh the screen.

## Key Components

### addSnapshotListener

`addSnapshotListener` listens for changes to a Firestore collection or document.

When a report is added, modified, or removed, the listener receives an updated snapshot. The application can then update the user interface automatically.

### set()

The `set()` method creates a new document or replaces the existing contents of a document.

It can be used to save a new report or store a user's profile information.

### update()

The `update()` method changes specific fields in an existing Firestore document without replacing the entire document.

In CampusFind, it can be used to update report statuses such as:

- OPEN.
- MATCH_SUGGESTED.
- CLAIMED.
- RETURNED.

### runBatch()

The `runBatch()` method allows multiple Firestore write operations to be submitted together.

This can be used for related updates, such as linking a lost report to a found report and updating the relevant statuses.

Batch operations help ensure that the related write operations are processed together.

## Code Example: Real-Time Report Listener

**File:** `FirebaseReportRepository.kt`

```kotlin
// Real-time listener for the reports collection.

private fun listenToAllReports() {

    firestore.collection("reports")
        .orderBy(
            "createdAt",
            Query.Direction.DESCENDING
        )
        .addSnapshotListener { snapshot, error ->

            if (error != null) {
                return@addSnapshotListener
            }

            val reports =
                snapshot?.documents?.map { doc ->

                    mapDocumentToReport(
                        doc.id,
                        doc.data ?: emptyMap()
                    )

                } ?: emptyList()

            // Reactively updates the Compose UI.
            _allReports.value = reports
        }
}
```

## Code Example: Updating a Report Status

```kotlin
override suspend fun updateReportStatus(
    reportId: String,
    status: ReportStatus,
    customLabel: String?
): Result<Unit> {

    return try {

        val updates =
            mutableMapOf<String, Any>(
                "status" to status.name
            )

        if (customLabel != null) {
            updates["customStatusLabel"] = customLabel
        }

        firestore
            .collection("reports")
            .document(reportId)
            .update(updates)
            .await()

        Result.success(Unit)

    } catch (e: Exception) {

        Result.failure(e)
    }
}
```

## Role in CampusFind

Cloud Firestore supports the following application functionality:

1. Storing lost-and-found reports.
2. Updating report statuses.
3. Synchronising report changes in real time.
4. Storing user profile information.
5. Supporting administrator report management.
6. Linking possible lost-and-found matches.
7. Supporting the display of updated information across devices.

Firestore security rules should be configured to restrict access to sensitive data and prevent unauthorised users from changing reports or accessing administrator functionality.

---

# 3. Google Firebase Authentication API

## Primary Use Case

Firebase Authentication is used to manage user registration, login, account authentication, and session tracking within the CampusFind application.

It allows students and staff to create accounts and securely sign in using their registered credentials.

The authentication system also provides a way to determine whether a user is currently logged in or logged out.

## Key Components

### signInWithEmailAndPassword

`signInWithEmailAndPassword` authenticates an existing user using their email address and password.

It is used during the login process.

### createUserWithEmailAndPassword

`createUserWithEmailAndPassword` registers a new user account using an email address and password.

After the account is created, the application can store additional user profile information in Cloud Firestore.

### addAuthStateListener

`addAuthStateListener` monitors authentication state changes.

It allows the application to respond when a user signs in, signs out, or when the current authentication session changes.

## Code Example: User Registration

**File:** `FirebaseUserRepository.kt`

```kotlin
override suspend fun register(
    firstName: String,
    lastName: String,
    email: String,
    password: String,
    studentNumber: String,
    institution: String,
    avatarUrl: String?,
    role: UserRole
): Result<User> {

    return try {

        val authResult =
            auth.createUserWithEmailAndPassword(
                email,
                password
            ).await()

        val firebaseUser =
            authResult.user
                ?: throw Exception("Registration failed")

        val user = User(
            id = firebaseUser.uid,
            email = email,
            firstName = firstName,
            lastName = lastName,
            studentStaffNumber = studentNumber,
            role = role,
            institution = institution,
            avatarUrl = avatarUrl
        )

        // Save the user profile document in Firestore.
        firestore
            .collection("users")
            .document(firebaseUser.uid)
            .set(mapUserToMap(user))
            .await()

        Result.success(user)

    } catch (e: Exception) {

        Result.failure(e)
    }
}
```

## Role in CampusFind

Firebase Authentication supports the following functionality:

1. Registering new student and staff accounts.
2. Authenticating existing users.
3. Managing user sessions.
4. Connecting authenticated users to their Firestore profiles.
5. Supporting role-based application functionality.
6. Identifying the user who submitted a lost-and-found report.

User passwords should be managed by Firebase Authentication rather than stored as plain text in the application's Firestore database.

Administrator privileges should also be controlled through secure backend rules and validated role information. Hiding administrator screens in the application alone is not sufficient to protect administrator functionality.

---

# 4. Android System Notification Manager API

## Primary Use Case

The Android System Notification Manager API is used to create and display notifications in the CampusFind application.

Notifications can inform users about important events, such as possible matches, report status changes, and other application updates.

The application uses notification channels to organise different types of notifications.

## Key Components

### NotificationChannel

`NotificationChannel` creates notification categories for devices running Android 8.0 and later.

CampusFind can use separate channels for notifications such as:

- Match Alerts.
- Report Updates.
- General Notifications.

Each channel can have its own importance level and notification settings.

### NotificationCompat.Builder

`NotificationCompat.Builder` creates the content of a notification.

It can be used to specify:

- Notification title.
- Notification message.
- Small icon.
- Priority.
- Click behaviour.
- Whether the notification closes after being selected.

### NotificationManagerCompat

`NotificationManagerCompat` sends the notification to the Android system notification tray.

The application must check notification permission requirements before attempting to display notifications.

## Code Example: Creating Notification Channels

**File:** `NotificationHelper.kt`

```kotlin
// Create notification channels for Android O and later.

fun createNotificationChannels(context: Context) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val matchesChannel = NotificationChannel(
            CHANNEL_MATCHES,
            "Match Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {

            description =
                "Notifications when a lost item matches a found item"

            enableVibration(true)
        }

        manager.createNotificationChannel(matchesChannel)
    }
}
```

## Code Example: Sending a System Notification

```kotlin
// Dispatch a notification to the Android system tray.

fun sendPushNotification(
    context: Context,
    title: String,
    message: String,
    channelId: String = CHANNEL_GENERAL
) {

    if (!hasNotificationPermission(context)) {
        return
    }

    val builder =
        NotificationCompat.Builder(
            context,
            channelId
        )
            .setSmallIcon(
                android.R.drawable.ic_dialog_info
            )
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(
                NotificationCompat.PRIORITY_HIGH
            )
            .setAutoCancel(true)

    NotificationManagerCompat
        .from(context)
        .notify(
            System.currentTimeMillis().toInt(),
            builder.build()
        )
}
```

## Role in CampusFind

The notification API supports the following functionality:

1. Creating notification channels.
2. Displaying report updates.
3. Informing users about possible item matches.
4. Displaying notifications in the Android system tray.
5. Requesting and checking notification permissions.
6. Providing users with timely updates about their reports.

The application must request the `POST_NOTIFICATIONS` permission on supported Android versions where runtime notification permission is required.

---

# 5. API Integration Summary

| API or Component | Purpose in CampusFind |
|---|---|
| Google Maps Android SDK | Displays interactive maps and geographical locations. |
| Maps Compose API | Integrates Google Maps into the Jetpack Compose interface. |
| Android Geocoder | Converts campus and location queries into coordinates. |
| Firebase Cloud Firestore | Stores reports, user data, statuses, and other cloud data. |
| Firebase Authentication | Supports registration, login, and authentication state tracking. |
| Android Notification Manager API | Creates and displays Android system notifications. |

---

# 6. Data Flow Between the APIs

The APIs work together to support the main CampusFind workflow.

1. A user registers or logs in through Firebase Authentication.
2. The user's profile information is stored in Cloud Firestore.
3. The user submits a lost-and-found report.
4. The report is saved in the Firestore `reports` collection.
5. The application uses Google Maps and Geocoder to identify and display the reported location.
6. Reports are displayed on the interactive map using markers.
7. Firestore listeners update the application when reports or statuses change.
8. When a possible match or report update occurs, the notification system can display an alert to the relevant user.

---
