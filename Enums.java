package com.example.bombgame;

public class Enums {
    // Коды, маркирующие начало пакета
    enum MESSAGES_CODES
    {
        // Коды "Андроид -> Ардуино"
        // Код по умолчанию, не используется
        MSG_NOMSG,
        // Хендшейк
        MSG_HANDSHAKE,
        // Начало игры
        MSG_START,
        // Нажатие на цифру
        MSG_INPUT,

        // Коды "Ардуино -> Андроид"
        // Поражение - кончилось время
        MSG_LOSE,
        // Победа - код разгадан до конца таймера
        MSG_WIN,

        // Верхняя граница возможных кодов
        MSG_MAX
    };
}
