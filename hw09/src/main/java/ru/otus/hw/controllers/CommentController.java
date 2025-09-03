package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.services.CommentService;

@RequiredArgsConstructor
@Controller
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/books/{bookId}/comments/{commentId}/deleteComment")
    public String deleteComment(@PathVariable("bookId") String bookId,
                                @PathVariable("commentId") String commentId) {
        commentService.deleteById(commentId);
        return "redirect:/book/view/" + bookId;
    }

    @PostMapping("/books/{bookId}/comments/addComment")
    public String saveComment(@PathVariable("bookId") String bookId, @ModelAttribute("comment") CommentDto commentDto) {
        commentService.insert(commentDto);
        return "redirect:/book/view/" + bookId;
    }
}
