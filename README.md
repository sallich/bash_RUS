# bash_RUS

Реализация bash на Java/Kotlin.

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
