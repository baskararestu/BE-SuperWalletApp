package com.enigma.superwallet.constant;

public enum ECurrencyCode {
    IDR("Indonesian Rupiah"),
    EUR("European"),
    USD("United State Dollar"),
    JPY("Japanese Yen"),
    CNY("Chinese Yuan"),
    SGD("Singapore Dollar"),
    AUD("Australian Dollar"),
    KRW("Korean Won"),
    SAR("Saudi Arabian Riyal"),
    GBP("Great Britain Pound Sterling");

    public final String currencyName;

    ECurrencyCode(String currencyName) {
        this.currencyName = currencyName;
    }
}
