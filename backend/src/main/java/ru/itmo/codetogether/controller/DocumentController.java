package ru.itmo.codetogether.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.codetogether.dto.DocumentDto;
import ru.itmo.codetogether.model.UserEntity;
import ru.itmo.codetogether.service.DocumentService;
import ru.itmo.codetogether.service.SessionService;

@RestController
@RequestMapping("/sessions/{sessionId}")
public class DocumentController {

    private final DocumentService documentService;
    private final SessionService sessionService;

    public DocumentController(DocumentService documentService, SessionService sessionService) {
        this.documentService = documentService;
        this.sessionService = sessionService;
    }

    @GetMapping("/document")
    public DocumentDto.DocumentState document(
            @AuthenticationPrincipal UserEntity user, @PathVariable Long sessionId) {
        sessionService.ensureMember(sessionId, user.getId());
        return documentService.getDocument(sessionId);
    }

    @GetMapping("/document/operations")
    public DocumentDto.OperationsResponse operations(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "0") int sinceVersion) {
        sessionService.ensureMember(sessionId, user.getId());
        return documentService.getOperations(sessionId, Math.max(0, sinceVersion));
    }

    @PostMapping("/document/operations")
    public ResponseEntity<DocumentDto.OperationAck> append(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long sessionId,
            @Valid @RequestBody DocumentDto.OperationRequest request) {
        sessionService.ensureEditor(sessionId, user.getId());
        DocumentDto.OperationAck ack = documentService.appendOperations(sessionId, user.getId(), request);
        return ResponseEntity.status(201).body(ack);
    }

    @GetMapping("/document/snapshots")
    public List<DocumentDto.DocumentSnapshot> snapshots(
            @AuthenticationPrincipal UserEntity user, @PathVariable Long sessionId) {
        sessionService.ensureMember(sessionId, user.getId());
        return documentService.listSnapshots(sessionId);
    }

    @PostMapping("/document/snapshots")
    public ResponseEntity<DocumentDto.DocumentSnapshot> createSnapshot(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long sessionId,
            @Valid @RequestBody DocumentDto.SnapshotRequest request) {
        sessionService.ensureEditor(sessionId, user.getId());
        DocumentDto.DocumentSnapshot snapshot = documentService.saveSnapshot(sessionId, user.getId(), request);
        return ResponseEntity.status(201).body(snapshot);
    }

    @GetMapping("/statistics")
    public DocumentDto.DocumentStats stats(
            @AuthenticationPrincipal UserEntity user, @PathVariable Long sessionId) {
        sessionService.ensureMember(sessionId, user.getId());
        return documentService.documentStats(sessionId, sessionService.memberCount(sessionId));
    }
}
