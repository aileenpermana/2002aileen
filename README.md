BTO Management System

📝 Project Overview
The BTO (Build-To-Order) Management System is a comprehensive Java application designed to streamline the process of managing Build-To-Order housing applications in Singapore. This system provides a robust platform for applicants, HDB officers, and managers to interact with BTO projects efficiently.
🌟 Key Features
User Roles

Applicant:

- View and apply for BTO projects
- Submit and manage enquiries
- Withdraw applications


HDB Officer:

- Register for project management
- Book flats for successful applicants
- Manage project enquiries


HDB Manager:

- Create and manage BTO projects
- Process officer registrations
- Approve/reject applications
- Generate detailed reports



System Capabilities

- User authentication with NRIC-based login
- Detailed project filtering and searching
- Strict eligibility checks for applications
- Comprehensive enquiry management
- Flat booking and allocation system

🛠 Technical Requirements
Prerequisites

Java 17 or higher
No additional external libraries required

Supported Platforms

Windows
macOS
Linux


🚀 Getting Started
Installation

Clone the repository
Ensure Java 17+ is installed
Compile the project using your preferred Java IDE or command line

Running the Application
bash# Compile the project
javac src/App.java

# Run the application
java -cp src App
🔐 User Authentication

Login Format:

NRIC starting with S or T
7 digit middle section
Ending with a letter


Default password: password
Case-insensitive login

📋 Eligibility Criteria
Applicant Eligibility

Single Applicants:

Minimum age: 35 years
Can only apply for 2-Room flats


Married Applicants:

Minimum age: 21 years
Can apply for 2-Room or 3-Room flats



🛡️ Security Notes

Passwords are stored in CSV files
Basic input validation
Role-based access control

📊 Project Filtering Options

Filter by neighborhood
Filter by flat type
Sort by various criteria
View project details

🤝 Contributing
Development Setup

Fork the repository
Create a feature branch
Commit your changes
Push to the branch
Create a Pull Request

Coding Guidelines

Follow Java naming conventions
Write clear, commented code
Maintain existing code structure
Add unit tests for new features

🐛 Known Limitations

File-based storage (not suitable for large-scale use)
Limited user interface
Limited file handling capabilities

🔧 Future Improvements

Implement database backend
Add more robust error handling
Develop graphical user interface
Enhance security features
More efficient and implementation of design system

📄 License
This project is created as part of the SC/CE/CZ2002 Object-Oriented Design & Programming course at Nanyang Technological University.
👥 Contributors

