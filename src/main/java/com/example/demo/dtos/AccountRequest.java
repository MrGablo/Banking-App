package com.example.demo.dtos;

import com.example.demo.common.enums.AccountType;
import com.example.demo.common.enums.Currency;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountRequest {

    private AccountType type;
    private Currency currency;

}
