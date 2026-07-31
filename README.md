<p align="center">
  <img src="assets/banner.png" alt="Gatherly - A workflow and social events app" width="100%">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose">
  <img src="https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black" alt="Firebase">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white" alt="Figma">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Process-Scrum-0A66C2?style=for-the-badge" alt="Scrum">
  <img src="https://img.shields.io/badge/Team-7%20Engineers-1c7293?style=for-the-badge" alt="Team of 7">
  <img src="https://img.shields.io/badge/EPFL-Software%20Enterprise-d6001c?style=for-the-badge" alt="EPFL Software Enterprise">
</p>

<p align="center">
  Gatherly blends a personal productivity toolkit with social event coordination,<br>
  built over 10 sprints by a team of 7 for EPFL's <strong>Software Enterprise</strong> (CS-311) Bachelor's project.
</p>

---

## Table of Contents

- [About](#about)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Team & Process](#team--process)
- [Getting Started](#getting-started)
- [License](#license)

---

## About

Arriving at EPFL can be complicated: you might not have built working habits, nor connected with friends to work with, or you might be overwhelmed by the number of tasks to do.

Gatherly aims to help you achieve your academic goals by connecting you with people who are looking for study groups or friends, while encouraging you to focus without being distracted by your phone, thanks to a timer that rewards you the more time you spend focusing, plus a to-do list to keep it all organized. **Manage your workflow and your social circle!**

The project was built from scratch over a single semester by **Team 22** (7 engineers), following Scrum, with weekly retros and a fully cloud-synced backend on Firebase.

---

## Features

<table>
<tr>
<td align="center" width="20%"><img src="screenshots/welcome.png" width="150"><br><sub><b>🔐 Sign in, your way</b><br>Google sync or anonymous - your call</sub></td>
<td align="center" width="20%"><img src="screenshots/home.png" width="150"><br><sub><b>🏠 Home</b><br>Upcoming events, tasks, and friends at a glance</sub></td>
<td align="center" width="20%"><img src="screenshots/focus-timer.png" width="150"><br><sub><b>⏱️ Focus Timer</b><br>Turn deep work into focus points</sub></td>
<td align="center" width="20%"><img src="screenshots/leaderboard.png" width="150"><br><sub><b>🏆 Leaderboard</b><br>Friendly competition, ranked by focus</sub></td>
<td align="center" width="20%"><img src="screenshots/todo.png" width="150"><br><sub><b>✅ To-Do Lists</b><br>Color-coded, never untangled</sub></td>
</tr>
<tr>
<td align="center" width="20%"><img src="screenshots/map.png" width="150"><br><sub><b>🗺️ Map</b><br>Events and tasks, mapped to you</sub></td>
<td align="center" width="20%"><img src="screenshots/notifications.png" width="150"><br><sub><b>🔔 Friends &amp; Notifications</b><br>Build your circle, stay in the loop</sub></td>
<td align="center" width="20%"><img src="screenshots/groups.png" width="150"><br><sub><b>👥 Groups</b><br>Bundle friends for private events</sub></td>
<td align="center" width="20%"><img src="screenshots/badges.png" width="150"><br><sub><b>🥇 Badges</b><br>Unlock achievements as you use the app</sub></td>
<td align="center" width="20%"><img src="screenshots/profile.png" width="150"><br><sub><b>👤 Profile &amp; Settings</b><br>Personal profile with focus points and badges</sub></td>
</tr>
</table>

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | **Kotlin** |
| UI | **Jetpack Compose**, following Material UI/UX guidelines |
| Backend | **Firebase Firestore** - authentication, cloud storage, real-time sync |
| Cloud Functions | **Firebase Cloud Functions** (JavaScript) - scheduled weekly leaderboard resets and focus point rewards |
| Design | **Figma** wireframes + architecture diagrams, evolved alongside each feature |
| Collaboration | GitHub workflows with mandatory peer code review on every PR |

---

## Team & Process

**Team 22** - Claire Chaffard, Alessandro Cioffi, Colombe Jourdan, Claudia Jovignot-Puerto, Gersende Kerjan, Mohamed Kharrat, Gabriel Pineda Serna

- Working collaboratively under **Scrum**
- Weekly Scrum meetings, plus stand-ups twice a week
- Work planned and tracked on a Scrum board across **10 sprints** (Sep 29 → Dec 18)
- Every change merged through a GitHub PR with mandatory peer review

---

## Getting Started

```bash
git clone https://github.com/SwentProj2025/gatherly.git
```

1. Open the project in **Android Studio**.
2. Add your own `google-services.json` (Firebase project config) to the app module.
3. Let Gradle sync, then run on an emulator or physical device.

---

## License

This project is licensed under the **MIT License** - see [LICENSE](LICENSE) for details.

---

<p align="center"><sub>Built with 💙 at EPFL.</sub></p>
