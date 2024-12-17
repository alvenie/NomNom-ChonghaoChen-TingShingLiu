# NomNom: Restaurant Roulette App

NomNom is an innovative Android application that helps users discover new restaurants in their area through a fun and interactive roulette-style selection process. The app combines location-based services, user authentication, and a sleek user interface to provide a unique dining experience.

## Features

### Authentication
- User registration with email, password, and display name
- Secure login system
- Profile management with customizable display picture

### Location-based Restaurant Search
- Utilizes Google Maps API to obtain the user's current location
- Adjustable search radius using an interactive slider
- Filters restaurants based on the selected range

### Restaurant Discovery
- Displays a list of filtered restaurants with details such as name, rating, address, and distance
- Allows users to view restaurant details on Yelp
- Random restaurant selection feature for indecisive users

### Favorites
- Users can add restaurants to their favorites list
- View and manage favorite restaurants
- Quick access to preferred dining spots

### Friend System
- Add friends using email addresses
- Accept or reject incoming friend requests
- View friends list and initiate chats

### Chat Functionality
- Real-time messaging with friends
- Message history and scrollable chat interface

## Technical Details

### Architecture
- Built using Jetpack Compose for modern UI development
- Utilizes MVVM (Model-View-ViewModel) architecture
- Implements Firebase for authentication and real-time database

### Key Components
1. **LoginPage & SignupPage**: Handle user authentication
2. **HomePage**: Displays the map and search radius slider
3. **SearchPage**: Shows the list of filtered restaurants
4. **RoulettePage**: Implements the random restaurant selection with animation
5. **ProfilePage**: Manages user profile and app settings
6. **FavoritesPage**: Displays and manages user's favorite restaurants
7. **FriendsPage**: Handles friend management and requests
8. **ChatPage**: Provides real-time messaging functionality

### HomeViewModel

The `HomeViewModel` class is responsible for managing the app's main functionality:

- Fetching and storing restaurant data
- Handling restaurant search and filtering
- Managing the loading state
- Selecting a random restaurant for the roulette feature

# AuthViewModel

The `AuthViewModel` is a crucial component of the NomNom restaurant roulette app, handling user authentication, profile management, and favorites functionality.

## Features

### Authentication
- User login and signup
- Logout functionality
- Authentication state management

### User Profile
- Fetch and update username
- Update and fetch profile picture
- Manage user document in Firestore

### Favorites
- Add restaurants to favorites
- Remove restaurants from favorites
- Fetch user's favorite restaurants
- Check favorite status of a restaurant

### Friends
- Fetch friends list with display names

## Key Components

### State Management
- `authState`: Manages the current authentication state (Authenticated, Unauthenticated, Loading, Error)
- `username`: Stores the current user's display name
- `profilePictureUrl`: Stores the URL of the user's profile picture
- `favorites`: Maintains a list of user's favorite restaurants
- `friends`: Stores a list of user's friends with their display names

### Firebase Integration
- Utilizes Firebase Authentication for user management
- Interacts with Firestore for storing user data and favorites
- Uses Firebase Storage for profile picture uploads

### Libraries and APIs
- Google Maps API for location services
- Firebase Authentication and Firestore for backend services
- Yelp for restaurant details
- Coil for efficient image loading
- Lottie for smooth animations

## Getting Started

To run the NomNom app:

1. Clone the repository
2. Set up a Firebase project and add the `google-services.json` file to the app directory
3. Enable Google Maps API and add the API key to your `local.properties` file
4. Build and run the app on an Android device or emulator

## Future Enhancements

- Implement dietary preference filters
- Add restaurant reviews and ratings within the app
- Integrate with more restaurant APIs for broader coverage
- Develop group dining features for coordinating with friends

NomNom aims to make dining decisions fun and effortless, providing users with a delightful way to explore new culinary experiences in their area.
