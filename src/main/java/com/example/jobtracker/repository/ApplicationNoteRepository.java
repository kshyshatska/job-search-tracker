package com.example.jobtracker.repository;

import com.example.jobtracker.entity.ApplicationNote;
import com.example.jobtracker.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationNoteRepository extends JpaRepository<ApplicationNote, Long> {

    Optional<ApplicationNote> findByIdAndApplication(Long id, JobApplication application);
}
