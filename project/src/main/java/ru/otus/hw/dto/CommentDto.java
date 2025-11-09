package ru.otus.hw.dto;

import ru.otus.hw.models.Comment;

import java.time.LocalDateTime;

public record CommentDto(
        Long id,
        String content,
        UserDto user,
        Long recipeId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean canEdit,
        boolean canDelete
) {
//    public static CommentDto fromEntity(Comment comment, Long currentUserId) {
//        boolean isOwner = currentUserId != null &&
//                comment.getUser() != null &&
//                comment.getUser().getId()== currentUserId;
//
//        UserDto userDto = UserDto.fromEntity(comment.getUser());
//
//        return new CommentDto(
//                comment.getId(),
//                comment.getContent(),
//                userDto,
//                comment.getRecipe().getId(),
//                comment.getCreatedAt(),
//                comment.getUpdatedAt(),
//                isOwner,
//                isOwner
//        );
//    }
}
