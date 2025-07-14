package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import ru.otus.hw.service.LocalizedIOService;
import ru.otus.hw.service.StudentService;
import ru.otus.hw.service.TestRunnerService;

@ShellComponent(value = "Application command")
@RequiredArgsConstructor
public class ApplicationShell {

    private final TestRunnerService testRunnerService;
    private final StudentService studentService;
    private final LocalizedIOService localizedIOService;

    @ShellMethod(value = "Introduce", key = {"introduce","intro"})
    public String introduce() {
        return localizedIOService.getMessage("ApplicationShell.introduce");
    }

    @ShellMethod(value = "Run testing", key = {"run", "rt", "r"})
    @ShellMethodAvailability("isRunCommandAvailable")
    public void runTest() {
        testRunnerService.run();
    }

    @ShellMethod(value = "Log in User", key = {"login", "li"})
    @ShellMethodAvailability("isLogInCommandAvailable")
    public String logIn() {
        studentService.logIn(localizedIOService.readStringWithPromptLocalized("StudentService.input.first.name"),
                             localizedIOService.readStringWithPromptLocalized("StudentService.input.last.name"));
        return localizedIOService.getMessage("ApplicationShell.log.in.success");
    }

    @ShellMethod(value = "Log out User", key = {"logout", "lo"})
    @ShellMethodAvailability("isLogOutCommandAvailable")
    public String logOut() {
        studentService.logOut();
        return localizedIOService.getMessage("ApplicationShell.log.out.success");
    }

    public Availability isLogOutCommandAvailable() {
        return studentService.getCurrentStudent() != null
                ? Availability.available()
                : Availability.unavailable(localizedIOService.getMessage("ApplicationShell.not.authorized"));
    }

    public Availability isRunCommandAvailable() {
        return studentService.getCurrentStudent() != null
                ? Availability.available()
                : Availability.unavailable(localizedIOService.getMessage("ApplicationShell.check.log.in"));
    }

    public Availability isLogInCommandAvailable() {
        return studentService.getCurrentStudent() == null
                ? Availability.available()
                : Availability.unavailable(localizedIOService.getMessage("ApplicationShell.check.log.out"));
    }

}
