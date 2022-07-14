[![Build status](https://build.appcenter.ms/v0.1/apps/05ef055d-a842-4628-bce1-1608ad4af9c2/branches/main/badge)](https://appcenter.ms)
[![Build Android](https://github.com/aeremin/ostranna-flasks/actions/workflows/build_android.yml/badge.svg)](https://github.com/aeremin/ostranna-flasks/actions/workflows/build_android.yml)
[![Build server](https://github.com/aeremin/ostranna-flasks/actions/workflows/build_server.yml/badge.svg)](https://github.com/aeremin/ostranna-flasks/actions/workflows/build_server.yml)

# Остранна-Колбы

Софт для управления [магическими колбами Остранны](https://ostranna.ru/magic/hogwartsflasks/).

## Установка и использование

### Мобильное приложение

Свежую версию приложения можно скачать [здесь](https://appcenter.ms/orgs/Ostranna/apps/Ostranna-Flasks).
В настоящий момент доступ только по приглашению. Для установки необходимо разрешить установку из неизвестных источников.  
Приложение не требует логина, любой пользователь может начислять баллы от лица любого преподавателя.

### Серверное приложение

Для запуска нужна установленная Java (JRE). На многих машинах она уже установлена.

Чтоб скачать свежую версию приложения - идем 
[сюда](https://github.com/aeremin/ostranna-flasks/actions/workflows/build_server.yml),
выбираем последний успешный (с зеленой галочкой) билд, жмем на ссылку, в разделе Artifacts скачиваем server.
Полученный архив нужно распаковать куда-то не очень глубоко на диске (могут быть проблемы с длинными путями).

Для авторизации в облачной БД так же нужен ключ-файл. Идем [сюда](https://console.firebase.google.com/u/0/project/ostranna-flasks/settings/serviceaccounts/adminsdk),
жмем на "Generate new private key", сохраняем файл под именем `ostranna-flasks-account-key.json` в подпапку
`bin` (рядом с файлами `server.bat` и `server`).

Далее нужно запустить `server.bat` (под линуксом - `server`) из консоли:

```
server.bat COM4
```

Единственный (и обязательный) аргумент - COM-порт, по которому будут передаваться команды. Доступны следующие варианты:
* Явное указание порта: `COMХ` на Windows-системах, `/dev/ttyX` - на Linux.
* Автоматическая детекция (работает в случае если подключено ровно одно устройство) - значение `auto`.
* Использование заглушки вместо реального общения по COM-порту - значение `stub`.

В случае неправильного использования - см. вывод, скорее всего там будет информация о проблеме.

После запуска по адресу http://localhost:8080/ будет доступна страница с историей всех начислений.

### Облачная база данных

[Здесь](https://console.firebase.google.com/u/0/project/ostranna-flasks/database/ostranna-flasks-default-rtdb/data) можно
посмотреть (и при необходимости - подредактировать, но аккуратно) состояние базы данных.

Разделы:
* [professors](https://console.firebase.google.com/u/0/project/ostranna-flasks/database/ostranna-flasks-default-rtdb/data/~2Fprofessors):
  тут можно добавлять новых преподавателей/других персонажей с правом начислять баллы и указывать их дневной лимит. 
  Записи должны иметь поля `name` и `limit`. После редактирования - нужно проверить в мобильном приложении, что список
  синхронизировался. Если формат данных в БД неправильный - синхронизироваться не будет.
* [actions](https://console.firebase.google.com/u/0/project/ostranna-flasks/database/ostranna-flasks-default-rtdb/data/~2Factions):
  все операции снятия/начисления баллов. Новые записи тут создавать не стоит, подправить что-то в старых потенциально
  можно.

