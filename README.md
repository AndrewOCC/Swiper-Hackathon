# Freelancer API Hackathon Android Application

Base code was provided by Freelancer for their API Hackathon. Original project was broken and required some additional work.

The application fetches a list of recommended Freelancer projects for your account. You can swipe projects left or right into one of two piles: "interested" or "not interested". Projects can be demoted or promoted from these piles.

> Don't forget to add a valid freelancer username and password in `MainActivity`.

## Libraries used in this sample include:
- [Retrofit 2](https://square.github.io/retrofit/) for asynchronous HTTP
- [OkHttp 4](https://square.github.io/okhttp/) for HTTP client and interceptors
- [GSON](https://github.com/google/gson) for JSON serialization
- [AndroidX](https://developer.android.com/jetpack/androidx) for UI components

## Build configuration:
- Gradle - 8.13
- Android Gradle Plugin - 8.13.2
- Compile SDK - 35 (Android 15)
- Min SDK - 24 (Android 7.0)
- Target SDK - 35 (Android 15)
- Java - 17

You are free to change any of them to fit your development environment, but we cannot guarantee that the app will build smoothly if you do.
