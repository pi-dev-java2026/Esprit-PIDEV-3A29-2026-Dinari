# Esprit-PIDEV-3A29-2026-Dinari

## Overview

DINARI is a comprehensive desktop financial management platform designed to help users manage their finances efficiently. Built with JavaFX, the application integrates multiple modules including educational content management, budget planning with AI-powered predictions, subscription management, expense tracking, and user authentication. The platform leverages modern technologies and artificial intelligence to provide intelligent financial insights and recommendations.

---

## Features

### Education Module
- Course management with full CRUD operations
- Interactive quiz creation with exam mode
- Student reviews and ratings system
- AI-powered content summarization using Groq API
- PDF generation for course materials
- Multi-language translation support

### User Management & Reclamation
- Secure user authentication and authorization
- Role-based access control (Admin, Manager, User)
- User profile management
- Reclamation submission and tracking system
- Session management

### Budget Planning
- Budget creation and tracking
- Activity and planification management
- AI-powered risk prediction using machine learning
- Monthly financial analysis and insights
- Calendar integration for financial planning
- Receipt OCR using Mindee API

### Subscription Management
- Subscription plans management (Normal, Premium, Gold)
- Stripe payment integration
- Google Calendar synchronization
- Payment history tracking
- AI-powered subscription recommendations

### Expense Tracking
- Expense recording and categorization
- Justification document management
- Expense history and dashboard
- Visual analytics and reporting

---

## Tech Stack

### Frontend
- **JavaFX 17** - Desktop UI framework
- **FXML** - UI markup language
- **CSS** - Styling and theming
- **CalendarFX** - Calendar components

### Backend
- **Java 17** - Core programming language
- **Maven** - Build automation and dependency management
- **MySQL 8** - Relational database
- **Python 3.8+** - AI/ML module

### Libraries & APIs
- **Stripe API** - Payment processing
- **Google Calendar API** - Calendar integration
- **Groq API** - AI-powered recommendations
- **Mindee API** - OCR for receipt scanning
- **PDFBox** - PDF generation
- **Smile ML** - Machine learning library
- **Gson** - JSON processing

---

## Architecture

The application follows a modular architecture with clear separation of concerns:

```
DINARI/
├── Education Module (com.gestion)
│   ├── Entities: Cours, Quiz, Avis
│   ├── Services: ServiceCours, ServiceQuiz, ServiceAvis
│   └── Controllers: Course and quiz management
│
├── Fintech Module (Fintech)
│   ├── Entities: User, Reclamation
│   ├── Services: ServiceUser, ServiceReclamation
│   └── Controllers: Authentication and user management
│
├── Budget Module (org.example)
│   ├── AI: BudgetRiskPredictor, MonthlyAnalysisService
│   ├── Services: Budget and activity management
│   └── Controllers: Budget planning and analysis
│
├── Subscription Module (tn.esprit)
│   ├── Entities: Abonnement, Paiement
│   ├── Services: AbonnementService, StripeService
│   └── Controllers: Subscription and payment management
│
└── Expense Module
    ├── Entities: Depense, JustificatifDepense
    ├── Services: ServiceDepense
    └── Controllers: Expense tracking and reporting
```

**Design Patterns:**
- MVC (Model-View-Controller)
- Singleton (Database connections, Navigation)
- Service Layer pattern
- Repository pattern

---

## Contributors

This project was developed by a team of students as part of their academic curriculum:

| Module | Responsibilities |
|--------|------------------|
| **Education** | Course management, quizzes, reviews, AI summarization |
| **Fintech** | User authentication, reclamation system |
| **Budget** | Budget planning, AI predictions, financial analysis |
| **Subscription** | Subscription management, payment processing |
| **Expense** | Expense tracking, justification management |

---

## Academic Context

Developed at **Esprit School of Engineering** – Tunisia  
**PIDEV – 3A | 2025–2026**

This project represents a comprehensive integrated development project (Projet Intégré) that demonstrates the practical application of software engineering principles, including:

- Object-oriented programming and design patterns
- Desktop application development with JavaFX
- Database design and management
- API integration and web services
- Artificial intelligence and machine learning integration
- Team collaboration and version control
- Agile development methodologies

The project showcases the skills acquired throughout the academic program and serves as a capstone project demonstrating proficiency in full-stack desktop application development.

---

## Getting Started

### Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **MySQL 8.0+**
- **Python 3.8+** (for AI module)

### Database Setup

1. Create the required databases:

```sql
CREATE DATABASE pidev;
CREATE DATABASE projetpidev;
CREATE DATABASE gestionabonnement;
```

2. Import the SQL scripts:

```bash
mysql -u root -p pidev < src/main/resources/sql/SIMPLE_MIGRATION.sql
mysql -u root -p pidev < src/main/resources/sql/add_question_column.sql
mysql -u root -p pidev < src/main/resources/sql/complete_quiz_migration.sql
mysql -u root -p gestionabonnement < src/main/resources/sql/subscription_setup.sql
mysql -u root -p projetpidev < src/main/resources/sql/budget_setup.sql
```

3. Update database credentials in the respective configuration files if needed.

### Installation

1. Clone the repository:
```bash
git clone https://github.com/pi-dev-java2026/Esprit-PIDEV-3A29-2026-Dinari.git
cd Esprit-PIDEV-3A29-2026-Dinari
```

2. Install dependencies:
```bash
mvn clean install
```

3. Configure API keys (optional):
   - Copy `local.properties.template` to `local.properties`
   - Add your API keys (Groq, Stripe, Mindee, Google Calendar)

4. Set up Python AI module:
```bash
cd scripts/ai-training/PiApp-IA
python -m venv venv
venv\Scripts\activate
pip install -r requirements.txt
python train.py
```

### Running the Application

**Using Maven:**
```bash
# Run Fintech module (default)
mvn clean javafx:run

# Run User interface
mvn clean javafx:run -Puser

# Run Budget module
mvn clean javafx:run -Pbudget

# Run Subscription module
mvn clean javafx:run -Psubscription

# Run with dynamic launcher (choose at runtime)
mvn clean javafx:run -Plauncher
```

**From IDE (IntelliJ IDEA):**
1. Open project in IntelliJ IDEA
2. Wait for Maven to import dependencies
3. Right-click on a Main class (e.g., `Fintech.MainApp`)
4. Select "Run"

**Default Login Credentials:**
- Admin: `admin@dinari.tn` / `admin123`
- User: `john@dinari.tn` / `user123`

### Building for Production

```bash
# Package as executable JAR
mvn clean package

# Skip tests
mvn clean package -DskipTests
```

---

## Acknowledgments

We would like to express our gratitude to:

- **Esprit School of Engineering** for providing the academic framework and resources for this project
- Our project supervisors and instructors for their guidance and support throughout the development process
- The open-source community for the excellent libraries and frameworks that made this project possible
- Our team members for their dedication, collaboration, and hard work

Special thanks to the developers of JavaFX, MySQL, Stripe, Google APIs, and all other technologies that power this application.

---

**License:** This project is developed for academic purposes as part of the curriculum at Esprit School of Engineering.
## Deployment

This desktop application is packaged as an executable JAR file and can be deployed locally on any machine with Java 17 installed.
