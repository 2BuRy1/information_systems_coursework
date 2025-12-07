package ru.itmo.codetogether.controller;

import java.security.Principal;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import ru.itmo.codetogether.dto.DocumentDto;
import ru.itmo.codetogether.model.UserEntity;
import ru.itmo.codetogether.service.DocumentService;
import ru.itmo.codetogether.service.SessionService;

@Controller
public class DocumentSocketController {

    private final DocumentService documentService;
    private final SessionService sessionService;
    private final SimpMessagingTemplate messagingTemplate;

    public DocumentSocketController(
            DocumentService documentService, SessionService sessionService, SimpMessagingTemplate messagingTemplate) {
        this.documentService = documentService;
        this.sessionService = sessionService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/sessions/{sessionId}/document/operations")
    public void handleOperations(Principal principal, @DestinationVariable Long sessionId, DocumentDto.OperationRequest request) {
        UserEntity user = extractUser(principal);
        sessionService.ensureEditor(sessionId, user.getId());
        DocumentDto.OperationAck ack = documentService.appendOperations(sessionId, user.getId(), request);
        messagingTemplate.convertAndSend("/topic/sessions/" + sessionId + "/document", ack);
    }

    private UserEntity extractUser(Principal principal) {
        if (principal instanceof Authentication authentication && authentication.getPrincipal() instanceof UserEntity user) {
            return user;
        }
        throw new IllegalStateException("Не удалось определить пользователя WebSocket");
    }
}
