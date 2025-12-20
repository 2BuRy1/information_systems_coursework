package ru.itmo.codetogether.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.codetogether.dto.CrdtDto;
import ru.itmo.codetogether.dto.DocumentDto;
import ru.itmo.codetogether.exception.CodeTogetherException;
import ru.itmo.codetogether.model.DocumentEntity;
import ru.itmo.codetogether.model.DocumentOperationEntity;
import ru.itmo.codetogether.model.DocumentSnapshotEntity;
import ru.itmo.codetogether.model.UserEntity;
import ru.itmo.codetogether.repository.DocumentOperationRepository;
import ru.itmo.codetogether.repository.DocumentRepository;
import ru.itmo.codetogether.repository.DocumentSnapshotRepository;
import ru.itmo.codetogether.repository.UserRepository;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentOperationRepository operationRepository;
    private final DocumentSnapshotRepository snapshotRepository;
    private final UserRepository userRepository;

    private final ConcurrentMap<Long, CrdtSequence> sequences = new ConcurrentHashMap<>();

    public DocumentService(
            DocumentRepository documentRepository,
            DocumentOperationRepository operationRepository,
            DocumentSnapshotRepository snapshotRepository,
            UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.operationRepository = operationRepository;
        this.snapshotRepository = snapshotRepository;
        this.userRepository = userRepository;
    }

    public void initializeSequence(DocumentEntity document) {
        sequences.put(document.getId(), loadSequence(document));
    }

    @Transactional(readOnly = true)
    public DocumentDto.DocumentState getDocument(Long sessionId) {
        DocumentEntity document = documentRepository
                .findBySession_Id(sessionId)
                .orElseThrow(() -> new CodeTogetherException(HttpStatus.NOT_FOUND, "Документ не найден"));
        if (document.getSession() != null) {
            document.getSession().getId();
            document.getSession().getUpdatedAt();
        }
        return new DocumentDto.DocumentState(
                document.getId(), document.getSession().getId(), document.getVersion(), document.getContentText(), document.getSession().getUpdatedAt());
    }

    public DocumentDto.OperationsResponse getOperations(Long sessionId, Integer sinceVersion) {
        DocumentEntity document = documentRepository
                .findBySession_Id(sessionId)
                .orElseThrow(() -> new CodeTogetherException(HttpStatus.NOT_FOUND, "Документ не найден"));
        if (document.getSession() != null) {
            document.getSession().getId();
        }
        List<DocumentOperationEntity> operations = operationRepository.findByDocumentIdOrderByVersionAsc(document.getId());
        List<CrdtDto.Operation> ops = operations.stream()
                .filter(op -> op.getVersion() > sinceVersion)
                .map(this::toOperation)
                .toList();
        int toVersion = ops.isEmpty() ? sinceVersion : ops.get(ops.size() - 1).version();
        return new DocumentDto.OperationsResponse(sinceVersion, toVersion, ops);
    }

    @Transactional
    public DocumentDto.OperationAck appendOperations(Long sessionId, Long userId, DocumentDto.OperationRequest request) {
        DocumentEntity document = documentRepository
                .findBySession_Id(sessionId)
                .orElseThrow(() -> new CodeTogetherException(HttpStatus.NOT_FOUND, "Документ не найден"));
        if (!Objects.equals(document.getVersion(), request.baseVersion())) {
            throw new CodeTogetherException(HttpStatus.CONFLICT, "Версия документа устарела");
        }
        UserEntity user = userRepository
                .findById(userId)
                .orElseThrow(() -> new CodeTogetherException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        CrdtSequence sequence = sequences.computeIfAbsent(document.getId(), id -> loadSequence(document));
        List<CrdtDto.Operation> applied = new ArrayList<>();
        for (CrdtDto.OperationInput input : request.operations()) {
            DocumentOperationEntity entity = new DocumentOperationEntity();
            entity.setDocument(document);
            entity.setOperationType(input.operationType());
            entity.setNodeCounter(input.nodeCounter());
            entity.setNodeSite(input.nodeSite());
            if (input.leftNode() != null) {
                operationRepository.findById(input.leftNode()).ifPresent(entity::setLeftNode);
            }
            if (input.rightNode() != null) {
                operationRepository.findById(input.rightNode()).ifPresent(entity::setRightNode);
            }
            entity.setColor(input.color());
            entity.setValue(input.value() != null ? input.value() : "");
            document.incrementVersion();
            entity.setVersion(document.getVersion());
            entity.setUser(user);
            DocumentOperationEntity saved = operationRepository.save(entity);
            sequence.apply(saved);
            applied.add(toOperation(saved));
        }
        document.setContentText(sequence.currentText());
        documentRepository.save(document);
        return new DocumentDto.OperationAck(document.getVersion(), applied);
    }

    public List<DocumentDto.DocumentSnapshot> listSnapshots(Long sessionId) {
        DocumentEntity document = documentRepository
                .findBySession_Id(sessionId)
                .orElseThrow(() -> new CodeTogetherException(HttpStatus.NOT_FOUND, "Документ не найден"));
        return snapshotRepository.findByDocumentIdOrderByVersionDesc(document.getId()).stream()
                .map(snapshot -> new DocumentDto.DocumentSnapshot(
                        snapshot.getId(), snapshot.getDocument().getId(), snapshot.getVersion(), snapshot.getUser().getId(), snapshot.getCreatedAt(), snapshot.getContentText()))
                .toList();
    }

    @Transactional
    public DocumentDto.DocumentSnapshot saveSnapshot(Long sessionId, Long userId, DocumentDto.SnapshotRequest request) {
        DocumentEntity document = documentRepository
                .findBySession_Id(sessionId)
                .orElseThrow(() -> new CodeTogetherException(HttpStatus.NOT_FOUND, "Документ не найден"));
        if (!Objects.equals(document.getVersion(), request.version())) {
            throw new CodeTogetherException(HttpStatus.CONFLICT, "Невозможно сохранить устаревший снапшот");
        }
        UserEntity user = userRepository
                .findById(userId)
                .orElseThrow(() -> new CodeTogetherException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        DocumentSnapshotEntity snapshot = new DocumentSnapshotEntity();
        snapshot.setDocument(document);
        snapshot.setVersion(document.getVersion());
        snapshot.setContentText(request.content());
        snapshot.setUser(user);
        DocumentSnapshotEntity saved = snapshotRepository.save(snapshot);
        document.setContentText(request.content());
        documentRepository.save(document);
        return new DocumentDto.DocumentSnapshot(
                saved.getId(), document.getId(), saved.getVersion(), user.getId(), saved.getCreatedAt(), saved.getContentText());
    }

    public DocumentDto.DocumentStats documentStats(Long sessionId, int activeParticipants) {
        DocumentEntity document = documentRepository
                .findBySession_Id(sessionId)
                .orElseThrow(() -> new CodeTogetherException(HttpStatus.NOT_FOUND, "Документ не найден"));
        int operations = operationRepository.findByDocumentIdOrderByVersionAsc(document.getId()).size();
        int lastSnapshotVersion = snapshotRepository.findByDocumentIdOrderByVersionDesc(document.getId()).stream()
                .map(DocumentSnapshotEntity::getVersion)
                .findFirst()
                .orElse(0);
        return new DocumentDto.DocumentStats(
                sessionId,
                document.getId(),
                activeParticipants,
                operations,
                lastSnapshotVersion,
                null,
                null);
    }

    private CrdtSequence loadSequence(DocumentEntity document) {
        List<DocumentOperationEntity> operations = operationRepository.findByDocumentIdOrderByVersionAsc(document.getId());
        CrdtSequence sequence = new CrdtSequence();
        operations.forEach(sequence::apply);
        document.setContentText(sequence.currentText());
        documentRepository.save(document);
        return sequence;
    }

    private CrdtDto.Operation toOperation(DocumentOperationEntity entity) {
        return new CrdtDto.Operation(
                entity.getId(),
                entity.getDocument().getId(),
                entity.getOperationType(),
                entity.getNodeCounter(),
                entity.getNodeSite(),
                entity.getLeftNode() != null ? entity.getLeftNode().getId() : null,
                entity.getRightNode() != null ? entity.getRightNode().getId() : null,
                entity.getValue(),
                entity.getColor(),
                entity.getVersion(),
                entity.getUser().getId(),
                entity.getCreatedAt());
    }

    private static final class CrdtSequence {

        private final List<InsertNode> nodes = new ArrayList<>();

        synchronized void apply(DocumentOperationEntity operation) {
            switch (operation.getOperationType()) {
                case "insert" -> insert(operation);
                case "delete" -> delete(operation);
                default -> {
                }
            }
        }

        private void insert(DocumentOperationEntity operation) {
            int index = insertionIndex(operation);
            InsertNode node = new InsertNode(operation);
            nodes.add(index, node);
        }

        private void delete(DocumentOperationEntity operation) {
            int targetIndex = insertionIndex(operation);
            int offset = characterOffsetBefore(targetIndex);
            int toRemove = Math.max(1, operation.getValue() == null ? 1 : operation.getValue().length());
            int remaining = toRemove;
            int position = 0;
            for (InsertNode node : nodes) {
                if (node.tombstone) {
                    continue;
                }
                int nodeLen = node.value.length();
                int nextPosition = position + nodeLen;
                if (nextPosition <= offset) {
                    position = nextPosition;
                    continue;
                }
                int start = Math.max(0, offset - position);
                int end = Math.min(nodeLen, start + remaining);
                node.value.delete(start, end);
                if (node.value.length() == 0) {
                    node.tombstone = true;
                }
                remaining -= (end - start);
                position = nextPosition - (nodeLen - node.value.length());
                offset = position;
                if (remaining <= 0) {
                    break;
                }
            }
        }

        private int insertionIndex(DocumentOperationEntity operation) {
            Long leftId = operation.getLeftNode() != null ? operation.getLeftNode().getId() : null;
            Long rightId = operation.getRightNode() != null ? operation.getRightNode().getId() : null;
            int insertPos = leftId != null ? indexOf(leftId) + 1 : 0;
            int rightPos = rightId != null ? indexOf(rightId) : nodes.size();
            if (rightPos >= 0) {
                insertPos = Math.min(insertPos, rightPos);
            } else {
                rightPos = nodes.size();
            }
            while (insertPos < rightPos) {
                InsertNode current = nodes.get(insertPos);
                if (sameAnchors(current.operation, leftId, rightId)
                        && compare(operation, current.operation) > 0) {
                    insertPos++;
                } else {
                    break;
                }
            }
            return insertPos;
        }

        private int indexOf(Long operationId) {
            if (operationId == null) {
                return -1;
            }
            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i).operation.getId().equals(operationId)) {
                    return i;
                }
            }
            return -1;
        }

        private boolean sameAnchors(DocumentOperationEntity candidate, Long leftId, Long rightId) {
            Long candidateLeft = candidate.getLeftNode() != null ? candidate.getLeftNode().getId() : null;
            Long candidateRight = candidate.getRightNode() != null ? candidate.getRightNode().getId() : null;
            return Objects.equals(candidateLeft, leftId) && Objects.equals(candidateRight, rightId);
        }

        private int compare(DocumentOperationEntity first, DocumentOperationEntity second) {
            int cmp = Integer.compare(first.getNodeCounter(), second.getNodeCounter());
            if (cmp != 0) {
                return cmp;
            }
            return Integer.compare(first.getNodeSite(), second.getNodeSite());
        }

        private int characterOffsetBefore(int nodeIndex) {
            int offset = 0;
            for (int i = 0; i < nodeIndex && i < nodes.size(); i++) {
                InsertNode node = nodes.get(i);
                if (!node.tombstone) {
                    offset += node.value.length();
                }
            }
            return offset;
        }

        synchronized String currentText() {
            StringBuilder builder = new StringBuilder();
            for (InsertNode node : nodes) {
                if (!node.tombstone) {
                    builder.append(node.value);
                }
            }
            return builder.toString();
        }

        private static final class InsertNode {
            private final DocumentOperationEntity operation;
            private final StringBuilder value;
            private boolean tombstone;

            private InsertNode(DocumentOperationEntity operation) {
                this.operation = operation;
                this.value = new StringBuilder(operation.getValue());
            }
        }
    }
}
