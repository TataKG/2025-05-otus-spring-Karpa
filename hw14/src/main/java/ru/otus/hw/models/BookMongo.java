package ru.otus.hw.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "books")
public class BookMongo {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    private String id;

    @Field(name = "title")
    @EqualsAndHashCode.Include
    @ToString.Include
    private String title;

    @Field(name = "author")
    private String author;

    @Field(name = "genre")
    private String genre;
}
