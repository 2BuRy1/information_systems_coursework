package ru.itmo.codetogether.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "document_operation")
public class DocumentOperationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private DocumentEntity document;

    @Column(name = "operation_type", nullable = false)
    private String operationType;

    @Column(name = "node_counter", nullable = false)
    private Integer nodeCounter;

    @Column(name = "node_site", nullable = false)
    private Integer nodeSite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "left_node")
    private DocumentOperationEntity leftNode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "right_node")
    private DocumentOperationEntity rightNode;

    private String color;

    @Column(nullable = false)
    private String value;

    @Column(nullable = false)
    private Integer version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public DocumentEntity getDocument() {
        return document;
    }

    public void setDocument(DocumentEntity document) {
        this.document = document;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public Integer getNodeCounter() {
        return nodeCounter;
    }

    public void setNodeCounter(Integer nodeCounter) {
        this.nodeCounter = nodeCounter;
    }

    public Integer getNodeSite() {
        return nodeSite;
    }

    public void setNodeSite(Integer nodeSite) {
        this.nodeSite = nodeSite;
    }

    public DocumentOperationEntity getLeftNode() {
        return leftNode;
    }

    public void setLeftNode(DocumentOperationEntity leftNode) {
        this.leftNode = leftNode;
    }

    public DocumentOperationEntity getRightNode() {
        return rightNode;
    }

    public void setRightNode(DocumentOperationEntity rightNode) {
        this.rightNode = rightNode;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
