package ru.itmo.codetogether.service;

import java.time.Instant;

record TokenRecord(Long userId, Instant expiresAt) {}
