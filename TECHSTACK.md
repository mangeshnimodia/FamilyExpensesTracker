# Tech Stack

This document summarizes the tech stack used in the FamilyExpensesTracker project.

### Core Language & Frameworks
* **Language**: Kotlin
* **UI Framework**: Jetpack Compose with Material Design 3.
* **Navigation**: Jetpack Navigation Compose for screen-to-screen routing.
* **Concurrency**: Kotlin Coroutines and Flow for asynchronous operations and reactive data streams.

### Data & Networking
* **Remote Storage**: Google Sheets API (v4). The app uses Google Sheets as its primary backend.
* **Networking**: Retrofit 2 for HTTP requests.
* **Local Persistence**: SharedPreferences with Gson for serializing complex objects.
* **Architecture**: MVVM (Model-View-ViewModel) with a clean separation between UI, ViewModels, and Repositories.

### Authentication & Identity
* **Google Sign-In**: Integrated using Google Play Services Auth and Android Credentials Manager.

### Dependency Injection
* **Manual DI**: Manual instantiation and wiring of dependencies (DataSources, Repositories) in `MainActivity`.

### Testing Stack
* **Unit Testing**: JUnit 4, MockK, Turbine, and Google Truth.
* **UI Testing**: Compose Test library and Espresso.

### Build & Environment
* **Build System**: Gradle with Kotlin DSL.
* **Java Version**: JDK 17.
* **Android SDK**: Target SDK 34, Minimum SDK 26.
