package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.h2.tools.Console;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@RequiredArgsConstructor
@ShellComponent
public class H2ConsoleCommands {
    @ShellMethod(value = "Start H2 console", key = "h2" )
    public void startConsole() throws Exception{
        Console.main("-web", "-browser");
    }
}
