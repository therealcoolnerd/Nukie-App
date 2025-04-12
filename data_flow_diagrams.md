# Nukie Social Media Aggregator - Data Flow Diagrams

## Overview

This document provides visual representations of the data flows within the Nukie social media aggregator, focusing on how data moves through the system while remaining on the user's device. These diagrams complement the client-side storage architecture document by illustrating the relationships between components.

## Authentication Flow

```
┌─────────────┐     ┌───────────────┐     ┌─────────────────────┐
│             │     │               │     │                     │
│  Nukie App  │────▶│  OAuth Flow   │────▶│  Social Media APIs  │
│             │     │               │     │                     │
└─────────────┘     └───────────────┘     └─────────────────────┘
       │                    │                       │
       │                    ▼                       │
       │            ┌───────────────┐               │
       │            │  Auth Tokens  │               │
       │            │  (Encrypted)  │               │
       │            └───────────────┘               │
       │                    │                       │
       ▼                    ▼                       ▼
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│                     User's Device Only                      │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Content Synchronization Flow

```
┌─────────────┐     ┌───────────────┐     ┌─────────────────────┐
│             │     │  Auth Tokens  │     │                     │
│  Nukie App  │────▶│  (Encrypted)  │────▶│  Social Media APIs  │
│             │     │               │     │                     │
└─────────────┘     └───────────────┘     └─────────────────────┘
       │                                           │
       │                                           │
       │                                           ▼
       │                                  ┌─────────────────────┐
       │                                  │                     │
       │                                  │   Content Response  │
       │                                  │                     │
       │                                  └─────────────────────┘
       │                                           │
       ▼                                           ▼
┌─────────────┐                          ┌─────────────────────┐
│             │                          │                     │
│  Room DB    │◀─────────────────────────│   Content Parser    │
│             │                          │                     │
└─────────────┘                          └─────────────────────┘
       │                                           │
       │                                           ▼
       │                                  ┌─────────────────────┐
       │                                  │                     │
       │                                  │   Media Downloader  │
       │                                  │                     │
       │                                  └─────────────────────┘
       │                                           │
       │                                           ▼
       │                                  ┌─────────────────────┐
       ▼                                  │                     │
┌─────────────────────────────────────────│   Local File Cache  │
│                                         │                     │
│          User's Device Only             └─────────────────────┘
│                                                             
└─────────────────────────────────────────────────────────────┘
```

## Post Creation and Publishing Flow

```
┌─────────────┐     ┌───────────────┐     
│             │     │               │     
│  User Input │────▶│  Draft Post   │     
│             │     │               │     
└─────────────┘     └───────────────┘     
                            │             
                            ▼             
                    ┌───────────────┐     
                    │               │     
                    │  Room DB      │     
                    │  (Local Save) │     
                    │               │     
                    └───────────────┘     
                            │             
                            ▼             
┌─────────────┐     ┌───────────────┐     ┌─────────────────────┐
│             │     │               │     │                     │
│  Publish    │────▶│  Auth Tokens  │────▶│  Social Media APIs  │
│  Command    │     │  (Encrypted)  │     │                     │
│             │     │               │     │                     │
└─────────────┘     └───────────────┘     └─────────────────────┘
                                                   │
                                                   ▼
                                          ┌─────────────────────┐
                                          │                     │
                                          │   Status Response   │
                                          │                     │
                                          └─────────────────────┘
                                                   │
                                                   ▼
                                          ┌─────────────────────┐
                                          │                     │
                                          │   Update Local DB   │
                                          │                     │
                                          └─────────────────────┘
                                                   │
                                                   ▼
                                          ┌─────────────────────┐
                                          │                     │
                                          │   User's Device     │
                                          │                     │
                                          └─────────────────────┘
```

## Token System Flow

```
┌─────────────┐     ┌───────────────┐     ┌─────────────────────┐
│             │     │               │     │                     │
│  User       │────▶│  App Actions  │────▶│  Token Rules Engine │
│  Activity   │     │               │     │                     │
│             │     │               │     │                     │
└─────────────┘     └───────────────┘     └─────────────────────┘
                                                   │
                                                   ▼
                                          ┌─────────────────────┐
                                          │                     │
                                          │   Token Calculator  │
                                          │                     │
                                          └─────────────────────┘
                                                   │
                                                   ▼
                                          ┌─────────────────────┐
                                          │                     │
                                          │   Token DB Update   │
                                          │                     │
                                          └─────────────────────┘
                                                   │
                                                   ▼
                                          ┌─────────────────────┐
                                          │                     │
                                          │   Transaction Log   │
                                          │                     │
                                          └─────────────────────┘
                                                   │
                                                   ▼
                                          ┌─────────────────────┐
                                          │                     │
                                          │   User's Device     │
                                          │                     │
                                          └─────────────────────┘
```

## Offline Mode Flow

```
┌─────────────┐     ┌───────────────┐     ┌─────────────────────┐
│             │     │               │     │                     │
│  User       │────▶│  App Actions  │────▶│  Connection Check   │
│  Activity   │     │               │     │                     │
│             │     │               │     │                     │
└─────────────┘     └───────────────┘     └─────────────────────┘
                                                   │
                                                   ▼
                                          ┌─────────────────────┐
                                          │                     │
                                          │   Offline?          │
                                          │                     │
                                          └─────────────────────┘
                                                   │
                                         ┌─────────┴─────────┐
                                         │                   │
                                         ▼                   ▼
                               ┌─────────────────┐  ┌─────────────────┐
                               │                 │  │                 │
                               │  Action Queue   │  │  Direct Action  │
                               │                 │  │                 │
                               └─────────────────┘  └─────────────────┘
                                         │                   │
                                         ▼                   │
                               ┌─────────────────┐           │
                               │                 │           │
                               │  Sync Manager   │           │
                               │                 │           │
                               └─────────────────┘           │
                                         │                   │
                                         ▼                   ▼
                               ┌─────────────────────────────────────┐
                               │                                     │
                               │            Room Database            │
                               │                                     │
                               └─────────────────────────────────────┘
                                                │
                                                ▼
                                      ┌─────────────────────┐
                                      │                     │
                                      │   User's Device     │
                                      │                     │
                                      └─────────────────────┘
```

## Data Storage Layer

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│                     Application Layer                       │
│                                                             │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│                     Repository Layer                        │
│                                                             │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│                     Data Access Layer                       │
│                                                             │
└─────────────────────────────────────────────────────────────┘
          │                   │                   │
          ▼                   ▼                   ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│                 │  │                 │  │                 │
│    Room DB      │  │ EncryptedShared │  │   File Storage  │
│                 │  │   Preferences   │  │                 │
└─────────────────┘  └─────────────────┘  └─────────────────┘
          │                   │                   │
          ▼                   ▼                   ▼
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│                     User's Device Only                      │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

These diagrams illustrate the key data flows within the Nukie application, emphasizing how all data remains on the user's device while still enabling full functionality of a social media aggregator. The architecture prioritizes privacy and data sovereignty while providing a seamless user experience.
