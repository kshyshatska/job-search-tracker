package com.example.jobtracker.repository;

import com.example.jobtracker.entity.JobApplication;
import com.example.jobtracker.entity.User;
import com.example.jobtracker.enums.ApplicationStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findByUserOrderByCreatedAtDesc(User user);

    List<JobApplication> findByUser(User user, Sort sort);

    Optional<JobApplication> findByIdAndUser(Long id, User user);

    Optional<JobApplication> findByUserAndSourceUrl(User user, String sourceUrl);

    long countByUser(User user);

    long countByUserAndStatus(User user, ApplicationStatus status);

    List<JobApplication> findTop5ByUserOrderByCreatedAtDesc(User user);

    List<JobApplication> findByUserAndStatusOrderByCreatedAtDesc(User user, ApplicationStatus status);

    List<JobApplication> findByUserAndStatus(User user, ApplicationStatus status, Sort sort);

    @Query("select application.sourceUrl from JobApplication application where application.user = :user and application.sourceUrl is not null")
    List<String> findSourceUrlsByUser(@Param("user") User user);
}
