insert into authors(full_name)
values ('Author_1'),
       ('Author_2'),
       ('Author_3');

insert into genres(name)
values ('Genre_1'),
       ('Genre_2'),
       ('Genre_3'),
       ('Genre_4'),
       ('Genre_5'),
       ('Genre_6');

insert into books(title, author_id)
values ('BookTitle_1', 1),
       ('BookTitle_2', 2),
       ('BookTitle_3', 3);

insert into books_genres(book_id, genre_id)
values (1, 1),
       (1, 2),
       (2, 3),
       (2, 4),
       (3, 5),
       (3, 6);

insert into comments(book_id, text)
values (1, 'comment11'),
       (1, 'comment12'),
       (2, 'comment21'),
       (3, 'comment31'),
       (3, 'comment32'),
       (3, 'comment33');

insert into users(username, password)
values ('admin', '$2a$12$t3r/4wTmYU4/SnUcCHGZaOSBmUhTc3mJFoT3g6oEXdecA5aGPsxV.'),-- ''admin'),
       ('user', '$2a$12$ompACDrhhPifepnzeFc9Ges4NOTYs2YLE607.aVi3SiTYl7tslQx2');--'user');

insert into USER_ROLES(USER_ID, ROLE)
values (1, 'ADMIN'),
       (1, 'USER'),
       (2, 'USER');

---- ACL
-- CREATE SEQUENCE IF NOT EXISTS hibernate_sequence START WITH 1 INCREMENT BY 1;

-- CREATE ALIAS IF NOT EXISTS identity FOR "java.lang.System.identityHashCode";

--позволяет идентифицировать security identity (роль или пользователь)
--Определяем по кому мы будем раскидывать полномочия: по роли или по пользователю
insert into ACL_SID (PRINCIPAL, SID)
values (0, 'ROLE_ADMIN'),
       (0, 'ROLE_USER');

--идентифицирует тип сущности
--обозначаем класс, для которого будем разграничивать полномочия
insert into ACL_CLASS (CLASS)
values ('ru.otus.hw.models.Book'),
       ('ru.otus.hw.models.Comment');

--содержит информацию о всех сущностях системы
--конкретный объект класса c установкой владельца и наследования
INSERT INTO acl_object_identity (object_id_class, object_id_identity, parent_object, owner_sid, entries_inheriting)
VALUES (1, 1, NULL, 1, 0),
       (1, 2, NULL, 1, 0),
       (1, 3, NULL, 1, 0),

       (2,1,NULL, 1,0),
       (2,2,NULL, 1,0),
       (2,3,NULL, 1,0),
       (2,4,NULL, 1,0),
       (2,5,NULL, 1,0),
       (2,6,NULL, 1,0);

--содержит права, назначенные для security identity на domain object
--сами полномочия для каждого объекта из acl_object_identity
--Значения MASK:
---- Разрешение	        Значение (десятичное)	Значение (бинарное)
---- READ	            1	                    00000001
---- WRITE	            2	                    00000010
---- CREATE	            4	                    00000100
---- DELETE	            8	                    00001000
---- ADMINISTRATION     16	                    00010000
insert into acl_entry (acl_object_identity, ace_order, sid, mask,
                       granting, audit_success, audit_failure)
VALUES
-- book1
(1, 1, 1, 1, 1, 0, 0),
(1, 2, 1, 2, 1, 0, 0),
(1, 3, 1, 8, 1, 0, 0),
(1, 4, 2, 1, 1, 0, 0),
-- book2
(2, 1, 1, 1, 1, 0, 0),
(2, 2, 1, 2, 1, 0, 0),
(2, 3, 1, 8, 1, 0, 0),
(2, 4, 2, 1, 1, 0, 0),
-- book3
(3, 1, 1, 1, 1, 0, 0),
(3, 2, 1, 2, 1, 0, 0),
(3, 3, 1, 8, 1, 0, 0),
(3, 4, 2, 1, 1, 0, 0),
-- comment1
(4, 1, 1, 1, 1, 0, 0),
(4, 2, 1, 2, 1, 0, 0),
(4, 3, 1, 8, 1, 0, 0),
(4, 4, 2, 1, 1, 0, 0),
-- comment2
(5, 1, 1, 1, 1, 0, 0),
(5, 2, 1, 2, 1, 0, 0),
(5, 3, 1, 8, 1, 0, 0),
(5, 4, 2, 1, 1, 0, 0),
-- comment3
(6, 1, 1, 1, 1, 0, 0),
(6, 2, 1, 2, 1, 0, 0),
(6, 3, 1, 8, 1, 0, 0),
(6, 4, 2, 1, 1, 0, 0),
-- comment4
(7, 1, 1, 1, 1, 0, 0),
(7, 2, 1, 2, 1, 0, 0),
(7, 3, 1, 8, 1, 0, 0),
(7, 4, 2, 1, 1, 0, 0),
-- comment5
(8, 1, 1, 1, 1, 0, 0),
(8, 2, 1, 2, 1, 0, 0),
(8, 3, 1, 8, 1, 0, 0),
(8, 4, 2, 1, 1, 0, 0),
-- comment6
(9, 1, 1, 1, 1, 0, 0),
(9, 2, 1, 2, 1, 0, 0),
(9, 3, 1, 8, 1, 0, 0),
(9, 4, 2, 1, 1, 0, 0)

;