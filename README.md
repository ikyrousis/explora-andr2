Explora - Android Mobile Application

Explora is a mobile application designed for the Android platform, utilizing Google Maps and Firebase services. It allows users to register, authenticate, and track each other’s locations using GPS, with features like user authentication, real-time location tracking, team-based competition, and gamification.

Implemented in June 2022, by ANDR2 Group A consisting of Ioannis Kyrousis, Kaylee Joy Fürst, Laura van Helden, Nearchos Katsanikakis.

Features

    User Registration & Login: Users can create accounts and authenticate using email/password, with credentials managed securely through Firebase Authentication.
    Location Tracking: Once logged in, users can view their own and other users' locations on Google Maps. Locations are updated in real-time using Firebase Firestore.
    Team-Based Gamification: Users can join teams and capture waypoints on the map for their team, with a scoreboard showing team progress on the home screen.
    Notifications: If a user comes within 10 meters of another user, a notification is triggered, alerting them via vibration and a sound.
    Profile Management: Users can create and update their profiles, including uploading profile images stored in Firebase Cloud Storage.

Core Technologies

    Firebase: Used for authentication, Firestore for real-time database management, and Cloud Storage for handling profile images.
    Google Maps API: Provides real-time tracking and display of user locations.
    Android SDK: Used for core application development, including user interface design and navigation.

App Architecture

    Authentication Module: Implements Firebase’s signInWithEmailAndPassword for secure user authentication.
    Firestore Database: Stores user data such as name, email, location, and team, using Firebase’s Firestore NoSQL database.
    Google Maps Integration: Displays user locations on the map with dynamically updated markers.
    Gamification System: Users can capture waypoints, with scores updated and displayed in real-time.

Tests

    Unit Tests: Test location distance calculations using the Haversine formula to determine proximity.
    Espresso Tests: Include registration, login, and logout tests to ensure core functionalities work as expected.

Configuration

    Maps API Key: An Android-restricted key is defined in local.properties as MAPS_API_KEY. Without it map tiles do not load. The build still succeeds using the fallback in local.defaults.properties.
    Firebase: Registration, login and gameplay require app/google-services.json to point to a Firebase project with Email/Password authentication, Firestore with the Users and Waypoints collections, and Cloud Storage.