package com.jti.JustTranscribeIt.model;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "generatedurl")
public class GeneratedUrl {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "transcript_id")
    private Integer transcriptId;

    @Column(name = "generated_url")
    private String generatedUrl;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_time")
    private Date createTime;

    public GeneratedUrl(Integer transcriptId, String generatedUrl) {
        this.transcriptId = transcriptId;
        this.generatedUrl = generatedUrl;
    }

    public GeneratedUrl() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTranscriptId() {
        return transcriptId;
    }

    public void setTranscriptId(Integer transcriptId) {
        this.transcriptId = transcriptId;
    }

    public String getGeneratedUrl() {
        return generatedUrl;
    }

    public void setGeneratedUrl(String generatedUrl) {
        this.generatedUrl = generatedUrl;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
