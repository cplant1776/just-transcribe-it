package com.jti.JustTranscribeIt.model;


import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Table(name = "userusage")
public class UserUsage {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "transcript_length")
    private Integer transcriptLength;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "transcript_id")
    private Integer transcriptId;

    public UserUsage() {

    }

    public UserUsage(Integer userId, Integer transcriptLength, Integer transcriptId) {
        this.userId = userId;
        this.transcriptLength = transcriptLength;
        this.transcriptId = transcriptId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getTranscriptLength() {
        return transcriptLength;
    }

    public void setTranscriptLength(Integer transcriptLength) {
        this.transcriptLength = transcriptLength;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getTranscriptId() {
        return transcriptId;
    }

    public void setTranscriptId(Integer transcriptId) {
        this.transcriptId = transcriptId;
    }
}
