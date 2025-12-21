package ru.itmo.codetogether.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import ru.itmo.codetogether.dto.document.DocumentSnapshot;
import ru.itmo.codetogether.dto.document.DocumentState;
import ru.itmo.codetogether.dto.document.DocumentStats;
import ru.itmo.codetogether.dto.document.OperationAck;
import ru.itmo.codetogether.dto.document.OperationRequest;
import ru.itmo.codetogether.dto.document.OperationsResponse;
import ru.itmo.codetogether.dto.document.SnapshotRequest;
import ru.itmo.codetogether.model.UserEntity;
import ru.itmo.codetogether.service.DocumentService;
import ru.itmo.codetogether.service.SessionService;

@RestController
@RequestMapping("/sessions/{sessionId}")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final SessionService sessionService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/document")
    public DocumentState document(@AuthenticationPrincipal UserEntity user, @PathVariable Long sessionId) {
        sessionService.ensureMember(sessionId, user.getId());
        return documentService.getDocument(sessionId);
    }

    @GetMapping("/document/operations")
    public OperationsResponse operations(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "0") int sinceVersion) {
        sessionService.ensureMember(sessionId, user.getId());
        return documentService.getOperations(sessionId, Math.max(0, sinceVersion));
    }

    @PostMapping("/document/operations")
    public ResponseEntity<OperationAck> append(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long sessionId,
            @Valid @RequestBody OperationRequest request) {
        sessionService.ensureEditor(sessionId, user.getId());
        OperationAck ack = documentService.appendOperations(sessionId, user.getId(), request);
        messagingTemplate.convertAndSend("/topic/sessions/" + sessionId + "/document", ack);
        return ResponseEntity.status(201).body(ack);
    }

    @GetMapping("/document/snapshots")
    public List<DocumentSnapshot> snapshots(@AuthenticationPrincipal UserEntity user, @PathVariable Long sessionId) {
        sessionService.ensureMember(sessionId, user.getId());
        return documentService.listSnapshots(sessionId);
    }

    @PostMapping("/document/snapshots")
    public ResponseEntity<DocumentSnapshot> createSnapshot(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long sessionId,
            @Valid @RequestBody SnapshotRequest request) {
        sessionService.ensureEditor(sessionId, user.getId());
        DocumentSnapshot snapshot = documentService.saveSnapshot(sessionId, user.getId(), request);
        return ResponseEntity.status(201).body(snapshot);
    }

    @GetMapping("/statistics")
    public DocumentStats stats(@AuthenticationPrincipal UserEntity user, @PathVariable Long sessionId) {
        sessionService.ensureMember(sessionId, user.getId());
        return documentService.documentStats(sessionId, sessionService.memberCount(sessionId));
    }
}
