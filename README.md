# Kotlin Spring Boot MVC API

이 프로젝트는 **"플레이스 순위"** 서비스의 백엔드 API를 제공하는 Spring Boot MVC 입니다. 현재 학습중이며 지속적인 수정 예정입니다.
Kotlin을 사용하여 개발되었으며, 전통적인 MVC 패턴을 기반으로 RESTful API를 구현합니다.

## 🏗️ 아키텍처 개요

```
┌─────────────────┐
│   Controller    │  ← Presentation Layer (REST API 엔드포인트)
└─────────────────┘
         ↓
┌─────────────────┐
│    Service      │  ← Business Logic Layer (비즈니스 로직)
└─────────────────┘
         ↓
┌─────────────────┐
│   Repository    │  ← Data Access Layer (데이터 접근)
└─────────────────┘
         ↓
┌─────────────────┐
│    Entity       │  ← Data Layer (데이터 모델)
└─────────────────┘
```

## 📦 프로젝트 구조

```
main/
└── kotlin/
    └── ezrank.api/
        ├── controller/
        │   ├── AuthController.kt
        │   └── RankController.kt
        ├── dto/
        │   ├── AuthDto.kt
        │   └── RankDto.kt
        ├── entity/
        │   ├── Keyword.kt
        │   ├── Place.kt
        │   ├── Rank.kt
        │   └── User.kt
        ├── repository/
        │   ├── KeywordRepository.kt
        │   ├── PlaceRepository.kt
        │   ├── RankRepository.kt
        │   └── UserRepository.kt
        ├── scheduler/
        │   └── RankScheduler.kt
        ├── service/
        │   ├── AuthService.kt
        │   └── RankService.kt
        ├── ApiApplication.kt
        ├── config.kt
        └── SecurityConfig.kt
```
