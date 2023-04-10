package com.example.moneyfypro.ui.setting

import java.util.Currency

sealed class CurrencySelection(val currencyCode: String) {
    class CurrencySelectionConfirmed(currencyCode: String): CurrencySelection(currencyCode)
    class CurrencySelectionInProgress(currencyCode: String): CurrencySelection(currencyCode)
}

