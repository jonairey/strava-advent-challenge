package com.airey.service;

import com.airey.domain.authorization.Authorization;

public interface AuthenticationService {
    Authorization authenticate(String id);
}
