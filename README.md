# Freelancer API Hackathon Android Application
Base code was provided by Freelancer for their API Hackathon. Original project was broken and required some additional work.

The original application fetched a list of recommended Freelancer projects for your account. The new application allows you to swipe these projects _left_ or _right_ into one of two 'piles'; 'interested' or 'not interested'. Projects can be demoted or promoted from these piles. 

The goal was to allow a user to gradually filter down a list of projects they are interested in with minimal friction, and without concern of losing track of projects- if they swipe away a project, they can still browse through the 'not interested' list and reconsider them before clearing the list and deleting them.

# Original Documentation- Android Sample App
A simple Android application that demonstrates how to use the Freelancer API.

At a high level, this application downloads and displays the recommended
projects of a particular user.

> Don't forget to add a valid freelancer username and password in MainActivity

## Libraries used in this sample include:
- [Retrofit](http://square.github.io/retrofit/) for asynchronous HTTP
- [Butterknife](http://jakewharton.github.io/butterknife/) for view injection
- [GSON](https://github.com/google/gson) for JSON serialization

## This project uses the following versions of the build tools:
- Gradle - 1.2.3
- Build Tools - 23.0.0
- Min SDK Version - 14
- Target SDK Version - 23

You are free to change any of them to fit your development environment, but we
cannot guarantee that the app will build smoothly if you do.
