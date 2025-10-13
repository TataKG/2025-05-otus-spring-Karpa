package ru.otus.hw.models;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "comments")
public class Comment {
    @Id
    @ToString.Include
    @EqualsAndHashCode.Include
    private String id;

    @ToString.Include
    @EqualsAndHashCode.Include
    private String text;

    @ToString.Include
    @EqualsAndHashCode.Include
    @Indexed(unique = false)
    private String bookId;

    public Comment(String text, String bookId) {
        this.text = text;
        this.bookId = bookId;
    }
}
