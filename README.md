# Note-archive

Цель проекта создать WEB-сайт для сохранения учебых материалов, конспектов, кода и прочего с возможностью последующего редактирования и группового доступа к заметке. 

## Текущие возможности

На данный момент реализовано:

- ✅ Хранение заметок локально на сервере, где запущено приложение.
- ✅ Создание, редактирование и удаление заметок
- ✅ Генерация markdown-описания для каждой заметки с использованием Gemini API 
- ✅ Простой поиск по названию
- ✅ Аутентификация и авторизация пользователей

## В планах

- [ ] Дополнительные возможности поиска и поддержка тегов
- [ ] Oauth авторизация или подтверждение Email
- [ ] Возможность передать права авторства и поделиться правами на редактирование

## Используемые технологии

- **Java 17+**
- **Spring Boot** — каркас приложения
- **Spring Security** — аутентификация и авторизация пользователей
- **Spring Data JPA** — работа с базой данных через ORM (Hibernate)
- **H2** — реляционная база данных
- **Lombok** — для сокращения шаблонного кода
