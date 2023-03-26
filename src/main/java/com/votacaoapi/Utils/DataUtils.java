package com.votacaoapi.Utils;

import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DataUtils {

    public static LocalDateTime convertStringToDateTime(String date) {
        if (ObjectUtils.isEmpty(date)) {
            return LocalDateTime.now();
        }

        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public static String convertLocalDateTimeToString(LocalDateTime date) {
        if (ObjectUtils.isEmpty(date)) {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }

        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
