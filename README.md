# bash_RUS

Реализация bash на Java/Kotlin.

## Архитектура
<img width="826" height="412" alt="Bash architecture" src="https://github.com/user-attachments/assets/89387a7a-c8d8-4c6c-a4b2-e8c0f99c7640" />

## Сборка

- **Локально:** установите [JDK 17+](https://adoptium.net/) и [Gradle](https://gradle.org/), затем:
  ```bash
  gradle build
  ```
  Для работы без установленного Gradle один раз выполните `gradle wrapper`, далее используйте `./gradlew build`.

- **CI:** при пуше в `main`/`master` GitHub Actions собирает проект и запускает тесты на JDK 17 и 21.

## Структура

- `src/main/kotlin/ru/bash/` — исходный код
- `src/test/kotlin/ru/bash/` — тесты

## Интеграция с AI

### Архитектура


### Настройка CI
