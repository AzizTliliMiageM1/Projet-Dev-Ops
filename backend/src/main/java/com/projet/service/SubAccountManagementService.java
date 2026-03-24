package com.projet.service;

import java.util.List;
import java.util.Map;

import com.projet.backend.domain.SubAccount;
import com.projet.backend.domain.SubAccountPayment;
import com.projet.backend.domain.SubAccountSubscription;

public interface SubAccountManagementService {
    SubAccount createSubAccount(String parentEmail, Map<String, Object> request);

    List<SubAccount> listSubAccounts(String parentEmail);

    SubAccount getSubAccount(String parentEmail, String subAccountId);

    List<SubAccountSubscription> listSubscriptions(String parentEmail, String subAccountId);

    SubAccount disableSubAccount(String parentEmail, String subAccountId);

    SubAccount deleteSubAccount(String parentEmail, String subAccountId);

    Map<String, Object> addSubscription(String parentEmail, Map<String, Object> request);

    List<SubAccountPayment> listPayments(String parentEmail);

    SubAccount updatePreferences(String parentEmail, String subAccountId, Map<String, String> preferences);
}
