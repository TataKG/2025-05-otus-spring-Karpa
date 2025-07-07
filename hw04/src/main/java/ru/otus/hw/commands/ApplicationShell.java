package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import ru.otus.hw.service.LocalizedIOService;
import ru.otus.hw.service.StudentService;
import ru.otus.hw.service.TestRunnerService;

@ShellComponent(value = "Application Shell")
@RequiredArgsConstructor
public class ApplicationShell {

    private final TestRunnerService testRunnerService;
    private final StudentService studentService;
    private final LocalizedIOService localizedIOService;

    @ShellMethod(value = "Welcome", key = {"welcome", "w"})
    public void welcome() {
        localizedIOService.getMessage("ApplicationShell.hello");
    }

    @ShellMethod(value = "Run test", key = {"run", "rt", "r"})
    @ShellMethodAvailability("isStartCommandAvailable")
    public void runTest() {
        testRunnerService.run();
    }

    @ShellMethod(value = "Log in User", key = {"login", "li"})
    @ShellMethodAvailability("isLogInCommandAvailable")
    public String logIn() {
        studentService.logIn();
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

    public Availability isStartCommandAvailable() {
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
