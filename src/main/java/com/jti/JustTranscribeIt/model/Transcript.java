package com.jti.JustTranscribeIt.model;

import com.jti.JustTranscribeIt.utilities.Status;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "transcript")
public class Transcript {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "file_id")
    private Integer fileId;

    @Column(name = "transcript")
    private String transcript;

    @Column(name = "job_name")
    private String jobName;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "user_given_name")
    private String userGivenName;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_time")
    private Date createTime;

    public Transcript(Integer fileId, String transcript, String jobName, Integer userId, String userGivenName) {
        this.fileId = fileId;
        this.transcript = transcript;
        this.jobName = jobName;
        this.userId = userId;
        this.userGivenName = userGivenName;
    }

    public Transcript(Integer userId, String userGivenName, Integer audioFileId) {
        this.userId = userId;
        this.userGivenName = userGivenName;
        this.fileId = audioFileId;
        this.status = Status.PENDING;
    }

    public Transcript() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFileId() {
        return fileId;
    }

    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserGivenName() {
        return userGivenName;
    }

    public void setUserGivenName(String userGivenName) {
        this.userGivenName = userGivenName;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
