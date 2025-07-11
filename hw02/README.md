## Текущее задание:
Приложение должно запросить фамилию и имя студента, задать вопросы с вариантами ответа и вывести результат тестирования. Задание выполняется на базе заготовки, содержащей каркас приложения. Необходимо дописать классы и служебные файлы заготовки, ориентируясь на оставленные в ней подсказки так, чтобы выполнялись условия задания. Также, решение должно удовлетворять нижеуказанным требованиям.
## Требования к использованию заготовки:
- То что уже было в заготовке должно остаться неизменным (количество, имена и текущее содержимое классов и файлов, сигнатуры методов и т.д.)
- Можно добавлять код/текст в существующие методы классов и файлы, писать свои приватные методы, а также создавать новые классы
- Вышеуказанные требования не относятся к комментариям, их после реализации нужно удалить
- Можно адаптировать/перенести актуальный код/файлы первого ДЗ, но с учетом того, что некоторые классы в новой заготовке были изменены
## Требования к реализации:
- Учесть актуальные требования прошлой работы
- Контекст описывается с помощью Java + Annotation-based конфигурации
- Имя ресурса с вопросами (csv-файла), а также количество правильных ответов для прохождения теста должны конфигурироваться через файл настроек и использоваться через соответствующие классы заготовки
- Необходимо адаптировать/перенести юнит-тест сервиса тестирования, а также написать интеграционный тест дао, читающего вопросы. Под интеграционностью тут понимается интеграция с файловой системой. В остальном, это должен быть юнит-тест (без контекста и с моками зависимостей)
- Тесты из заготовки должны проходить