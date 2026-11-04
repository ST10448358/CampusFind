
<h1 align="center"> CampusFind</h1>
<h3 align="center"> Campus Lost and Found App</h3>

<p align="center">
  Find it. Report it. Reunite it.
</p>



## About the App

CampusFind is a mobile lost-and-found application designed to help students report, search for, and recover lost items within their campus environment.

The application aims to provide a centralised platform where students can submit lost-item and found-item reports, search for reported items, and view information about their submitted reports.

CampusFind includes an AI-powered matching system designed to identify potential matches between lost and found reports. The application also incorporates an AI chatbot to assist users and voice search functionality to make searching for reported items more convenient.
The app uses Firebase as its backend service and Cloud Firestore as its database for storing application data.


## Project Links

<p align="center">
  <a href="https://github.com/ST10448358/CampusFind.git">
    <img src="https://img.shields.io/badge/View%20Code-GitHub-black?style=for-the-badge&logo=github">
  </a>
  <br><br>
  <a href="https://drive.google.com/file/d/1syMCbweyLr37D7T9dvljcqiRGpAKjYjX/view?usp=sharing">
    <img src="https://img.shields.io/badge/Watch%20Demo-Google%20Drive-red?style=for-the-badge&logo=youtube">
  
</p>

Click here if icon is not working:

GitHub repository link: https://github.com/ST10448358/CampusFind.git

Google Drive demonstration video link: https://drive.google.com/file/d/1syMCbweyLr37D7T9dvljcqiRGpAKjYjX/view?usp=sharing



## Admin login details
Admin Email: admin@campus.ac.za <br>
Admin Password: AdminPassword123!



## Design Considerations

CampusFind was designed with a focus on usability, simplicity, and an organised user experience. The application aims to provide clear navigation so that students can access essential functions such as reporting lost or found items, searching for reported items, and managing their account settings.

The user interface is designed to use a consistent layout, readable text, and appropriate icons to make the application easy to navigate. Input validation is considered to help prevent incomplete or invalid information from being submitted.

The application also incorporates an administrator role. The administrator section supports report management, the review of potential matches between lost and found items, and the management of reported items.

Firebase was selected as the backend service because it provides authentication and cloud database functionality that can be integrated with the Android application. Cloud Firestore is used to store structured application data.

TAdditional design considerations include dark mode, image uploads, profile customisation, and multi-language support. The application supports English, Setswana, and isiZulu to make it accessible to a wider range of students.



## Key Features

- User registration and login.
- Google Single Sign-On (SSO).
- Reporting lost and found items.
- Uploading pictures when reporting items.
- Searching for reported items.
- Voice search functionality.
- AI-powered item matching system.
- AI chatbot assistance.
- Administrator-assisted report management.
- Ownership verification and item return code.
- User profile editing and account settings.
- Dark mode.
- Firebase Cloud Messaging (FCM) notifications.
- Multi-language support (English, Setswana, and isiZulu).
- Firebase backend integration.
- Cloud Firestore database.




## Technologies Used

- **Programming Language:** Kotlin
- **Development Environment:** Android Studio
- **Backend:** Firebase
- **Database:** Cloud Firestore
- **Authentication:** Firebase Authentication and Google Single Sign-On
- **Version Control:** Git and GitHub
- **Continuous Integration:** GitHub Actions
- **Testing:** Android Instrumented Tests





## GitHub Utilisation

GitHub is used as the version control and collaboration platform for the CampusFind application. The repository stores the project's source code and supports the management of changes throughout the development process.

Git enables the development team to track changes made to the application, while GitHub provides a centralised repository for storing and managing project files. The team can use commits to record development progress and branches to work on features separately before merging changes into the main branch.

GitHub also supports project organisation and collaboration by providing a shared location for the development team to access the application source code. This helps maintain an organised development process and provides a record of changes made during the project.


## GitHub Actions

GitHub Actions is used to support the continuous integration process for the CampusFind application. A workflow has been configured to run when code is pushed to the repository and when pull requests are created or updated, according to the workflow configuration.

The GitHub Actions workflow is intended to provide an automated environment for validating changes to the application. This allows the development team to integrate automated development checks into the version control process.

The current workflow triggers include:

- **Push:** The workflow runs when changes are pushed to the repository.
- **Pull Request:** The workflow runs when a pull request triggers the configured workflow conditions.

Android instrumented tests are used to test application functionality on an Android device or emulator. These tests support the verification of application behaviour and help identify potential issues during development.

The specific build and testing steps performed by GitHub Actions depend on the configured workflow file. Additional workflow steps can be added as the project develops.





## API Documentation

For a detailed explanation of the APIs used in CampusFind, including their purposes, key components, and code examples, view the documentation below.

[View API Documentation](API_DOCUMENTATION.md)




## Google Play Store Listing

View the proposed Google Play Store listing for CampusFind, including the application description, features, screenshots, and store information.

[View Google Play Store Listing](docs/GOOGLE_PLAY_STORE_LISTING.md)

## AI Usage Declaration

Artificial Intelligence (AI) tools were used throughout the development of the CampusFind application to support various stages of the software development process.

AI was used to assist with identifying and resolving application crashes, troubleshooting coding errors, understanding technical problems, and finding possible solutions when challenges occurred during development. It was also used to provide guidance when implementing functionality or working with technologies and concepts that the development team was unfamiliar with.

Furthermore, AI tools assisted with researching the application, gathering additional information relevant to the project, and recommending possible features and improvements that could enhance the application's functionality and user experience.

AI was therefore used as a supporting resource for research, learning, debugging, problem-solving, feature recommendations, and improving development efficiency. The development team remained responsible for the application's design, implementation, decisions, and overall development.


