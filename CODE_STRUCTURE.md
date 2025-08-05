# Okane - Code Organization

## Folder Structure

```
app/src/main/java/com/dash_laifu/okane/
├── features/
│   ├── notificationlog/
│   │   ├── ui/                 # UI components (Fragments, Activities, ViewModels)
│   │   ├── data/               # Data layer (Repositories, Data sources)
│   │   └── domain/             # Business logic (Use cases, Domain models)
│   └── [future features]/
│       ├── ui/
│       ├── data/
│       └── domain/
│
├── shared/
│   ├── models/                 # Shared data models across features
│   ├── utils/                  # Utility classes and helper functions
│   ├── services/               # Background services and system integrations
│   └── extensions/             # Kotlin extension functions
│
└── MainActivity.kt             # Main entry point
```

## Guidelines

### Feature Modules
- Each feature should be self-contained within its own package
- Use the three-layer structure: `ui`, `data`, `domain`
- Features should communicate through shared models and services

### Shared Module
- `models/`: Common data classes used across multiple features
- `utils/`: Helper functions, constants, and utility classes
- `services/`: Background services, notification listeners, system integrations
- `extensions/`: Kotlin extension functions for enhanced functionality

### Future Features
When adding new features, create a new package under `features/` following the same structure:
```
features/
├── [feature_name]/
│   ├── ui/
│   ├── data/
│   └── domain/
```

## Current Status
- ✅ Folder structure created
- ✅ Existing fragments moved to `notificationlog/ui/`
- ✅ Placeholder classes added for demonstration
- 🔄 Ready for feature implementation
