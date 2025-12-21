package ru.itmo.codetogether.service;

import java.time.Instant;

record OAuthState(String provider, Instant expiresAt) {}

