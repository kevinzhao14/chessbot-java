package com.example.demo;

import com.example.demo.ai.Bot;
import com.example.demo.ai.objects.BestMove;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChessController {

    @CrossOrigin
    @GetMapping("/getMove")
    public HttpEntity<? extends Object> getMove(@RequestParam("fen") String fen) {
        BestMove result = Bot.getMove(fen);
        return ResponseEntity.ok().body(result.toString());
    }

    @CrossOrigin
    @GetMapping("/getMovePath/{fen}")
    public HttpEntity<? extends Object> getMovePath(@PathVariable String fen) {
        System.out.println("path " + fen);
        BestMove result = Bot.getMove(fen);
        return ResponseEntity.ok().body(result.toString());
    }
}
