# bash_RUS

Реализация bash на Java/Kotlin.

## Архитектура
<img width="826" height="412" alt="Bash architecture" src="https://github.com/user-attachments/assets/89387a7a-c8d8-4c6c-a4b2-e8c0f99c7640" />

## Сборка

- **Локально:** нужен [JDK 17+](https://adoptium.net/). Сборка через wrapper (Gradle ставить не нужно):
  ```bash
  ./gradlew build
  ```

## Структура

- `src/main/kotlin/ru/bash/` — исходный код
- `src/test/kotlin/ru/bash/` — тесты

## Интеграция с AI

### Архитектура

https://chatgpt.com/share/69b43156-b8e4-8007-bf48-1d0e4c58e42a

ИИ выдал базовую архитектуру bash, которая была доработана, добавлены блоки Expansion, Command register, в который включена работа с пайпланами, типы команд поделены на различные блоки, добавлены выходные данные из каждого блока.

### Настройка CI

**Платформа:** GitHub Actions. Запуск при push/PR в ветки `main` и `master`.

**Пайплайн (по шагам):**
1. **build** — компиляция main и test (Kotlin).
2. **lint** — статический анализ кода (Detekt).
3. **tests** — прогон тестов (JUnit 5 + Kotest).
4. **coverage** — сбор покрытия (JaCoCo), отчёт загружается артефактом.

**Используется:** JDK 17, Gradle (через `gradle/actions/setup-gradle`), Detekt, JUnit 5, Kotest, JaCoCo. Конфиг CI: `.github/workflows/ci.yml`.

ИИ использовался для подбора наиболее подходящей конфигурации используемых инструметов в CI.
