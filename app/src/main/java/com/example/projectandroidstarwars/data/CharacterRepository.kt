package com.example.projectandroidstarwars.data

import com.example.projectandroidstarwars.model.StarWarsCharacter

class CharacterRepository {

    private val characters = listOf(
        StarWarsCharacter(
            id = 1,
            name = "Люк Скайуокер",
            height = "172",
            mass = "77",
            birthYear = "19BBY",
            gender = "Мужской",
            eyeColor = "Голубой",
            description = "Люк вырос на Татуине и стал одним из героев " +
                    "Повстанческого альянса. Его путь связан с изучением " +
                    "Силы, наследием джедаев и противостоянием Империи."
        ),

        StarWarsCharacter(
            id = 2,
            name = "C-3PO",
            height = "167",
            mass = "75",
            birthYear = "112BBY",
            gender = "Не применимо",
            eyeColor = "Жёлтый",
            description = "Протокольный дроид, предназначенный для общения " +
                    "и перевода. C-3PO часто оказывается в центре приключений " +
                    "вместе со своим спутником R2-D2."
        ),

        StarWarsCharacter(
            id = 3,
            name = "R2-D2",
            height = "96",
            mass = "32",
            birthYear = "33BBY",
            gender = "Не применимо",
            eyeColor = "Красный",
            description = "Астромеханический дроид, который помогает " +
                    "ремонтировать корабли и работать с бортовыми системами. " +
                    "Несмотря на небольшой размер, он часто спасает друзей."
        ),

        StarWarsCharacter(
            id = 4,
            name = "Дарт Вейдер",
            height = "202",
            mass = "136",
            birthYear = "41.9BBY",
            gender = "Мужской",
            eyeColor = "Жёлтый",
            description = "Один из главных представителей Галактической " +
                    "Империи. За чёрными доспехами скрывается Энакин " +
                    "Скайуокер, бывший рыцарь-джедай."
        ),

        StarWarsCharacter(
            id = 5,
            name = "Лея Органа",
            height = "150",
            mass = "49",
            birthYear = "19BBY",
            gender = "Женский",
            eyeColor = "Карий",
            description = "Одна из лидеров Повстанческого альянса. " +
                    "Лея сочетает дипломатические способности, решительность " +
                    "и готовность лично участвовать в опасных заданиях."
        ),

        StarWarsCharacter(
            id = 10,
            name = "Оби-Ван Кеноби",
            height = "182",
            mass = "77",
            birthYear = "57BBY",
            gender = "Мужской",
            eyeColor = "Серо-голубой",
            description = "Мастер-джедай и наставник Энакина Скайуокера. " +
                    "Позднее он помогает Люку сделать первые шаги " +
                    "в изучении Силы."
        ),

        StarWarsCharacter(
            id = 13,
            name = "Чубакка",
            height = "228",
            mass = "112",
            birthYear = "200BBY",
            gender = "Мужской",
            eyeColor = "Голубой",
            description = "Вуки и верный друг Хана Соло. Чубакка служит " +
                    "вторым пилотом «Тысячелетнего сокола» и отличается " +
                    "силой, смелостью и преданностью."
        ),

        StarWarsCharacter(
            id = 14,
            name = "Хан Соло",
            height = "180",
            mass = "80",
            birthYear = "29BBY",
            gender = "Мужской",
            eyeColor = "Карий",
            description = "Пилот «Тысячелетнего сокола». Начав как " +
                    "контрабандист, Хан становится важным участником " +
                    "борьбы против Империи."
        ),

        StarWarsCharacter(
            id = 20,
            name = "Йода",
            height = "66",
            mass = "17",
            birthYear = "896BBY",
            gender = "Мужской",
            eyeColor = "Карий",
            description = "Один из наиболее опытных мастеров-джедаев. " +
                    "Йода обучаетвладению Силой и напоминает ученикам " +
                    "о важности терпения и внутреннего равновесия."
        ),

        StarWarsCharacter(
            id = 21,
            name = "Палпатин",
            height = "170",
            mass = "75",
            birthYear = "82BBY",
            gender = "Мужской",
            eyeColor = "Жёлтый",
            description = "Политик, ставший правителем Галактической " +
                    "Империи. За его публичным образом скрывается " +
                    "владыка ситхов Дарт Сидиус."
        ),

        StarWarsCharacter(
            id = 22,
            name = "Боба Фетт",
            height = "183",
            mass = "78.2",
            birthYear = "31.5BBY",
            gender = "Мужской",
            eyeColor = "Карий",
            description = "Охотник за головами в узнаваемых доспехах. " +
                    "Боба Фетт известен своей сдержанностью и умением " +
                    "выслеживать цели."
        ),

        StarWarsCharacter(
            id = 25,
            name = "Лэндо Калриссиан",
            height = "177",
            mass = "79",
            birthYear = "31BBY",
            gender = "Мужской",
            eyeColor = "Карий",
            description = "Предприниматель и пилот, управлявший Облачным " +
                    "городом. Впоследствии Лэндо присоединяется " +
                    "к борьбе Повстанческого альянса."
        )
    )

    fun getCharacters(): List<StarWarsCharacter> {
        return characters
    }

    fun getCharacterById(id: Int): StarWarsCharacter? {
        return characters.find { character ->
            character.id == id
        }
    }
}