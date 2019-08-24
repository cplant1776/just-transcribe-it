package com.jti.JustTranscribeIt.model;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "transcriptexplicit")
public class TranscriptExplicit {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "transcript_explicit")
    private String transcriptExplicit;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_time")
    private Date createTime;

    public TranscriptExplicit(Integer transcriptId, String transcriptText) {
        this.id = transcriptId;
        this.transcriptExplicit = transcriptText;
    }

    public TranscriptExplicit() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTranscriptExplicit() {
        return transcriptExplicit;
    }

    public void setTranscriptExplicit(String transcriptExplicit) {
        this.transcriptExplicit = transcriptExplicit;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
