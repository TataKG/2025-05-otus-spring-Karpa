package ru.otus.hw.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "comments")
public class CommentMongo {
    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    private String id;

    @Field(name = "text")
    @EqualsAndHashCode.Include
    @ToString.Include
    private String text;

    @Field(name = "book")
    private String book;
}
