package com.studentnotes.config;

import com.studentnotes.model.Note;
import com.studentnotes.model.enums.NoteStatus;
import com.studentnotes.repository.NoteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.nio.file.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
public class ProjectSeeder {

    @Bean
    public CommandLineRunner seedNotes(NoteRepository noteRepository) {
        return args -> {
            String pagesPath = "/Users/shankar/Projects/Notes folder/exam-notes/Frontend/src/pages";
            Path startPath = Paths.get(pagesPath);

            if (!Files.exists(startPath)) {
                System.out.println("⚠️ Seed path not found: " + pagesPath);
                return;
            }

            System.out.println("🌱 Starting Note Seeding from local files...");

            Files.walk(startPath)
                    .filter(p -> p.toString().endsWith(".md"))
                    .forEach(path -> {
                        try {
                            String relativePath = startPath.relativize(path).toString();
                            String[] parts = relativePath.split(File.separator);

                            // Structure: dept/year/section/subject/filename.md
                            if (parts.length == 5) {
                                String dept = parts[0];
                                String year = parts[1];
                                String section = parts[2];
                                String subject = parts[3];
                                String filename = parts[4].replace(".md", "");

                                String content = Files.readString(path);
                                String title = extractTitle(content, filename);
                                String pureContent = stripFrontmatter(content);

                                // Check if already exists by title and subject
                                if (noteRepository.findAll().stream()
                                        .noneMatch(n -> n.getTitle().equals(title) && n.getSubject().equals(subject))) {
                                    Note note = new Note();
                                    note.setTitle(title);
                                    note.setContent(pureContent);
                                    note.setDepartment(dept);
                                    note.setYear(year);
                                    note.setSection(section);
                                    note.setSubject(subject);
                                    note.setType("md");
                                    note.setStatus(NoteStatus.PUBLISHED); // Use status enum
                                    note.setUploadedByName("Super Admin");
                                    note.setUploadedByEmail("admin@test.com");
                                    noteRepository.save(note);
                                    System.out.println("✅ Seeded: " + title);
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("❌ Error seeding file: " + path + " - " + e.getMessage());
                        }
                    });

            System.out.println("✨ Seeding Complete!");
        };
    }

    private String extractTitle(String content, String fallback) {
        Pattern pattern = Pattern.compile("title:\\s*(.*)");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim().replace("\"", "").replace("'", "");
        }
        return fallback;
    }

    private String stripFrontmatter(String content) {
        return content.replaceFirst("(?s)^---.*?---", "").trim();
    }
}
